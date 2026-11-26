package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class NumericalTemplateAttemptRulesTest {
    private val template = NumericalTemplateEngine.Template(
        id = "linear-v1", prompt = "Calculate {x} + 2", explanation = "Add 2 to {x}",
        variables = listOf(NumericalTemplateEngine.Variable("x", 3.0, 3.0)),
        answerExpression = "x + 2", answerUnit = "m", absoluteTolerance = 0.01,
        authoringStatus = "VERIFIED"
    )

    @Test fun sameAttemptAndQuestionReconstructSameVariant() {
        val first = NumericalTemplateAttemptRules.generate(template, "attempt-123", "question-a").getOrThrow()
        val resumed = NumericalTemplateAttemptRules.generate(template, "attempt-123", "question-a").getOrThrow()
        assertEquals(first.seed, resumed.seed)
        assertEquals(first.values, resumed.values)
        assertEquals(first.answer, resumed.answer, 0.0)
        assertEquals(first.prompt, resumed.prompt)
    }

    @Test fun differentQuestionGetsIndependentSeed() {
        assertNotEquals(
            NumericalTemplateAttemptRules.seedFor("attempt-123", "question-a"),
            NumericalTemplateAttemptRules.seedFor("attempt-123", "question-b")
        )
    }

    @Test fun gradingUsesAttemptVariantAndUnits() {
        assertTrue(NumericalTemplateAttemptRules.isCorrect(template, "attempt-123", "question-a", "5.0 m"))
        assertFalse(NumericalTemplateAttemptRules.isCorrect(template, "attempt-123", "question-a", "5.0 s"))
        assertTrue(NumericalTemplateAttemptRules.isCorrect(template, "attempt-123", "question-a", "500 cm"))
        assertFalse(NumericalTemplateAttemptRules.isCorrect(template, "attempt-123", "question-a", "500 cm", "s"))
    }
}
