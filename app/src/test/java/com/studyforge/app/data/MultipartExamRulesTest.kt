package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultipartExamRulesTest {
    private fun part(id: String, answer: String, points: Double) = QuestionPartEntity(
        id = id, questionId = "q", position = 0, prompt = "Prompt", answer = answer, points = points
    )

    @Test fun weightedScoreAwardsOnlyCorrectPartPoints() {
        val parts = listOf(part("a", "mitochondria", 2.0), part("b", "ATP", 3.0))
        val score = MultipartExamRules.score(parts, mapOf("a" to "Mitochondria", "b" to "ADP"))
        assertEquals(2.0, score.earned, 0.0001)
        assertEquals(5.0, score.possible, 0.0001)
        assertEquals(40.0, score.percentage, 0.0001)
        assertEquals(2, score.answeredParts)
        assertEquals(1, score.correctParts)
    }

    @Test fun whitespaceAndCaseAreNormalized() {
        assertTrue(MultipartExamRules.isCorrect(part("a", "ATP is produced", 1.0), "  ATP   IS produced "))
    }

    @Test fun blankAnswersDoNotEarnPoints() {
        val score = MultipartExamRules.score(listOf(part("a", "42", 4.0)), emptyMap())
        assertEquals(0.0, score.earned, 0.0001)
        assertEquals(0, score.answeredParts)
    }

    @Test fun wrongAnswerIsNotCorrect() {
        assertFalse(MultipartExamRules.isCorrect(part("a", "42", 1.0), "41"))
    }

    @Test fun numericAbsoluteToleranceAcceptsNearbyValue() {
        val numeric = part("n", "9.8", 2.0).copy(absoluteTolerance = 0.05)
        assertTrue(MultipartExamRules.isCorrect(numeric, "9.84"))
        assertFalse(MultipartExamRules.isCorrect(numeric, "9.9"))
    }

    @Test fun numericRelativeToleranceScalesWithExpectedValue() {
        val numeric = part("n", "100", 1.0).copy(relativeTolerance = 0.02)
        assertTrue(MultipartExamRules.isCorrect(numeric, "101.5"))
        assertFalse(MultipartExamRules.isCorrect(numeric, "103"))
    }

    @Test fun acceptedAlternativeAnswersAreRecognized() {
        val p = part("a", "mitochondrion", 1.0).copy(acceptedAnswers = "powerhouse of the cell\nmitochondria")
        assertTrue(MultipartExamRules.isCorrect(p, "Powerhouse of the cell"))
    }

    @Test fun invalidPointPartsAreExcludedFromTotal() {
        val score = MultipartExamRules.score(listOf(part("a", "x", 2.0), part("b", "y", -1.0)), mapOf("a" to "x", "b" to "y"))
        assertEquals(2.0, score.possible, 0.0001)
        assertEquals(2.0, score.earned, 0.0001)
    }
}
