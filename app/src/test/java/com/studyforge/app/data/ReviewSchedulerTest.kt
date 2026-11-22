package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {
    private fun newQuestion(state: String = "New") = QuestionEntity(
        id = "q1",
        prompt = "Question",
        answer = "Answer",
        learningState = state
    )

    @Test
    fun goodSchedulesFirstReviewForOneDay() {
        val now = 1_000_000L
        val result = ReviewScheduler.apply(newQuestion(), ReviewScheduler.GOOD, now)

        assertEquals(1.0, result.intervalDays, 0.0001)
        assertEquals(1, result.repetitions)
        assertEquals("Review", result.learningState)
        assertEquals(now + 24L * 60L * 60L * 1000L, result.dueAt)
    }

    @Test
    fun easySchedulesNewQuestionForFourDays() {
        val now = 1_000_000L
        val result = ReviewScheduler.apply(newQuestion(), ReviewScheduler.EASY, now)

        assertEquals(4.0, result.intervalDays, 0.0001)
        assertTrue(result.easeFactor > 2.5)
        assertEquals(now + 4L * 24L * 60L * 60L * 1000L, result.dueAt)
    }

    @Test
    fun againUsesTenMinuteRelearningStep() {
        val now = 1_000_000L
        val previous = newQuestion("Review").copy(repetitions = 3, intervalDays = 12.0)
        val result = ReviewScheduler.apply(previous, ReviewScheduler.AGAIN, now)

        assertEquals(10.0 / (24.0 * 60.0), result.intervalDays, 0.000001)
        assertEquals(now + 10L * 60L * 1000L, result.dueAt)
        assertEquals("Relearning", result.learningState)
        assertEquals(1, result.lapseCount)
        assertEquals(0, result.repetitions)
    }

    @Test
    fun consecutiveGoodReviewsIncreaseInterval() {
        val first = ReviewScheduler.apply(newQuestion(), ReviewScheduler.GOOD, 0L)
        val second = ReviewScheduler.apply(first, ReviewScheduler.GOOD, 0L)

        assertEquals(3.0, second.intervalDays, 0.0001)
        assertEquals(2, second.repetitions)
    }
}
