package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ExamAttemptRulesTest {
    @Test fun weightedPercentageUsesPointsAndClampsRange() {
        assertEquals(62.5, ExamAttemptRules.percentage(5.0, 8.0), 0.0001)
        assertEquals(100.0, ExamAttemptRules.percentage(12.0, 8.0), 0.0001)
        assertEquals(0.0, ExamAttemptRules.percentage(-2.0, 8.0), 0.0001)
    }

    @Test fun invalidPossiblePointsProduceZero() {
        assertEquals(0.0, ExamAttemptRules.percentage(5.0, 0.0), 0.0001)
        assertEquals(0.0, ExamAttemptRules.percentage(Double.NaN, 10.0), 0.0001)
    }

    @Test fun historyAggregatesScoresWithoutChangingAttempts() {
        val attempts = listOf(
            attempt("a", 40.0),
            attempt("b", 80.0),
            attempt("c", 60.0)
        )
        assertEquals(60.0, ExamAttemptRules.averageScore(attempts), 0.0001)
        assertEquals(80.0, ExamAttemptRules.bestScore(attempts), 0.0001)
        assertEquals(listOf("a", "b", "c"), attempts.map { it.id })
    }

    @Test fun medianHandlesOddAndEvenAttemptCounts() {
        assertEquals(60.0, ExamAttemptRules.medianScore(listOf(attempt("a", 40.0), attempt("b", 60.0), attempt("c", 80.0))), 0.0001)
        assertEquals(50.0, ExamAttemptRules.medianScore(listOf(attempt("a", 40.0), attempt("b", 60.0))), 0.0001)
        assertEquals(0.0, ExamAttemptRules.medianScore(emptyList()), 0.0001)
    }

    @Test fun trendComparesNewestWindowWithPreviousWindow() {
        val attempts = listOf(90.0, 80.0, 70.0, 60.0, 50.0, 50.0, 40.0, 30.0, 20.0, 10.0)
            .mapIndexed { index, score -> attempt("$index", score) }
        assertEquals(40.0, ExamAttemptRules.recentTrend(attempts, 5)!!, 0.0001)
        assertEquals(null, ExamAttemptRules.recentTrend(attempts.take(5), 5))
    }

    @Test fun nonFiniteScoresAreSafelyTreatedAsZero() {
        val attempts = listOf(attempt("nan", Double.NaN), attempt("infinity", Double.POSITIVE_INFINITY))
        assertEquals(0.0, ExamAttemptRules.averageScore(attempts), 0.0001)
        assertEquals(0.0, ExamAttemptRules.bestScore(attempts), 0.0001)
        assertEquals(0.0, ExamAttemptRules.medianScore(attempts), 0.0001)
        assertEquals(2, ExamAttemptRules.scoreBandCounts(attempts).first { it.first == "0–39%" }.second)
    }

    @Test fun scoreBandsCoverAllAttemptsExactlyOnce() {
        val attempts = listOf(0.0, 39.9, 40.0, 59.9, 60.0, 79.9, 80.0, 100.0).mapIndexed { i, score -> attempt("$i", score) }
        val bands = ExamAttemptRules.scoreBandCounts(attempts)
        assertEquals(listOf(2, 2, 2, 2), bands.map { it.second })
        assertEquals(attempts.size, bands.sumOf { it.second })
    }

    private fun attempt(id: String, score: Double) = ExamAttemptEntity(
        id = id, startedAt = 1L, completedAt = 2L, durationSeconds = 60,
        questionCount = 2, answeredCount = 2, markedCount = 0,
        earnedPoints = score / 10, possiblePoints = 10.0, scorePercent = score,
        itemsJson = "[]"
    )
}
