package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionTypeAnalyticsTest {
    @Test fun accuracyUsesOnlyScoredReviewsAndGroupsByCurrentQuestionType() {
        val questions = listOf(QuestionEntity("mcq", "P", "A", type = "MCQ"), QuestionEntity("card", "P", "A", type = "Flashcard"))
        val logs = listOf(
            ReviewLogEntity("1", "mcq", 1, "Good", true),
            ReviewLogEntity("2", "mcq", 2, "Again", false),
            ReviewLogEntity("3", "mcq", 3, "Good", null),
            ReviewLogEntity("4", "card", 4, "Good", true),
            ReviewLogEntity("5", "gone", 5, "Again", false)
        )
        val result = QuestionTypeAnalytics.summarize(questions, logs)
        assertEquals(3, result.size)
        assertEquals("MCQ", result[0].type)
        assertEquals(2, result[0].scored)
        assertEquals(50, result[0].accuracyPercent)
        assertEquals("Flashcard", result[1].type)
        assertEquals(100, result[1].accuracyPercent)
        assertEquals("Deleted question", result[2].type)
    }

    @Test fun emptyHistoryProducesNoTypeSummaries() {
        assertEquals(emptyList<QuestionTypeAnalytics.Summary>(), QuestionTypeAnalytics.summarize(emptyList(), emptyList()))
    }
}
