package com.example.fruitgrade.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fruitgrade.R
import com.example.fruitgrade.ui.theme.FreshGreen
import com.example.fruitgrade.ui.theme.FreshGreenDark
import com.example.fruitgrade.viewmodel.ScanViewModel
import java.io.File
import java.util.concurrent.Executors

// ---------------------------------------------------------------------------
// ScanScreen — Redesigned capture experience
// ---------------------------------------------------------------------------

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onResult: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Camera permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Gallery picker — adds a single image to the captured slots (batch mode)
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            bitmap?.let { bmp -> viewModel.captureImage(bmp) }
        }
    }

    // Solo gallery picker — picks one image and runs inference immediately
    val soloGalleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            bitmap?.let { bmp ->
                viewModel.startScan()
                viewModel.captureImage(bmp)
            }
        }
    }

    // Request camera permission on first launch
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Auto-start camera only for batch mode
    LaunchedEffect(hasPermission, uiState.scanMode) {
        if (hasPermission && !uiState.isScanning && uiState.scanMode == "batch") {
            viewModel.startScan()
        }
    }

    // Navigate to result when inference completes
    LaunchedEffect(uiState.result) {
        if (uiState.result != null && !uiState.isLoading) {
            onResult()
        }
    }

    // Derive model badge text
    val modelBadge = when {
        uiState.modelName.contains("large") -> "MobileNetV3 – L"
        else -> "MobileNetV3 – S"
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            ScanBottomNavigation(selectedRoute = "scan")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ──────────────────────────────────────────────
            //  Header: fruit info + model badge
            // ──────────────────────────────────────────────
            FruitHeader(
                fruitName = uiState.fruitName,
                scientificName = uiState.fruitScientificName,
                modelBadge = modelBadge,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Solo mode: show method selection, then camera or gallery
            // Batch mode: show camera viewfinder + captured samples
            val isSolo = uiState.scanMode == "solo"
            val showMethodSelection = isSolo && !uiState.isScanning && uiState.capturedCount == 0

            if (showMethodSelection) {
                // ──────────────────────────────────────────
                //  Solo: capture method selection
                // ──────────────────────────────────────────
                SoloMethodSelection(
                    onCameraSelected = {
                        if (hasPermission) {
                            viewModel.startScan()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onGallerySelected = {
                        soloGalleryPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
            } else {
                // ──────────────────────────────────────────
                //  Camera viewfinder
                // ──────────────────────────────────────────
                CameraViewfinder(
                    hasPermission = hasPermission,
                    isScanning = uiState.isScanning,
                    isProcessing = uiState.isProcessing,
                    inferenceProgress = uiState.inferenceProgress,
                    cameraReady = uiState.cameraReady,
                    error = uiState.error,
                    onCameraReady = { viewModel.onCameraReady() },
                    onCameraError = { viewModel.onCameraError(it) },
                    onCapture = { bitmap -> viewModel.captureImage(bitmap) },
                    onRetry = { viewModel.retryCamera(); viewModel.startScan() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Captured samples (batch only — solo fills automatically)
                if (!isSolo) {
                    CapturedSamplesSection(
                        thumbnails = uiState.thumbnails,
                        maxCaptures = uiState.maxCaptures,
                        isProcessing = uiState.isProcessing,
                        onAddFromGallery = {
                            galleryPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ──────────────────────────────────────────────
            //  Photo guidelines
            // ──────────────────────────────────────────────
            PhotoGuidelinesSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Fruit header
// ---------------------------------------------------------------------------

@Composable
private fun FruitHeader(
    fruitName: String,
    scientificName: String,
    modelBadge: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF0F0).copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF424242),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Fruit name + scientific name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fruitName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F),
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = scientificName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF757575),
                        fontSize = 13.sp
                    )
                )
            }

            // Model badge
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = FreshGreenDark,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = modelBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = FreshGreenDark,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Solo mode: capture method selection
// ---------------------------------------------------------------------------

@Composable
private fun SoloMethodSelection(
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading
        Text(
            text = "How would you like to capture?",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242),
                letterSpacing = 0.3.sp
            )
        )

        // ---- Camera card ----
        CaptureMethodCard(
            icon = Icons.Default.Camera,
            accentColor = FreshGreen,
            title = "Take Photo",
            subtitle = "Open camera and capture a fruit image",
            onClick = onCameraSelected
        )

        // ---- Gallery card ----
        CaptureMethodCard(
            icon = Icons.Default.PhotoLibrary,
            accentColor = Color(0xFF5C6BC0),
            title = "Upload from Gallery",
            subtitle = "Select an existing photo from your device",
            onClick = onGallerySelected
        )
    }
}

@Composable
private fun CaptureMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B1B1F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Camera viewfinder with corner brackets
// ---------------------------------------------------------------------------

@Composable
private fun CameraViewfinder(
    hasPermission: Boolean,
    isScanning: Boolean,
    isProcessing: Boolean,
    inferenceProgress: Float,
    cameraReady: Boolean,
    error: String?,
    onCameraReady: () -> Unit,
    onCameraError: (String) -> Unit,
    onCapture: (Bitmap) -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    var showFlash by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Dark background (visible before camera loads)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        )

        // Camera preview
        if (hasPermission && isScanning && error == null) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture.value = capture
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                                onCameraReady()
                            } catch (e: Exception) {
                                onCameraError(e.message ?: "Failed to bind camera")
                            }
                        } catch (e: Exception) {
                            onCameraError(e.message ?: "Failed to initialize camera")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = !isProcessing) {
                        // ---- Capture on tap ----
                        showFlash = true
                        val capture = imageCapture.value ?: return@clickable
                        val file = File(
                            context.cacheDir,
                            "capture_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions =
                            ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(
                            outputOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    output: ImageCapture.OutputFileResults
                                ) {
                                    val bitmap =
                                        BitmapFactory.decodeFile(file.absolutePath)
                                    val rotated =
                                        rotateBitmapIfNeeded(bitmap, file.absolutePath)
                                    onCapture(rotated)
                                    showFlash = false
                                }

                                override fun onError(
                                    exception: ImageCaptureException
                                ) {
                                    exception.printStackTrace()
                                    showFlash = false
                                }
                            }
                        )
                    }
            )
        }

        // Shutter flash overlay
        AnimatedVisibility(
            visible = showFlash,
            enter = fadeIn(animationSpec = tween(50)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Corner bracket overlay
        CornerBracketsOverlay()

        // Instruction overlay (show when camera ready and not processing)
        if (!isProcessing && (!cameraReady || !isScanning)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to scan photo",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "One fruit per photo",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // Processing overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Analyzing…",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    if (inferenceProgress > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { inferenceProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .clip(RoundedCornerShape(4.dp)),
                            color = FreshGreen,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${(inferenceProgress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Error overlay
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Corner brackets overlay (drawn via Canvas)
// ---------------------------------------------------------------------------

@Composable
private fun CornerBracketsOverlay() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeW = 3.dp.toPx()
        val cornerLen = 28.dp.toPx()
        val pad = 14.dp.toPx()
        val color = Color.White.copy(alpha = 0.8f)

        // Top-left
        drawLine(color, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeW, StrokeCap.Round)
        drawLine(color, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeW, StrokeCap.Round)

        // Top-right
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad - cornerLen, pad), strokeW, StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad, pad + cornerLen), strokeW, StrokeCap.Round)

        // Bottom-left
        drawLine(color, Offset(pad, size.height - pad), Offset(pad + cornerLen, size.height - pad), strokeW, StrokeCap.Round)
        drawLine(color, Offset(pad, size.height - pad), Offset(pad, size.height - pad - cornerLen), strokeW, StrokeCap.Round)

        // Bottom-right
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - cornerLen, size.height - pad), strokeW, StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - cornerLen), strokeW, StrokeCap.Round)
    }
}

