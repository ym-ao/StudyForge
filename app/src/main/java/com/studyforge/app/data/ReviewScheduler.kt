package com.studyforge.app.data

import androidx.room.withTransaction
import java.util.UUID
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** FSRS-6 scheduler using the published 21 default parameters. All calculations are local and deterministic. */
object ReviewScheduler {
    const val AGAIN = "Again"
    const val HARD = "Hard"
    const val GOOD = "Good"
    const val EASY = "Easy"

    private const val DAY_MS = 86_400_000.0
    private val weights = doubleArrayOf(
        0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194,
        0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614,
        0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912,
        0.0658, 0.1542
    )

    fun apply(
        question: QuestionEntity,
        rating: String,
        reviewedAt: Long,
        settings: StudySettingsEntity = StudySettingsEntity.DEFAULT
    ): QuestionEntity {
        val grade = when (rating) {
            AGAIN -> 1
            HARD -> 2
            GOOD -> 3
            EASY -> 4
            else -> error("Unknown review rating: $rating")
        }
        val priorState = question.learningState
        val isNew = priorState == "New" && question.fsrsStability <= 0.0 && question.intervalDays <= 0.0
        val wasLearning = priorState == "Learning"
        val wasRelearning = priorState == "Relearning"
        val wasInReview = priorState == "Review"
        val oldStability = when {
            question.fsrsStability > 0.0 -> question.fsrsStability
            question.intervalDays > 0.0 -> question.intervalDays.coerceAtLeast(0.1)
            else -> 0.0
        }
        val oldDifficulty = when {
            question.fsrsDifficulty > 0.0 -> question.fsrsDifficulty
            oldStability > 0.0 -> (10.0 - ((question.easeFactor.coerceIn(1.3, 3.0) - 1.3) / 1.7) * 9.0).coerceIn(1.0, 10.0)
            else -> 0.0
        }

        val stability: Double
        val difficulty: Double
        if (isNew) {
            stability = weights[grade - 1]
            difficulty = initialDifficulty(grade)
        } else {
            val currentS = oldStability.coerceAtLeast(0.1)
            val currentD = oldDifficulty.coerceIn(1.0, 10.0)
            val elapsedDays = question.lastReviewedAt?.let { elapsedStudyDays(it, reviewedAt) } ?: 0.0
            val retrievability = retrievability(elapsedDays, currentS)
            difficulty = nextDifficulty(currentD, grade)
            stability = when {
                elapsedDays < 1.0 -> nextShortTermStability(currentS, grade)
                grade == 1 -> nextForgetStability(difficulty, currentS, retrievability)
                else -> nextRecallStability(difficulty, currentS, retrievability, grade)
            }
        }

        val learningSteps = parseSteps(settings.learningStepsMinutesCsv).ifEmpty { listOf(10) }
        val relearningSteps = parseSteps(settings.relearningStepsMinutesCsv).ifEmpty { listOf(10) }
        var nextState = "Review"
        var nextStepIndex = -1
        var nextIntervalDays: Double

        when {
            grade == 1 -> {
                // A new card enters Learning; a graduated card enters Relearning.
                // Repeated Again ratings restart the appropriate sequence at step zero.
                val useRelearning = !isNew && !wasLearning && !wasRelearning
                val steps = if (useRelearning || wasRelearning) relearningSteps else learningSteps
                nextState = if (useRelearning || wasRelearning) "Relearning" else "Learning"
                nextStepIndex = 0
                nextIntervalDays = steps.first() / 1440.0
            }
            wasLearning || wasRelearning -> {
                val steps = if (wasRelearning) relearningSteps else learningSteps
                val currentIndex = question.learningStepIndex.coerceIn(0, steps.lastIndex)
                when (grade) {
                    2 -> {
                        // Hard repeats the current step rather than silently skipping it.
                        nextState = priorState
                        nextStepIndex = currentIndex
                        nextIntervalDays = steps[currentIndex] / 1440.0
                    }
                    3 -> {
                        if (currentIndex + 1 < steps.size) {
                            nextState = priorState
                            nextStepIndex = currentIndex + 1
                            nextIntervalDays = steps[nextStepIndex] / 1440.0
                        } else {
                            nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                        }
                    }
                    else -> {
                        // Easy graduates immediately from a learning/relearning sequence.
                        nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                    }
                }
            }
            else -> {
                // Good/Easy (and Hard on a new card) graduate directly to FSRS review.
                nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
            }
        }

        val due = reviewedAt + (nextIntervalDays * DAY_MS).roundToLong().coerceAtLeast(1L)
        val easeCompatibility = (3.0 - ((difficulty - 1.0) / 9.0) * 1.7).coerceIn(1.3, 3.0)

        return question.copy(
            reviewCount = question.reviewCount + 1,
            lastReviewedAt = reviewedAt,
            dueAt = due,
            intervalDays = nextIntervalDays,
            easeFactor = easeCompatibility,
            repetitions = if (grade == 1) 0 else question.repetitions + 1,
            lapseCount = question.lapseCount + if (grade == 1 && wasInReview) 1 else 0,
            learningState = nextState,
            learningStepIndex = nextStepIndex,
            fsrsStability = stability,
            fsrsDifficulty = difficulty
        )
    }

