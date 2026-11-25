package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class ManualScheduleRulesTest {
    @Test fun manualDueDateDoesNotChangeReviewHistoryCounters() {
        val source = QuestionEntity("q", "p", "a", reviewCount = 12, correctCount = 8, incorrectCount = 4, fsrsStability = 5.0)
        val updated = ManualScheduleRules.setDue(source, 123456L)
        assertEquals(123456L, updated.dueAt)
        assertEquals(12, updated.reviewCount)
        assertEquals(8, updated.correctCount)
        assertEquals(5.0, updated.fsrsStability, 0.0)
    }

    @Test fun resetClearsSchedulerButPreservesLifetimeStats() {
        val source = QuestionEntity("q", "p", "a", dueAt = 99L, reviewCount = 12, correctCount = 8, incorrectCount = 4, fsrsStability = 5.0, fsrsDifficulty = 6.0, learningState = "Learning", learningStepIndex = 2)
        val updated = ManualScheduleRules.reset(source)
        assertNull(updated.dueAt)
        assertEquals("New", updated.learningState)
        assertEquals(-1, updated.learningStepIndex)
        assertEquals(0.0, updated.fsrsStability, 0.0)
        assertEquals(12, updated.reviewCount)
        assertEquals(8, updated.correctCount)
        assertEquals(4, updated.incorrectCount)
    }
}
