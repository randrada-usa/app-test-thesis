package com.example.fruitgrade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fruitgrade.data.ScanResult
import com.example.fruitgrade.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val viewModel: HistoryViewModel = viewModel()
    val history by viewModel.history.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (history.isEmpty()) {
                Text(
                    "No scans yet.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(history) { scan ->
                        HistoryCard(scan = scan, onDelete = { viewModel.deleteScan(scan.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(scan: ScanResult, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val date = remember(scan.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(scan.timestamp))
    }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = gradeColor(scan.finalGrade).copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(date, style = MaterialTheme.typography.labelSmall)
            Text(
                gradeLabel(scan.finalGrade),
                style = MaterialTheme.typography.titleMedium,
                color = gradeColor(scan.finalGrade)
            )
            Text("Model: ${scan.modelName} | Mode: ${scan.scanMode}")
            Text("Duration: ${scan.durationMs} ms | Confidence: ${"%.1f".format(scan.finalConfidence * 100)}%")

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Method: ${scan.methodUsed}")
                Text("Individual Predictions:", style = MaterialTheme.typography.titleSmall)
                scan.predictionsList().forEachIndexed { index, pair ->
                    Text("  Image ${index + 1}: ${gradeLabel(pair.first)} (${"%.1f".format(pair.second * 100)}%)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}