// ---------------------------------------------------------------------------
// Captured samples section
// ---------------------------------------------------------------------------

@Composable
private fun CapturedSamplesSection(
    thumbnails: List<Bitmap>,
    maxCaptures: Int,
    isProcessing: Boolean,
    onAddFromGallery: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "CAPTURED SAMPLES",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242),
                letterSpacing = 1.sp,
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 0 until maxCaptures) {
                if (i < thumbnails.size) {
                    // Filled slot — show captured thumbnail
                    CapturedSlot(
                        bitmap = thumbnails[i],
                        index = i,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Empty slot — "+" button to add from gallery
                    EmptySlot(
                        enabled = !isProcessing && thumbnails.size == i,
                        onClick = onAddFromGallery,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CapturedSlot(
    bitmap: Bitmap,
    index: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Sample ${index + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .border(2.dp, FreshGreen, RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        // Small index badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(FreshGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptySlot(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F5F5))
            .border(
                width = 1.5.dp,
                color = if (enabled) Color(0xFFBDBDBD) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(10.dp)
            )
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = if (enabled) Color(0xFF9E9E9E) else Color(0xFFE0E0E0),
            fontSize = 24.sp,
            fontWeight = FontWeight.Light
        )
    }
}

// ---------------------------------------------------------------------------
// Photo guidelines section
// ---------------------------------------------------------------------------

@Composable
private fun PhotoGuidelinesSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "PHOTO GUIDELINES",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242),
                letterSpacing = 1.sp,
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Good practices
        GuidelineItem(
            text = "Center single fruit on plain background",
            isGood = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        GuidelineItem(
            text = "Good, even lighting – good or diffuse",
            isGood = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ❌ Bad practices
        GuidelineItem(
            text = "Multiple fruits in one frame",
            isGood = false
        )
        Spacer(modifier = Modifier.height(8.dp))
        GuidelineItem(
            text = "Blurry or poorly lit images",
            isGood = false
        )
    }
}

@Composable
private fun GuidelineItem(text: String, isGood: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isGood) FreshGreen else Color(0xFFE53935),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF424242),
                fontSize = 13.sp
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation (scan tab selected)
// ---------------------------------------------------------------------------

@Composable
private fun ScanBottomNavigation(selectedRoute: String) {
    NavigationBar(
        containerColor = FreshGreenDark,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = { },
            icon = { Icon(Icons.Default.Home, "Home", Modifier.size(24.dp)) },
            label = { Text("Home", fontSize = 11.sp) },
            colors = scanNavItemColors()
        )
        NavigationBarItem(
            selected = selectedRoute == "scan",
            onClick = { },
            icon = { Icon(Icons.Default.CameraEnhance, "Capture", Modifier.size(24.dp)) },
            label = { Text("Capture", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = scanNavItemColors()
        )
        NavigationBarItem(
            selected = selectedRoute == "result",
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.result),
                    contentDescription = "Result",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
                )
            },
            label = { Text("Result", fontSize = 11.sp) },
            colors = scanNavItemColors()
        )
        NavigationBarItem(
            selected = selectedRoute == "history",
            onClick = { },
            icon = { Icon(Icons.Default.BarChart, "Summary", Modifier.size(24.dp)) },
            label = { Text("Summary", fontSize = 11.sp) },
            colors = scanNavItemColors()
        )
    }
}

@Composable
private fun scanNavItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = Color.White.copy(alpha = 0.7f),
    unselectedTextColor = Color.White.copy(alpha = 0.7f),
    indicatorColor = Color.White.copy(alpha = 0.15f)
)



private fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun rotateBitmapIfNeeded(bitmap: Bitmap, path: String): Bitmap {
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
    )
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
