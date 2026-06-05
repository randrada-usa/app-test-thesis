package com.example.fruitgrade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fruitgrade.viewmodel.ScanViewModel

fun gradeColor(grade: String): Color {
    return when (grade) {
        "unripe" -> Color(0xFF8B4513)
        "ripe" -> Color(0xFF4CAF50)
        "overripe" -> Color(0xFFFFC107)
        "rotten" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

fun gradeLabel(grade: String): String {
    return when (grade) {
        "unripe" -> "Grade 1 — Unripe"
        "ripe" -> "Grade 2 — Ripe"
        "overripe" -> "Grade 3 — Overripe"
        "rotten" -> "Grade 4 — Rotten"
        else -> grade
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    onHome: () -> Unit,
    onHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Result") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (result != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = gradeColor(result.finalGrade).copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            gradeLabel(result.finalGrade),
                            style = MaterialTheme.typography.headlineMedium,
                            color = gradeColor(result.finalGrade)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Confidence: ${"%.1f".format(result.finalConfidence * 100)}%")
                        Text("Method: ${result.methodUsed}")
                        Text("Duration: ${result.durationMs} ms")
                        Text("Model: ${result.modelName}")
                        Text("Mode: ${result.scanMode}")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Individual Predictions", style = MaterialTheme.typography.titleMedium)
                result.predictionsList().forEachIndexed { index, pair ->
                    val label = gradeLabel(pair.first)
                    Text("Image ${index + 1}: $label (${"%.1f".format(pair.second * 100)}%)")
                }
            } else {
                Text("No result available")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Home") }
            Button(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("View History") }
        }
    }
}