    /**
     * FSRS-6 uses whole study-day differences. The local calendar day is used so
     * reviews either side of midnight count as a new study day, not as a fraction
     * of 24 elapsed hours. Pass a fixed ZoneId in tests for deterministic results.
     */
    fun elapsedStudyDays(lastReviewedAt: Long, reviewedAt: Long, zoneId: ZoneId = ZoneId.systemDefault()): Double {
        val previousDay = Instant.ofEpochMilli(lastReviewedAt).atZone(zoneId).toLocalDate()
        val currentDay = Instant.ofEpochMilli(reviewedAt).atZone(zoneId).toLocalDate()
        return ChronoUnit.DAYS.between(previousDay, currentDay).coerceAtLeast(0).toDouble()
    }

    /** Reads configured learning/relearning delays in minutes, preserving order and repeated steps. */
    fun parseSteps(csv: String): List<Int> = csv.split(',', ';', ' ')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..1440 }

    fun intervalFor(stability: Double, retention: Double, maxIntervalDays: Int): Double {
        val safeRetention = retention.coerceIn(0.70, 0.99)
        val safeMax = maxIntervalDays.coerceIn(1, 36500)
        val decay = weights[20]
        val factor = 0.9.pow(-1.0 / decay) - 1.0
        val days = stability.coerceAtLeast(0.1) / factor * (safeRetention.pow(-1.0 / decay) - 1.0)
        return days.roundToInt().coerceIn(1, safeMax).toDouble()
    }

    fun calculateRetrievability(elapsedDays: Double, stability: Double): Double = retrievability(elapsedDays, stability)

    private fun retrievability(elapsedDays: Double, stability: Double): Double {
        val decay = weights[20]
        val factor = 0.9.pow(-1.0 / decay) - 1.0
        return (1.0 + factor * elapsedDays.coerceAtLeast(0.0) / stability.coerceAtLeast(0.1)).pow(-decay)
    }

    private fun initialDifficulty(grade: Int): Double =
        (weights[4] - exp(weights[5] * (grade - 1)) + 1.0).coerceIn(1.0, 10.0)

    private fun nextDifficulty(current: Double, grade: Int): Double {
        val delta = -weights[6] * (grade - 3)
        val damped = delta * ((10.0 - current) / 9.0)
        val raw = current + damped
        return (weights[7] * initialDifficulty(4) + (1.0 - weights[7]) * raw).coerceIn(1.0, 10.0)
    }

    private fun nextShortTermStability(stability: Double, grade: Int): Double {
        var increase = exp(weights[17] * (grade - 3 + weights[18])) * stability.pow(-weights[19])
        if (grade >= 2) increase = max(increase, 1.0)
        return (stability * increase).coerceAtLeast(0.1)
    }

    private fun nextForgetStability(difficulty: Double, stability: Double, retrievability: Double): Double {
        val minimum = stability / exp(weights[17] * weights[18])
        val calculated = weights[11] * difficulty.pow(-weights[12]) *
            ((stability + 1.0).pow(weights[13]) - 1.0) *
            exp((1.0 - retrievability) * weights[14])
        return minOf(calculated, minimum).coerceAtLeast(0.1)
    }

    private fun nextRecallStability(difficulty: Double, stability: Double, retrievability: Double, grade: Int): Double {
        val hardPenalty = if (grade == 2) weights[15] else 1.0
        val easyBonus = if (grade == 4) weights[16] else 1.0
        val growth = exp(weights[8]) * (11.0 - difficulty) * stability.pow(-weights[9]) *
            (exp((1.0 - retrievability) * weights[10]) - 1.0) * hardPenalty * easyBonus
        return (stability * (1.0 + growth)).coerceAtLeast(0.1)
    }
}

suspend fun StudyDatabase.recordGradedReview(
    questionId: String,
    rating: String,
    wasCorrect: Boolean?,
    mistakeCategory: String = "",
    reviewMode: String = "Review",
    reviewedAt: Long = System.currentTimeMillis(),
    settings: StudySettingsEntity = StudySettingsEntity.DEFAULT
) {
    withTransaction {
        val dao = dao()
        val current = dao.getQuestion(questionId) ?: return@withTransaction
        val updated = ReviewScheduler.apply(current, rating, reviewedAt, settings)
        dao.updateReviewSchedule(
            id = updated.id,
            reviewCount = updated.reviewCount,
            reviewedAt = reviewedAt,
            dueAt = updated.dueAt ?: reviewedAt,
            intervalDays = updated.intervalDays,
            easeFactor = updated.easeFactor,
            repetitions = updated.repetitions,
            lapseCount = updated.lapseCount,
            learningState = updated.learningState,
            learningStepIndex = updated.learningStepIndex,
            correctCount = current.correctCount + if (wasCorrect == true) 1 else 0,
            incorrectCount = current.incorrectCount + if (wasCorrect == false) 1 else 0,
            fsrsStability = updated.fsrsStability,
            fsrsDifficulty = updated.fsrsDifficulty
        )
        dao.insertReviewLog(
            ReviewLogEntity(
                id = UUID.randomUUID().toString(),
                questionId = questionId,
                reviewedAt = reviewedAt,
                rating = rating,
                wasCorrect = wasCorrect,
                mistakeCategory = mistakeCategory,
                reviewMode = reviewMode,
                scheduledDueAt = updated.dueAt
            )
        )
    }
}
