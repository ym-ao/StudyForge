package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Golden invariants from the published FSRS-6 model: R(S,S)=0.9 and I(S,0.9)=S before rounding. */
class Fsrs6ReferenceInvariantTest {
    @Test fun retrievabilityAtStabilityIsNinetyPercent() {
        listOf(0.5, 1.0, 2.3065, 10.0, 100.0).forEach { stability ->
            assertEquals(0.9, ReviewScheduler.calculateRetrievability(stability, stability), 1e-9)
        }
    }
    @Test fun goodInitialStabilityMatchesPublishedDefault() {
        val card = QuestionEntity(id="q", prompt="p", answer="a")
        val updated = ReviewScheduler.apply(card, ReviewScheduler.GOOD, 1_000_000L)
        assertEquals(2.3065, updated.fsrsStability, 1e-9)
    }
    @Test fun ninetyPercentIntervalTracksStabilityWithinWholeDayRounding() {
        listOf(1.0, 2.3065, 10.0, 100.0).forEach { stability ->
            val interval = ReviewScheduler.intervalFor(stability, 0.90, 36500)
            assertTrue(kotlin.math.abs(interval - stability) <= 0.51)
        }
    }
}
