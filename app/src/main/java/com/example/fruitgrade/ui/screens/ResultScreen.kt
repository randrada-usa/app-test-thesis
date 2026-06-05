package com.example.fruitgrade.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fruitgrade.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    onHome: () -> Unit,
    onHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    Scaffold(
        topBar = { TopAppBar(title = { Text("Result") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (result != null) {
                val gradeText = when (result.finalGrade) {
                    "unripe" -> "Grade 1 — Unripe"
                    "ripe" -> "Grade 2 — Ripe"
                    "overripe" -> "Grade 3 — Overripe"
                    "rotten" -> "Grade 4 — Rotten"
                    else -> result.finalGrade
                }
                Text("Grade: $gradeText", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Confidence: ${"%.1f".format(result.finalConfidence * 100)}%")
                Text("Method: ${result.methodUsed}")
                Text("Duration: ${result.durationMs} ms")
                Text("Model: ${result.modelName}")
                Text("Mode: ${result.scanMode}")
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text("No result available")
            }
            Button(onClick = onHome) { Text("Home") }
            Button(onClick = onHistory) { Text("View History") }
        }
    }
}
