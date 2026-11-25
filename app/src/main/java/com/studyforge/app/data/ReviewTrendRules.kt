package com.studyforge.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One calendar day's review activity, derived only from persisted review logs. */
data class DailyReviewSummary(
    val date: LocalDate,
    val totalReviews: Int,
    val scoredReviews: Int,
    val correctReviews: Int
) {
    val accuracyPercent: Int?
        get() = if (scoredReviews == 0) null else (correctReviews * 100 / scoredReviews)
}

data class ReviewTrendSummary(
    val days: List<DailyReviewSummary>,
    val activeDays: Int,
    val currentStreakDays: Int
)

object ReviewTrendRules {
    /**
     * Builds a chronological, gap-filled calendar trend ending on [today].
     * If today has no reviews, the current streak may continue from yesterday.
     */
    fun summarize(
        logs: List<ReviewLogEntity>,
        today: LocalDate = LocalDate.now(),
        dayCount: Int = 14,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ReviewTrendSummary {
        require(dayCount in 1..366) { "dayCount must be between 1 and 366" }
        val start = today.minusDays((dayCount - 1).toLong())
        val grouped = logs.groupBy { Instant.ofEpochMilli(it.reviewedAt).atZone(zoneId).toLocalDate() }
        val days = (0 until dayCount).map { offset ->
            val date = start.plusDays(offset.toLong())
            val dayLogs = grouped[date].orEmpty()
            val scored = dayLogs.filter { it.wasCorrect != null }
            DailyReviewSummary(
                date = date,
                totalReviews = dayLogs.size,
                scoredReviews = scored.size,
                correctReviews = scored.count { it.wasCorrect == true }
            )
        }
        val activeDates = days.filter { it.totalReviews > 0 }.map { it.date }.toSet()
        var streak = 0
        var cursor = if (today in activeDates) today else today.minusDays(1)
        while (cursor >= start && cursor in activeDates) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return ReviewTrendSummary(days, activeDates.size, streak)
    }
}
