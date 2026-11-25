package com.studyforge.app.data

/** Pure calculations shared by exam result persistence and progress analytics. */
object ExamAttemptRules {
    private fun safeScore(value: Double): Double = if (value.isFinite()) value.coerceIn(0.0, 100.0) else 0.0

    fun percentage(earnedPoints: Double, possiblePoints: Double): Double {
        if (!earnedPoints.isFinite() || !possiblePoints.isFinite() || possiblePoints <= 0.0) return 0.0
        return (earnedPoints.coerceAtLeast(0.0) / possiblePoints * 100.0).coerceIn(0.0, 100.0)
    }

    fun averageScore(attempts: List<ExamAttemptEntity>): Double =
        attempts.map { safeScore(it.scorePercent) }.average().takeIf { it.isFinite() } ?: 0.0

    fun bestScore(attempts: List<ExamAttemptEntity>): Double =
        attempts.maxOfOrNull { safeScore(it.scorePercent) } ?: 0.0

    fun medianScore(attempts: List<ExamAttemptEntity>): Double {
        val scores = attempts.map { safeScore(it.scorePercent) }.sorted()
        if (scores.isEmpty()) return 0.0
        val middle = scores.size / 2
        return if (scores.size % 2 == 1) scores[middle] else (scores[middle - 1] + scores[middle]) / 2.0
    }

    /** Difference between the newest N attempts and the preceding N attempts, in percentage points. */
    fun recentTrend(attemptsNewestFirst: List<ExamAttemptEntity>, windowSize: Int = 5): Double? {
        val size = windowSize.coerceAtLeast(1)
        if (attemptsNewestFirst.size < size * 2) return null
        val recent = attemptsNewestFirst.take(size).map { safeScore(it.scorePercent) }.average()
        val previous = attemptsNewestFirst.drop(size).take(size).map { safeScore(it.scorePercent) }.average()
        return recent - previous
    }

    fun scoreBandCounts(attempts: List<ExamAttemptEntity>): List<Pair<String, Int>> {
        val scores = attempts.map { safeScore(it.scorePercent) }
        return listOf(
            "0–39%" to scores.count { it < 40.0 },
            "40–59%" to scores.count { it >= 40.0 && it < 60.0 },
            "60–79%" to scores.count { it >= 60.0 && it < 80.0 },
            "80–100%" to scores.count { it >= 80.0 }
        )
    }
}
