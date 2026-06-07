package com.example.fruitgrade.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fruitgrade.data.ScanResult
import com.example.fruitgrade.ui.theme.FreshGreen
import com.example.fruitgrade.viewmodel.HistoryViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// HistoryScreen – redesigned
// ---------------------------------------------------------------------------

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val viewModel: HistoryViewModel = viewModel()
    val history by viewModel.history.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    // Staggered card entrance animation
    var visibleCards by remember { mutableIntStateOf(0) }
    LaunchedEffect(history) {
        visibleCards = 0
        for (i in history.indices) {
            delay(60L)
            visibleCards = i + 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Custom top bar ──────────────────────────────────────────────
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF212121),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF424242),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F),
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Content area ────────────────────────────────────────────────
        if (history.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "No scans yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161),
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Your scan history will appear here",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF9E9E9E),
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Scan count
            Text(
                text = "${history.size} scan${if (history.size > 1) "s" else ""} recorded",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(history, key = { _, scan -> scan.id }) { index, scan ->
                    AnimatedVisibility(
                        visible = index < visibleCards,
                        enter = fadeIn(animationSpec = tween(350)) +
                                slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        HistoryCard(
                            scan = scan,
                            onDelete = { viewModel.deleteScan(scan.id) }
                        )
                    }
                }
                // Bottom spacing
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Redesigned history card
// ---------------------------------------------------------------------------

@Composable
fun HistoryCard(scan: ScanResult, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "historyCardScale"
    )

    val date = remember(scan.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            .format(Date(scan.timestamp))
    }
    val imagePaths = remember(scan.previewImagePaths) {
        scan.imagePathsList().filter { File(it).exists() }
    }

    val gc = gradeColor(scan.finalGrade)
    val gradeNumber = when (scan.finalGrade) {
        "unripe" -> "1"
        "ripe" -> "2"
        "overripe" -> "3"
        "rotten" -> "4"
        else -> "?"
    }

    // Model display name
    val modelDisplay = remember(scan.modelName) {
        val raw = scan.modelName
            .replace(".tflite", "")
            .replace("_", " ")
        raw.split(" ").joinToString(" ") { w ->
            w.replaceFirstChar { it.uppercase() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { expanded = !expanded }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // ── Left green accent bar ──
           

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // ── Fruit thumbnail ──
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imagePaths.isNotEmpty()) {
                            val bitmap = remember(imagePaths[0]) {
                                BitmapFactory.decodeFile(imagePaths[0])
                            }
                            bitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            // Fallback icon placeholder
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // ── Text content ──
                    Column(modifier = Modifier.weight(1f)) {
                        // Date
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF9E9E9E),
                                fontSize = 11.sp,
                                letterSpacing = 0.2.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Grade label
                        Text(
                            text = gradeLabel(scan.finalGrade),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121),
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Metadata: model · mode · duration
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$modelDisplay model",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                            )
                            MetaDot()
                            Text(
                                text = scan.scanMode.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                            )
                            MetaDot()
                            Text(
                                text = "${scan.durationMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ── Grade number badge ──
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(gc),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = gradeNumber,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // ── Expanded details ──
                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(10.dp))

                        // All preview images
                        if (imagePaths.size > 1) {
                            Text(
                                "Captured Images:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF424242),
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(imagePaths) { path ->
                                    val bitmap = remember(path) {
                                        BitmapFactory.decodeFile(path)
                                    }
                                    bitmap?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Scan image",
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFF5F5F5)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Method used
                        Text(
                            text = "Method: ${
                                scan.methodUsed.replace("_", " ")
                                    .replaceFirstChar { it.uppercase() }
                            }",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF757575),
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Individual predictions
                        Text(
                            "Individual Predictions:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        scan.predictionsList().forEachIndexed { index, pair ->
                            val predColor = gradeColor(pair.first)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(predColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Image ${index + 1}: ${gradeLabel(pair.first)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF424242),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Text(
                                    "${"%.1f".format(pair.second * 100)}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF424242),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delete action
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Delete",
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small dot separator for metadata row
// ---------------------------------------------------------------------------

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .size(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFBDBDBD))
    )
}
