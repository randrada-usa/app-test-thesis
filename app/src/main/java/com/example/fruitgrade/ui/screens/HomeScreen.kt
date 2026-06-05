package com.example.fruitgrade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScan: (modelName: String, mode: String) -> Unit,
    onHistory: () -> Unit
) {
    val models = listOf("small_model.tflite" to "Small", "large_model.tflite" to "Large")
    val modes = listOf("solo" to "Solo Grade", "batch" to "Batch Grade")

    var selectedModel by remember { mutableStateOf(models[0]) }
    var selectedMode by remember { mutableStateOf(modes[0]) }
    var modelExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FruitGrade") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Model", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = it }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedModel.second,
                    onValueChange = {},
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.second) },
                            onClick = {
                                selectedModel = model
                                modelExpanded = false
                            }
                        )
                    }
                }
            }

            Text("Select Mode", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = it }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedMode.second,
                    onValueChange = {},
                    label = { Text("Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    modes.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.second) },
                            onClick = {
                                selectedMode = mode
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onScan(selectedModel.first, selectedMode.first) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Scan")
            }

            Button(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View History")
            }
        }
    }
}
