package com.studyforge.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewTrendRulesTest {
    private val today = LocalDate.parse("2026-11-25")

    private fun log(day: String, correct: Boolean? = null, id: String = day) = ReviewLogEntity(
        id = id, questionId = "q", reviewedAt = Instant.parse("${day}T12:00:00Z").toEpochMilli(),
        rating = "Good", wasCorrect = correct
    )

    @Test fun returnsGapFilledFourteenDayTimelineAndScoredAccuracy() {
        val result = ReviewTrendRules.summarize(
            listOf(log("2026-11-25", true, "a"), log("2026-11-25", false, "b"), log("2026-11-23", null, "c")),
            today = today, zoneId = ZoneOffset.UTC
        )
        assertEquals(14, result.days.size)
        assertEquals(LocalDate.parse("2026-11-12"), result.days.first().date)
        assertEquals(2, result.days.last().totalReviews)
        assertEquals(2, result.days.last().scoredReviews)
        assertEquals(50, result.days.last().accuracyPercent)
        assertEquals(0, result.days[10].totalReviews)
        assertEquals(2, result.activeDays)
    }

    @Test fun currentStreakIncludesYesterdayWhenTodayHasNoReviews() {
        val result = ReviewTrendRules.summarize(
            listOf(log("2026-11-23"), log("2026-11-24")),
            today = today, zoneId = ZoneOffset.UTC
        )
        assertEquals(2, result.currentStreakDays)
    }

    @Test fun currentStreakStartsTodayAndStopsAtFirstGap() {
        val result = ReviewTrendRules.summarize(
            listOf(log("2026-11-22"), log("2026-11-24"), log("2026-11-25")),
            today = today, zoneId = ZoneOffset.UTC
        )
        assertEquals(2, result.currentStreakDays)
    }

    @Test fun unscoredReviewsCountAsActivityButHaveNoAccuracy() {
        val result = ReviewTrendRules.summarize(
            listOf(log("2026-11-25", null)), today = today, zoneId = ZoneOffset.UTC
        )
        assertNull(result.days.last().accuracyPercent)
        assertEquals(1, result.currentStreakDays)
    }

    @Test fun timestampsAreGroupedUsingTheRequestedLocalTimeZone() {
        val justAfterUtcMidnight = Instant.parse("2026-11-25T00:30:00Z").toEpochMilli()
        val entry = ReviewLogEntity("tz", "q", justAfterUtcMidnight, "Good", true)
        val result = ReviewTrendRules.summarize(
            listOf(entry), today = LocalDate.parse("2026-11-24"), zoneId = ZoneOffset.ofHours(-1)
        )
        assertEquals(1, result.days.last().totalReviews)
    }
}
