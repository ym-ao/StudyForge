package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class ClozeRulesTest {
    @Test fun masksAndExtractsMultipleClozeDeletions() {
        val source = "{{c1::Mitochondria}} produce {{c2::ATP::energy currency}}."
        assertEquals(listOf("Mitochondria", "ATP"), ClozeRules.answers(source))
        assertEquals("[ … ] produce [ … ].", ClozeRules.mask(source))
    }
    @Test fun leavesTextWithoutClozeUntouched() {
        assertFalse(ClozeRules.hasCloze("Ordinary text"))
        assertEquals("Ordinary text", ClozeRules.mask("Ordinary text"))
    }
}
