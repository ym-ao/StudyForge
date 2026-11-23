package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class DerivationRulesTest {
    @Test fun parsesOrderedDerivationSteps() {
        val steps = DerivationRules.parse("Start|v=u+at|First equation\nSubstitute|v=0+2(3)|Insert values")
        assertEquals(2, steps.size)
        assertEquals("v=0+2(3)", steps[1].equation)
        assertNull(DerivationRules.validate(steps))
    }
    @Test fun rejectsMissingEquation() {
        assertNotNull(DerivationRules.validate(listOf(DerivationRules.Step("Step", "", ""))))
    }
    @Test fun rejectsEmptyDerivation() { assertNotNull(DerivationRules.validate(emptyList())) }
    @Test fun questionAuthoringRejectsInvalidDerivationBlock() {
        val block = QuestionContentBlockEntity("b", "q", 0, "DERIVATION", "bad line")
        assertNotNull(QuestionAuthoringRules.validateBlocks(listOf(block)))
    }
}
