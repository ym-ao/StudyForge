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
    @Test fun initialMemoryStatesMatchAllFourPublishedDefaultGrades() {
        data class InitialVector(val rating: String, val stability: Double, val difficulty: Double)
        val vectors = listOf(
            InitialVector(ReviewScheduler.AGAIN, 0.2172, 7.0114),
            InitialVector(ReviewScheduler.HARD, 1.1771, 6.243132948566265),
            InitialVector(ReviewScheduler.GOOD, 3.2602, 4.884631634813845),
            InitialVector(ReviewScheduler.EASY, 16.1507, 2.482438522375996)
        )
        vectors.forEach { vector ->
            val card = QuestionEntity(id="initial-${vector.rating}", prompt="p", answer="a")
            val updated = ReviewScheduler.apply(card, vector.rating, 1_000_000L)
            assertEquals("initial stability for ${vector.rating}", vector.stability, updated.fsrsStability, 1e-9)
            assertEquals("initial difficulty for ${vector.rating}", vector.difficulty, updated.fsrsDifficulty, 1e-9)
        }
    }

    @Test fun goodInitialStabilityMatchesPublishedDefault() {
        val card = QuestionEntity(id="q", prompt="p", answer="a")
        val updated = ReviewScheduler.apply(card, ReviewScheduler.GOOD, 1_000_000L)
        assertEquals(3.2602, updated.fsrsStability, 1e-9)
    }
    @Test fun ninetyPercentIntervalTracksStabilityWithinWholeDayRounding() {
        listOf(1.0, 2.3065, 10.0, 100.0).forEach { stability ->
            val interval = ReviewScheduler.intervalFor(stability, 0.90, 36500)
            assertTrue(kotlin.math.abs(interval - stability) <= 0.51)
        }
    }

    @Test fun sameDayGoodAndEasyUpdatesMatchFsrs6ShortTermEquation() {
        val dayStart = java.time.Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()
        val base = QuestionEntity(
            id = "same-day", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 3.2602, fsrsDifficulty = 5.0,
            intervalDays = 2.0, lastReviewedAt = dayStart + 60_000L
        )
        val good = ReviewScheduler.apply(base, ReviewScheduler.GOOD, dayStart + 120_000L)
        val easy = ReviewScheduler.apply(base, ReviewScheduler.EASY, dayStart + 120_000L)
        assertEquals(3.536243655619573, good.fsrsStability, 1e-6)
        assertEquals(7.513226867074781, easy.fsrsStability, 1e-6)
    }

    @Test fun sameDayHardUsesPinnedV600ShortTermStabilityFormula() {
        val dayStart = java.time.Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()
        val base = QuestionEntity(
            id = "same-day-hard", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 3.2602, fsrsDifficulty = 5.0,
            intervalDays = 2.0, lastReviewedAt = dayStart + 60_000L
        )
        val updated = ReviewScheduler.apply(base, ReviewScheduler.HARD, dayStart + 120_000L)
        assertEquals(1.6644005848819015, updated.fsrsStability, 1e-9)
        assertEquals("Review", updated.learningState)
    }

    @Test fun sameDayAgainUsesShortTermMemoryUpdateBeforeRelearningStep() {
        val dayStart = java.time.Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()
        val base = QuestionEntity(
            id = "same-day-again", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 3.2602, fsrsDifficulty = 5.0,
            intervalDays = 2.0, lastReviewedAt = dayStart + 60_000L
        )
        val updated = ReviewScheduler.apply(base, ReviewScheduler.AGAIN, dayStart + 120_000L)
        assertEquals(0.7833819093752047, updated.fsrsStability, 1e-6)
        assertEquals("Relearning", updated.learningState)
    }

    /**
     * FSRS-6 reference vectors for the long-term review path. Fixture inputs and
     * expected values follow the py-fsrs v6.0.0 scheduler equations (21 defaults),
     * with fuzzing disabled: S=10, D=5, elapsed=5 study days, retention=0.90.
     * The scheduler must use the pre-review difficulty for stability, then update D.
     */
    @Test fun longTermReviewTransitionsMatchFsrs6ReferenceVectors() {
        val lastReview = java.time.Instant.parse("2026-01-01T12:00:00Z").toEpochMilli()
        val reviewedAt = lastReview + 5L * 86_400_000L
        val base = QuestionEntity(
            id = "fsrs-reference", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 10.0, fsrsDifficulty = 5.0,
            intervalDays = 10.0, lastReviewedAt = lastReview
        )
        data class Vector(val rating: String, val stability: Double, val difficulty: Double)
        val vectors = listOf(
            Vector(ReviewScheduler.AGAIN, 1.964360818327855, 7.296110448026617),
            Vector(ReviewScheduler.HARD, 12.83146342966157, 6.1393696369155055),
            Vector(ReviewScheduler.GOOD, 22.92315577207471, 4.982628825804394),
            Vector(ReviewScheduler.EASY, 48.77463657853296, 3.8258880146932825)
        )
        vectors.forEach { vector ->
            val actual = ReviewScheduler.apply(base, vector.rating, reviewedAt)
            assertEquals("stability for ${vector.rating}", vector.stability, actual.fsrsStability, 1e-9)
            assertEquals("difficulty for ${vector.rating}", vector.difficulty, actual.fsrsDifficulty, 1e-9)
        }
    }

    @Test fun sameDayHardUsesThePinnedV600FormulaWithoutGoodEasyFloor() {
        val lastReview = java.time.Instant.parse("2026-01-01T00:01:00Z").toEpochMilli()
        val base = QuestionEntity(
            id = "same-day-hard-v600", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 3.2602, fsrsDifficulty = 5.0,
            intervalDays = 3.0, lastReviewedAt = lastReview
        )
        val updated = ReviewScheduler.apply(base, ReviewScheduler.HARD, lastReview + 60_000L)
        assertEquals(1.6644005848819015, updated.fsrsStability, 1e-9)
    }

    @Test fun pinnedPyFsrsV600MemoStateSequenceMatchesEveryTransition() {
        val base = java.time.Instant.parse("2022-11-29T12:30:00Z").toEpochMilli()
        val ratings = listOf(
            ReviewScheduler.AGAIN, ReviewScheduler.GOOD, ReviewScheduler.GOOD,
            ReviewScheduler.GOOD, ReviewScheduler.GOOD, ReviewScheduler.GOOD, ReviewScheduler.GOOD
        )
        val dayIncrements = listOf(0L, 0L, 1L, 3L, 8L, 21L, 0L)
        val expectedStability = listOf(
            0.2172, 0.347699608, 1.954239205, 6.929102112,
            19.454748644, 49.447237416, 49.447237416
        )
        val expectedDifficulty = listOf(
            7.0114, 6.980150166, 6.949115955, 6.918295881,
            6.887688465, 6.857292241, 6.827105750
        )
        val settings = StudySettingsEntity(learningStepsMinutesCsv = "1,10", relearningStepsMinutesCsv = "10")
        var question = QuestionEntity(id = "pinned-v600", prompt = "p", answer = "a")
        var at = base
        for (index in ratings.indices) {
            at += dayIncrements[index] * 86_400_000L
            question = ReviewScheduler.apply(question, ratings[index], at, settings)
            assertEquals("stability after review ${index + 1}", expectedStability[index], question.fsrsStability, 5e-9)
            assertEquals("difficulty after review ${index + 1}", expectedDifficulty[index], question.fsrsDifficulty, 5e-9)
        }
    }

    @Test fun fsrsStabilityUsesPublishedPointZeroZeroOneLowerBound() {
        val card = QuestionEntity(
            id = "minimum-stability", prompt = "p", answer = "a", learningState = "Review",
            fsrsStability = 0.001, fsrsDifficulty = 10.0,
            intervalDays = 0.001, lastReviewedAt = java.time.Instant.parse("2026-01-01T12:00:00Z").toEpochMilli()
        )
        val updated = ReviewScheduler.apply(card, ReviewScheduler.AGAIN,
            card.lastReviewedAt!! + 5L * 86_400_000L)
        assertTrue(updated.fsrsStability >= 0.001)
    }

}
