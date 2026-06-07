package com.example.fruitgrade.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fruitgrade.data.HistoryRepository
import com.example.fruitgrade.data.ScanResult
import com.example.fruitgrade.ml.BatchAggregator
import com.example.fruitgrade.ml.BatchResult
import com.example.fruitgrade.ml.ImagePreprocessor
import com.example.fruitgrade.ml.TFLiteClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ScanUiState(
    val fruitName: String = "",
    val fruitScientificName: String = "",
    val modelName: String = "small_model.tflite",
    val scanMode: String = "solo",
    val isScanning: Boolean = false,
    val capturedCount: Int = 0,
    val maxCaptures: Int = 1,
    val thumbnails: List<Bitmap> = emptyList(),
    val result: ScanResult? = null,
    val durationMs: Long = 0,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val inferenceProgress: Float = 0f,
    val error: String? = null,
    val cameraReady: Boolean = false
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistoryRepository(application)
    private val preprocessor = ImagePreprocessor()
    private val aggregator = BatchAggregator()
    private var classifier: TFLiteClassifier? = null

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var startTime: Long = 0

    fun setModelAndMode(
        fruitName: String,
        fruitScientificName: String,
        modelName: String,
        mode: String
    ) {
        val max = if (mode == "solo") 1 else 5
        _uiState.value = ScanUiState(
            fruitName = fruitName,
            fruitScientificName = fruitScientificName,
            modelName = modelName,
            scanMode = mode,
            maxCaptures = max
        )
    }

    fun startScan() {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            capturedCount = 0,
            thumbnails = emptyList(),
            result = null,
            durationMs = 0,
            isProcessing = false,
            inferenceProgress = 0f,
            error = null,
            cameraReady = false
        )
    }

    fun onCameraReady() {
        _uiState.value = _uiState.value.copy(cameraReady = true)
    }

    fun onCameraError(error: String) {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            isProcessing = false,
            error = "Camera error: $error"
        )
    }

    fun retryCamera() {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            isProcessing = false,
            error = null,
            cameraReady = false
        )
    }

    fun captureImage(bitmap: Bitmap) {
        val current = _uiState.value
        val newThumbnails = current.thumbnails.toMutableList()
        newThumbnails.add(bitmap)
        val newCount = current.capturedCount + 1
        _uiState.value = current.copy(
            capturedCount = newCount,
            thumbnails = newThumbnails
        )
        if (newCount >= current.maxCaptures) {
            runInference(newThumbnails)
        }
    }

    private fun runInference(bitmaps: List<Bitmap>) {
        _uiState.value = _uiState.value.copy(isProcessing = true, isLoading = true, inferenceProgress = 0f)
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Start timing ONLY when inference begins
                startTime = System.currentTimeMillis()

                // Create classifier if not already created (reuse across scans)
                if (classifier == null) {
                    classifier = TFLiteClassifier(getApplication(), _uiState.value.modelName)
                }

                // Save preview images
                val imagePaths = mutableListOf<String>()
                bitmaps.forEachIndexed { index, bitmap ->
                    val path = savePreviewImage(bitmap, index)
                    if (path != null) imagePaths.add(path)
                }
                
                val predictions = bitmaps.mapIndexed { index, bitmap ->
                    val buffer = preprocessor.preprocess(bitmap)
                    val result = classifier?.classify(buffer) ?: ("unknown" to 0f)
                    // Update progress after each image
                    val progress = (index + 1).toFloat() / bitmaps.size
                    _uiState.value = _uiState.value.copy(inferenceProgress = progress)
                    result
                }

                val result: BatchResult = if (predictions.size == 1) {
                    BatchResult(
                        finalGrade = predictions[0].first,
                        finalConfidence = predictions[0].second,
                        methodUsed = "solo",
                        individualPredictions = predictions
                    )
                } else {
                    aggregator.aggregate(predictions)
                }

                val duration = System.currentTimeMillis() - startTime
                val scan = ScanResult.fromPredictions(
                    timestamp = System.currentTimeMillis(),
                    modelName = _uiState.value.modelName,
                    scanMode = _uiState.value.scanMode,
                    durationMs = duration,
                    predictions = predictions,
                    finalGrade = result.finalGrade,
                    finalConfidence = result.finalConfidence,
                    methodUsed = result.methodUsed,
                    previewImagePaths = imagePaths
                )
                val id = repository.saveScan(scan)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    isProcessing = false,
                    result = scan.copy(id = id),
                    durationMs = duration,
                    inferenceProgress = 1f,
                    cameraReady = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessing = false,
                    error = e.message ?: "Inference failed"
                )
            }
        }
    }

    private fun savePreviewImage(bitmap: Bitmap, index: Int): String? {
        return try {
            val context = getApplication<Application>()
            val dir = File(context.filesDir, "previews")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${UUID.randomUUID()}_${index}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun runGalleryInference(bitmaps: List<Bitmap>) {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            capturedCount = bitmaps.size,
            thumbnails = bitmaps,
            result = null,
            durationMs = 0,
            isProcessing = false,
            inferenceProgress = 0f,
            error = null,
            cameraReady = true
        )
        runInference(bitmaps)
    }

    fun reset() {
        classifier?.close()
        classifier = null
        _uiState.value = ScanUiState()
    }

    override fun onCleared() {
        super.onCleared()
        classifier?.close()
    }
}
