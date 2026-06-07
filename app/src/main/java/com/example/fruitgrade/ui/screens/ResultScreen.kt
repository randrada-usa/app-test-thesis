package com.example.fruitgrade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fruitgrade.R
import com.example.fruitgrade.ui.theme.FreshGreen
import com.example.fruitgrade.ui.theme.FreshGreenDark
import com.example.fruitgrade.viewmodel.ScanViewModel

// ---------------------------------------------------------------------------
// Grade helpers
// ---------------------------------------------------------------------------

fun gradeColor(grade: String): Color {
    return when (grade) {
        "unripe" -> Color(0xFF2E7D32)   // Green
        "ripe" -> Color(0xFF1565C0)     // Blue
        "overripe" -> Color(0xFFF57F17) // Amber
        "rotten" -> Color(0xFFD32F2F)   // Red
        else -> Color.Gray
    }
}

fun gradeLabel(grade: String): String {
    return when (grade) {
        "unripe" -> "Grade 1 – Unripe"
        "ripe" -> "Grade 2 – Ripe"
        "overripe" -> "Grade 3 – Overripe"
        "rotten" -> "Grade 4 – Rotten"
        else -> grade
    }
}

private fun gradeShortLabel(grade: String): String {
    return when (grade) {
        "unripe" -> "G1"
        "ripe" -> "G2"
        "overripe" -> "G3"
        "rotten" -> "G4"
        else -> "?"
    }
}

private fun gradeNameOnly(grade: String): String {
    return when (grade) {
        "unripe" -> "Unripe"
        "ripe" -> "Ripe"
        "overripe" -> "Overripe"
        "rotten" -> "Rotten"
        else -> grade.replaceFirstChar { it.uppercase() }
    }
}

private fun gradeBgColor(grade: String): Color {
    return when (grade) {
        "unripe" -> Color(0xFFE8F5E9)
        "ripe" -> Color(0xFFE3F2FD)
        "overripe" -> Color(0xFFFFF8E1)
        "rotten" -> Color(0xFFFCE4EC)
        else -> Color(0xFFF5F5F5)
    }
}

// ---------------------------------------------------------------------------
// ResultScreen
// ---------------------------------------------------------------------------

@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(44.dp))

        // ── View History button (top-right) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = onHistory,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View History",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (result != null) {
            val gc = gradeColor(result.finalGrade)
            val predictions = result.predictionsList()
            val isBatch = predictions.size > 1

            // ──────────────────────────────────────────
            //  Hero grade card
            // ──────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.85f
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = gradeBgColor(result.finalGrade)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.5.dp,
                                color = gc.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(
                                vertical = if (isBatch) 18.dp else 24.dp,
                                horizontal = 20.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // "Dominant Grade  •  Majority Vote"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dominant Grade",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF424242),
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(gc)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Majority Vote",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF424242),
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isBatch) 10.dp else 14.dp))

                        // Large grade badge
                        Box(
                            modifier = Modifier
                                .size(if (isBatch) 64.dp else 76.dp)
                                .clip(CircleShape)
                                .background(gc),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gradeShortLabel(result.finalGrade),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = if (isBatch) 28.sp else 34.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isBatch) 6.dp else 10.dp))

                        // Grade name
                        Text(
                            text = gradeNameOnly(result.finalGrade),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B1B1F),
                                fontSize = if (isBatch) 20.sp else 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Confidence
                        Text(
                            text = "${"%.1f".format(result.finalConfidence * 100)}% Confidence",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = gc,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(if (isBatch) 6.dp else 10.dp))

                        // Mode chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(gc.copy(alpha = 0.15f))
                                .padding(horizontal = 18.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = result.scanMode.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = gc,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isBatch) 14.dp else 20.dp))

            // ──────────────────────────────────────────
            //  Scan Details card
            // ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Scan Details",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // Duration — timer icon
                    DetailRowIcon(
                        icon = Icons.Default.Timer,
                        label = "Duration:",
                        value = "${result.durationMs} MS"
                    )

                    // Model — custom history icon (reusing as model icon)
                    DetailRowDrawable(
                        drawableRes = R.drawable.history,
                        label = "Model:",
                        value = result.modelName.replaceFirstChar { it.uppercase() }
                    )

                    // Mode — custom modescan icon
                    DetailRowDrawable(
                        drawableRes = R.drawable.modescan,
                        label = "Mode:",
                        value = result.scanMode.replaceFirstChar { it.uppercase() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isBatch) 10.dp else 12.dp))

            // ──────────────────────────────────────────
            //  Individual Predictions card
            // ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isBatch) 4.dp else 6.dp)
                ) {
                    Text(
                        text = "Individual Predictions",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            fontSize = 15.sp
                        )
                    )

                    predictions.forEachIndexed { index, pair ->
                        val predLabel = "Grade ${gradeShortLabel(pair.first).last()} –  ${gradeNameOnly(pair.first)}"
                        val predColor = gradeColor(pair.first)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(predColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Image ${index + 1}: $predLabel",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF424242),
                                        fontSize = 13.sp
                                    )
                                )
                            }
                            Text(
                                text = "${"%.1f".format(pair.second * 100)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF424242),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "No result available",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF9E9E9E)
            )
        }

        Spacer(modifier = Modifier.height(if (result != null && result.predictionsList().size > 1) 16.dp else 24.dp))

        // ──────────────────────────────────────────
        //  Action buttons
        // ──────────────────────────────────────────

        // Back to Home
        Button(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Back to Home",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Retry
        OutlinedButton(
            onClick = { onRetry?.invoke() ?: onHome() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenDark)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = FreshGreenDark
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Retry",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FreshGreenDark
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ---------------------------------------------------------------------------
// Detail row with Material icon
// ---------------------------------------------------------------------------

@Composable
private fun DetailRowIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = FreshGreen
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                fontSize = 14.sp
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Detail row with drawable resource icon
// ---------------------------------------------------------------------------

@Composable
private fun DetailRowDrawable(
    drawableRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(FreshGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                fontSize = 14.sp
            )
        )
    }
}
