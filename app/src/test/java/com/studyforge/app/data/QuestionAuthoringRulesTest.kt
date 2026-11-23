package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class QuestionAuthoringRulesTest {
    @Test fun rejectsBlankPartPromptAndAnswer() {
        val part = QuestionPartEntity("p", "q", 0, " ", "answer")
        assertTrue(QuestionAuthoringRules.validateParts(listOf(part))!!.contains("prompt"))
        val second = part.copy(prompt = "prompt", answer = "")
        assertTrue(QuestionAuthoringRules.validateParts(listOf(second))!!.contains("answer"))
    }

    @Test fun rejectsNonPositiveAndNonFinitePoints() {
        assertNotNull(QuestionAuthoringRules.validateParts(listOf(QuestionPartEntity("p", "q", 0, "p", "a", points = 0.0))))
        assertNotNull(QuestionAuthoringRules.validateParts(listOf(QuestionPartEntity("p", "q", 0, "p", "a", points = Double.NaN))))
    }

    @Test fun normalizesBlockOrderAndPreservesUnknownKinds() {
        val blocks = listOf(
            QuestionContentBlockEntity("b1", "old", 9, "future-kind", "one", mediaBase64 = "YWJj", mediaMimeType = "image/png", altText = "sample"),
            QuestionContentBlockEntity("b2", "old", 3, "latex", "x")
        )
        val normalized = QuestionAuthoringRules.normalizeBlocks("q", blocks)
        assertEquals(listOf(0, 1), normalized.map { it.position })
        assertEquals(listOf("FUTURE-KIND", "LATEX"), normalized.map { it.kind })
        assertTrue(normalized.all { it.questionId == "q" })
        assertEquals("YWJj", normalized.first().mediaBase64)
        assertEquals("image/png", normalized.first().mediaMimeType)
    }

    @Test fun normalizesMultipartOwnershipAndOrder() {
        val parts = listOf(
            QuestionPartEntity("p1", "old", 4, " first ", " answer ", points = 2.0),
            QuestionPartEntity("p2", "old", 8, "second", "answer", points = 1.0)
        )
        val normalized = QuestionAuthoringRules.normalizeParts("q", parts)
        assertEquals(listOf(0, 1), normalized.map { it.position })
        assertTrue(normalized.all { it.questionId == "q" })
        assertEquals("first", normalized.first().prompt)
    }
}
