package com.example.fruitgrade.ml

import org.junit.Assert.*
import org.junit.Test

class BatchAggregatorTest {
    @Test
    fun majorityVoteWins() {
        val predictions = listOf(
            "ripe" to 0.92f,
            "ripe" to 0.88f,
            "overripe" to 0.76f,
            "ripe" to 0.95f,
            "overripe" to 0.81f
        )
        val result = BatchAggregator().aggregate(predictions)
        assertEquals("ripe", result.finalGrade)
        assertEquals("majority_vote", result.methodUsed)
    }

    @Test
    fun tieBrokenByConfidence() {
        val predictions = listOf(
            "ripe" to 0.60f,
            "ripe" to 0.60f,
            "overripe" to 0.90f,
            "overripe" to 0.90f,
            "rotten" to 0.10f
        )
        val result = BatchAggregator().aggregate(predictions)
        assertEquals("overripe", result.finalGrade)
        assertEquals("tie_broken_by_confidence", result.methodUsed)
    }

    @Test
    fun lowConfidenceFlagged() {
        val predictions = listOf(
            "ripe" to 0.30f,
            "ripe" to 0.35f,
            "overripe" to 0.32f,
            "overripe" to 0.30f,
            "rotten" to 0.28f
        )
        val result = BatchAggregator().aggregate(predictions)
        assertEquals("low_confidence", result.methodUsed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyListThrows() {
        BatchAggregator().aggregate(emptyList())
    }
}
