package com.example.fruitgrade.viewmodel

import android.app.Application
import android.graphics.Bitmap
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
import kotlinx.coroutines.withContext

data class ScanUiState(
    val modelName: String = "small_model.tflite",
    val scanMode: String = "solo",
    val isScanning: Boolean = false,
    val capturedCount: Int = 0,
    val maxCaptures: Int = 1,
    val thumbnails: List<Bitmap> = emptyList(),
    val result: ScanResult? = null,
    val durationMs: Long = 0,
    val isLoading: Boolean = false,
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

    fun setModelAndMode(modelName: String, mode: String) {
        val max = if (mode == "solo") 1 else 5
        _uiState.value = ScanUiState(
            modelName = modelName,
            scanMode = mode,
            maxCaptures = max
        )
    }

    fun startScan() {
        startTime = System.currentTimeMillis()
        classifier?.close()
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            capturedCount = 0,
            thumbnails = emptyList(),
            result = null,
            durationMs = 0,
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
            error = "Camera error: $error"
        )
    }

    fun retryCamera() {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
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
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Create classifier if needed (should already be created in startScan)
                if (classifier == null) {
                    classifier = TFLiteClassifier(getApplication(), _uiState.value.modelName)
                }
                
                val predictions = bitmaps.map { bitmap ->
                    val buffer = preprocessor.preprocess(bitmap)
                    classifier?.classify(buffer) ?: ("unknown" to 0f)
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
                    methodUsed = result.methodUsed
                )
                val id = repository.saveScan(scan)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    result = scan.copy(id = id),
                    durationMs = duration,
                    cameraReady = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Inference failed"
                )
            }
        }
    }

    fun runGalleryInference(bitmaps: List<Bitmap>) {
        startTime = System.currentTimeMillis()
        classifier?.close()
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            capturedCount = bitmaps.size,
            thumbnails = bitmaps,
            result = null,
            durationMs = 0,
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
