package com.example.fruitgrade.ml

data class BatchResult(
    val finalGrade: String,
    val finalConfidence: Float,
    val methodUsed: String,
    val individualPredictions: List<Pair<String, Float>>
)

class BatchAggregator {
    fun aggregate(predictions: List<Pair<String, Float>>): BatchResult {
        if (predictions.isEmpty()) {
            throw IllegalArgumentException("Predictions list cannot be empty")
        }

        val votes = predictions.map { it.first }
        val majority = votes.groupingBy { it }.eachCount()
        val maxCount = majority.values.maxOrNull() ?: 0
        val winners = majority.filter { it.value == maxCount }.keys.toList()

        val method: String
        val finalGrade: String

        if (winners.size == 1) {
            finalGrade = winners[0]
            method = "majority_vote"
        } else {
            val avgConfidence = winners.associateWith { grade ->
                predictions.filter { it.first == grade }.map { it.second }.average().toFloat()
            }
            finalGrade = avgConfidence.maxBy { it.value }.key
            method = "tie_broken_by_confidence"
        }

        val overallConfidence = predictions.map { it.second }.average().toFloat()
        val gradeConfidence = predictions.filter { it.first == finalGrade }.map { it.second }.average().toFloat()

        val finalMethod = if (overallConfidence < 0.40f) {
            "low_confidence"
        } else {
            method
        }

        return BatchResult(
            finalGrade = finalGrade,
            finalConfidence = gradeConfidence,
            methodUsed = finalMethod,
            individualPredictions = predictions
        )
    }
}
