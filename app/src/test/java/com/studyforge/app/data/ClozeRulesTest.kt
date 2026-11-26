package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class ClozeRulesTest {
    @Test fun masksAndExtractsMultipleClozeDeletions() {
        val source = "{{c1::Mitochondria}} produce {{c2::ATP::energy currency}}."
        assertEquals(listOf("Mitochondria", "ATP"), ClozeRules.answers(source))
        assertEquals("[ … ] produce [ … ].", ClozeRules.mask(source))
        assertEquals("[ … ] produce ATP.", ClozeRules.mask(source, 1))
        assertEquals("Mitochondria produce [ … ].", ClozeRules.mask(source, 2))
    }

    @Test fun leavesTextWithoutClozeUntouched() {
        assertFalse(ClozeRules.hasCloze("Ordinary text"))
        assertEquals("Ordinary text", ClozeRules.mask("Ordinary text"))
    }

    @Test fun parsesHintsAndReturnsDistinctIndicesInAppearanceOrder() {
        val source = "A {{c2::beta::second hint}}, then {{c1::alpha}}, then {{c2::gamma}}."
        val deletions = ClozeRules.parse(source)
        assertEquals(listOf(2, 1, 2), deletions.map { it.index })
        assertEquals(listOf("beta", "alpha", "gamma"), deletions.map { it.answer })
        assertEquals("second hint", deletions.first().hint)
        assertEquals(listOf(2, 1), ClozeRules.indices(source))
    }

    @Test fun supportsMultilineAnswersAndCaseInsensitiveMarker() {
        val source = "Before {{C3::line one\nline two::hint}} after"
        assertEquals(listOf("line one\nline two"), ClozeRules.answers(source))
        assertEquals("Before [ … ] after", ClozeRules.mask(source))
    }

    @Test fun malformedOrEmptyMarkersArePreservedAndDoNotCountAsCloze() {
        val source = "{{c0::zero}} {{c1::}} {{c2::unfinished"
        assertFalse(ClozeRules.hasCloze(source))
        assertEquals(source, ClozeRules.mask(source))
    }

    @Test fun longInputIsRejectedSafely() {
        val source = "x".repeat(100_001) + " {{c1::answer}}"
        assertFalse(ClozeRules.hasCloze(source))
        assertEquals(source, ClozeRules.mask(source))
    }
}
