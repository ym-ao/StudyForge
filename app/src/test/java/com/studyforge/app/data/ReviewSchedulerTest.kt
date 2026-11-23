package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class ReviewSchedulerTest {
    private fun newQuestion(state: String = "New") = QuestionEntity(
        id = "q1",
        prompt = "Question",
        answer = "Answer",
        learningState = state
    )

    @Test
    fun newGoodCardUsesFsrsInitialStabilityAndDifficulty() {
        val result = ReviewScheduler.apply(newQuestion(), ReviewScheduler.GOOD, 1_000_000L)
        assertEquals(2.3065, result.fsrsStability, 0.0001)
        assertTrue(result.fsrsDifficulty in 1.0..10.0)
        assertEquals("Review", result.learningState)
        assertEquals(2.0, result.intervalDays, 0.0001)
        assertEquals(1_000_000L + 2L * 86_400_000L, result.dueAt)
    }

    @Test
    fun newEasyCardStartsWithHigherStabilityThanGood() {
        val good = ReviewScheduler.apply(newQuestion(), ReviewScheduler.GOOD, 1_000_000L)
        val easy = ReviewScheduler.apply(newQuestion(), ReviewScheduler.EASY, 1_000_000L)
        assertEquals(8.2956, easy.fsrsStability, 0.0001)
        assertTrue(easy.fsrsStability > good.fsrsStability)
        assertTrue(easy.fsrsDifficulty < good.fsrsDifficulty)
    }

    @Test
    fun againUsesConfiguredFirstLearningStep() {
        val now = 1_000_000L
        val settings = StudySettingsEntity(learningStepsMinutesCsv = "2,10")
        val result = ReviewScheduler.apply(newQuestion(), ReviewScheduler.AGAIN, now, settings)
        assertEquals(2.0 / 1440.0, result.intervalDays, 0.000001)
        assertEquals(now + 2L * 60L * 1000L, result.dueAt)
        assertEquals("Learning", result.learningState)
        assertEquals(0, result.learningStepIndex)
        assertEquals(0, result.lapseCount)
    }

    @Test
    fun learningStepsAdvanceAndGraduateInConfiguredOrder() {
        val settings = StudySettingsEntity(learningStepsMinutesCsv = "2,10")
        val start = 1_000_000L
        val first = ReviewScheduler.apply(newQuestion(), ReviewScheduler.AGAIN, start, settings)
        assertEquals("Learning", first.learningState)
        assertEquals(0, first.learningStepIndex)
        assertEquals(start + 2L * 60L * 1000L, first.dueAt)

        val second = ReviewScheduler.apply(first, ReviewScheduler.GOOD, first.dueAt!!, settings)
        assertEquals("Learning", second.learningState)
        assertEquals(1, second.learningStepIndex)
        assertEquals(second.lastReviewedAt!! + 10L * 60L * 1000L, second.dueAt)

        val graduated = ReviewScheduler.apply(second, ReviewScheduler.GOOD, second.dueAt!!, settings)
        assertEquals("Review", graduated.learningState)
        assertEquals(-1, graduated.learningStepIndex)
        assertTrue(graduated.intervalDays >= 1.0)
    }

    @Test
    fun hardRepeatsCurrentLearningStepAndAgainRestartsIt() {
        val settings = StudySettingsEntity(learningStepsMinutesCsv = "2,10")
        val first = ReviewScheduler.apply(newQuestion(), ReviewScheduler.AGAIN, 1_000_000L, settings)
        val hard = ReviewScheduler.apply(first, ReviewScheduler.HARD, first.dueAt!!, settings)
        assertEquals(0, hard.learningStepIndex)
        assertEquals("Learning", hard.learningState)
        assertEquals(hard.lastReviewedAt!! + 2L * 60L * 1000L, hard.dueAt)

        val advanced = ReviewScheduler.apply(hard, ReviewScheduler.GOOD, hard.dueAt!!, settings)
        assertEquals(1, advanced.learningStepIndex)
        val restarted = ReviewScheduler.apply(advanced, ReviewScheduler.AGAIN, advanced.dueAt!!, settings)
        assertEquals(0, restarted.learningStepIndex)
        assertEquals("Learning", restarted.learningState)
        assertEquals(2.0 / 1440.0, restarted.intervalDays, 0.000001)
    }

    @Test
    fun relearningStepsAdvanceAndGraduate() {
        val settings = StudySettingsEntity(relearningStepsMinutesCsv = "5,20")
        val reviewed = newQuestion("Review").copy(
            fsrsStability = 12.0,
            fsrsDifficulty = 5.0,
            intervalDays = 12.0,
            reviewCount = 4,
            lastReviewedAt = 900_000L
        )
        val first = ReviewScheduler.apply(reviewed, ReviewScheduler.AGAIN, 1_000_000L, settings)
        assertEquals("Relearning", first.learningState)
        assertEquals(0, first.learningStepIndex)
        assertEquals(first.lastReviewedAt!! + 5L * 60L * 1000L, first.dueAt)

        val second = ReviewScheduler.apply(first, ReviewScheduler.GOOD, first.dueAt!!, settings)
        assertEquals(1, second.learningStepIndex)
        assertEquals(second.lastReviewedAt!! + 20L * 60L * 1000L, second.dueAt)

        val graduated = ReviewScheduler.apply(second, ReviewScheduler.GOOD, second.dueAt!!, settings)
        assertEquals("Review", graduated.learningState)
        assertEquals(-1, graduated.learningStepIndex)
    }

    @Test
    fun repeatedConfiguredIntervalsArePreserved() {
        assertEquals(listOf(10, 10, 30), ReviewScheduler.parseSteps("10, 10, 30"))
    }

    @Test
    fun againOnPreviouslyReviewedCardUsesRelearningStepAndTracksLapse() {
        val now = 10_000_000L
        val previous = newQuestion("Review").copy(repetitions = 3, intervalDays = 12.0, lastReviewedAt = now - 4L * 86_400_000L)
        val result = ReviewScheduler.apply(previous, ReviewScheduler.AGAIN, now)
        assertEquals(10.0 / 1440.0, result.intervalDays, 0.000001)
        assertEquals(now + 10L * 60L * 1000L, result.dueAt)
        assertEquals("Relearning", result.learningState)
        assertEquals(1, result.lapseCount)
        assertEquals(0, result.repetitions)
        assertTrue(result.fsrsStability > 0.0)
        assertTrue(result.fsrsDifficulty in 1.0..10.0)
    }

    @Test
    fun higherDesiredRetentionProducesShorterTargetInterval() {
        val stability = 20.0
        val normal = ReviewScheduler.intervalFor(stability, 0.90, 36500)
        val higher = ReviewScheduler.intervalFor(stability, 0.95, 36500)
        assertTrue(higher < normal)
    }

    @Test
    fun intervalIsClampedToConfiguredMaximum() {
        assertEquals(30.0, ReviewScheduler.intervalFor(1000.0, 0.90, 30), 0.0001)
    }

    @Test
    fun fsrs6UsesWholeLocalStudyDaysRatherThanFractionalElapsedHours() {
        val sameDayStart = Instant.parse("2026-01-01T08:00:00Z").toEpochMilli()
        val sameDayEnd = Instant.parse("2026-01-01T23:50:00Z").toEpochMilli()
        val nextDay = Instant.parse("2026-01-02T00:05:00Z").toEpochMilli()
        assertEquals(0.0, ReviewScheduler.elapsedStudyDays(sameDayStart, sameDayEnd, ZoneOffset.UTC), 0.0)
        assertEquals(1.0, ReviewScheduler.elapsedStudyDays(sameDayEnd, nextDay, ZoneOffset.UTC), 0.0)
        assertEquals(0.0, ReviewScheduler.elapsedStudyDays(nextDay, sameDayEnd, ZoneOffset.UTC), 0.0)
    }

    @Test
    fun parseStepsPreservesConfiguredOrderAndIgnoresInvalidValues() {
        assertEquals(listOf(10, 1, 30), ReviewScheduler.parseSteps("10, 1, 0, -2, 30, 2000"))
    }

    @Test
    fun stabilityIsTheIntervalAtNinetyPercentRetrievability() {
        assertEquals(0.90, ReviewScheduler.calculateRetrievability(10.0, 10.0), 0.000001)
    }

    @Test
    fun retrievabilityFallsAsElapsedTimeIncreases() {
        val today = ReviewScheduler.calculateRetrievability(0.0, 10.0)
        val afterFiveDays = ReviewScheduler.calculateRetrievability(5.0, 10.0)
        assertTrue(today > afterFiveDays)
        assertTrue(afterFiveDays in 0.0..1.0)
    }
}
