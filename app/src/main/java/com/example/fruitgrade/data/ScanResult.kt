package com.example.fruitgrade.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val modelName: String,
    val scanMode: String,
    val durationMs: Long,
    val individualPredictions: String,
    val individualConfidences: String,
    val finalGrade: String,
    val finalConfidence: Float,
    val methodUsed: String
) {
    companion object {
        fun fromPredictions(
            timestamp: Long,
            modelName: String,
            scanMode: String,
            durationMs: Long,
            predictions: List<Pair<String, Float>>,
            finalGrade: String,
            finalConfidence: Float,
            methodUsed: String
        ): ScanResult {
            val grades = JSONArray()
            val confidences = JSONArray()
            predictions.forEach {
                grades.put(it.first)
                confidences.put(it.second)
            }
            return ScanResult(
                timestamp = timestamp,
                modelName = modelName,
                scanMode = scanMode,
                durationMs = durationMs,
                individualPredictions = grades.toString(),
                individualConfidences = confidences.toString(),
                finalGrade = finalGrade,
                finalConfidence = finalConfidence,
                methodUsed = methodUsed
            )
        }
    }

    fun predictionsList(): List<Pair<String, Float>> {
        val grades = JSONArray(individualPredictions)
        val confs = JSONArray(individualConfidences)
        val list = mutableListOf<Pair<String, Float>>()
        for (i in 0 until grades.length()) {
            list.add(grades.getString(i) to confs.getDouble(i).toFloat())
        }
        return list
    }
}
