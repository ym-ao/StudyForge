package com.studyforge.app.data

import androidx.room.withTransaction
import java.util.UUID
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong

/** FSRS-6 scheduler using the published 21 default parameters. All calculations are local and deterministic. */
object ReviewScheduler {
    const val AGAIN = "Again"
    const val HARD = "Hard"
    const val GOOD = "Good"
    const val EASY = "Easy"

    private const val DAY_MS = 86_400_000.0
    // Exact DEFAULT_PARAMETERS from open-spaced-repetition/py-fsrs v6.0.0
    // commit 352c7a2838071447fb1c55d9fbbbc04fa637ee81.
    private val weights = doubleArrayOf(
        0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966,
        0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133,
        0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332,
        0.1437, 0.2
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
            question.intervalDays > 0.0 -> question.intervalDays.coerceAtLeast(0.001)
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
            val currentS = oldStability.coerceAtLeast(0.001)
            val currentD = oldDifficulty.coerceIn(1.0, 10.0)
            val elapsedDays = question.lastReviewedAt?.let { elapsedStudyDays(it, reviewedAt) } ?: 0.0
            val retrievability = retrievability(elapsedDays, currentS)
            // FSRS-6 computes the new stability from the pre-review difficulty.
            // Difficulty is updated separately from that same prior state.
            stability = when {
                elapsedDays < 1.0 -> nextShortTermStability(currentS, grade)
                grade == 1 -> nextForgetStability(currentD, currentS, retrievability)
                else -> nextRecallStability(currentD, currentS, retrievability, grade)
            }
            difficulty = nextDifficulty(currentD, grade)
        }

        // Empty configured step lists are meaningful in py-fsrs: learning can be
        // disabled, and a lapse can remain in Review when relearning is disabled.
        val learningSteps = parseSteps(settings.learningStepsMinutesCsv)
        val relearningSteps = parseSteps(settings.relearningStepsMinutesCsv)
        var nextState = "Review"
        var nextStepIndex = -1
        var nextIntervalDays: Double

        when {
            grade == 1 && isNew -> {
                if (learningSteps.isEmpty()) {
                    nextState = "Review"
                    nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                } else {
                    nextState = "Learning"
                    nextStepIndex = 0
                    nextIntervalDays = learningSteps.first() / 1440.0
                }
            }
            grade == 1 && wasLearning -> {
                if (learningSteps.isEmpty()) {
                    nextState = "Review"
                    nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                } else {
                    nextState = "Learning"
                    nextStepIndex = 0
                    nextIntervalDays = learningSteps.first() / 1440.0
                }
            }
            grade == 1 && wasRelearning -> {
                if (relearningSteps.isEmpty()) {
                    nextState = "Review"
                    nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                } else {
                    nextState = "Relearning"
                    nextStepIndex = 0
                    nextIntervalDays = relearningSteps.first() / 1440.0
                }
            }
            grade == 1 && wasInReview -> {
                if (relearningSteps.isEmpty()) {
                    // py-fsrs keeps the card in Review when no relearning steps are configured.
                    nextState = "Review"
                    nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                } else {
                    nextState = "Relearning"
                    nextStepIndex = 0
                    nextIntervalDays = relearningSteps.first() / 1440.0
                }
            }
            isNew && learningSteps.isEmpty() -> {
                nextState = "Review"
                nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
            }
            isNew && grade == 2 -> {
                // py-fsrs v6.0.0 keeps step 0, but schedules the midpoint of the
                // first two learning steps (or 1.5x when only one step exists).
                nextState = "Learning"
                nextStepIndex = 0
                nextIntervalDays = hardStepMinutes(learningSteps, 0) / 1440.0
            }
            isNew && grade == 3 && learningSteps.size > 1 -> {
                // Good advances from the implicit first position to the next configured step.
                nextState = "Learning"
                nextStepIndex = 1
                nextIntervalDays = learningSteps[1] / 1440.0
            }
            wasLearning || wasRelearning -> {
                val steps = if (wasRelearning) relearningSteps else learningSteps
                val currentIndex = question.learningStepIndex.coerceAtLeast(0)
                if (steps.isEmpty() || (currentIndex >= steps.size && grade != 1)) {
                    nextState = "Review"
                    nextIntervalDays = intervalFor(stability, settings.desiredRetention, settings.maxIntervalDays)
                } else {
                    when (grade) {
                        2 -> {
                            // Hard keeps the current step and uses the upstream step-zero delay rule.
                            nextState = priorState
                            nextStepIndex = currentIndex
                            nextIntervalDays = hardStepMinutes(steps, currentIndex) / 1440.0
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
            }
            else -> {
                // Review cards and completed learning sequences use the FSRS review interval.
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
     * py-fsrs v6.0.0 uses timedelta.days: completed 24-hour periods, not local
     * calendar-day boundaries. A review 15 minutes after midnight can therefore
     * still be a same-day/short-term review if fewer than 24 hours elapsed.
     */
    fun elapsedStudyDays(lastReviewedAt: Long, reviewedAt: Long): Double =
        ((reviewedAt - lastReviewedAt).toDouble() / DAY_MS).toLong().coerceAtLeast(0L).toDouble()

    /** py-fsrs v6.0.0 Hard learning/relearning delay while retaining the step. */
    private fun hardStepMinutes(steps: List<Int>, currentIndex: Int): Double = when {
        currentIndex == 0 && steps.size == 1 -> steps[0] * 1.5
        currentIndex == 0 && steps.size >= 2 -> (steps[0] + steps[1]) / 2.0
        else -> steps[currentIndex].toDouble()
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
        val days = stability.coerceAtLeast(0.001) / factor * (safeRetention.pow(-1.0 / decay) - 1.0)
        // Python's round() uses ties-to-even; kotlin.math.round matches that rule.
        return kotlin.math.round(days).toInt().coerceIn(1, safeMax).toDouble()
    }

    fun calculateRetrievability(elapsedDays: Double, stability: Double): Double = retrievability(elapsedDays, stability)

    private fun retrievability(elapsedDays: Double, stability: Double): Double {
        val decay = weights[20]
        val factor = 0.9.pow(-1.0 / decay) - 1.0
        return (1.0 + factor * elapsedDays.coerceAtLeast(0.0) / stability.coerceAtLeast(0.001)).pow(-decay)
    }

    private fun initialDifficulty(grade: Int): Double {
        val value = weights[4] - exp(weights[5] * (grade - 1)) + 1.0
        return value.coerceIn(1.0, 10.0)
    }

    private fun nextDifficulty(current: Double, grade: Int): Double {
        val delta = -weights[6] * (grade - 3)
        val damped = delta * ((10.0 - current) / 9.0)
        val raw = current + damped
        // py-fsrs v6.0.0 uses the clamped initial Easy difficulty as the
        // mean-reversion target, then clamps the final updated difficulty.
        return (weights[7] * initialDifficulty(4) + (1.0 - weights[7]) * raw).coerceIn(1.0, 10.0)
    }

    private fun nextShortTermStability(stability: Double, grade: Int): Double {
        var increase = exp(weights[17] * (grade - 3 + weights[18])) * stability.pow(-weights[19])
        // The pinned v6.0.0 implementation applies the >= 1 floor only to
        // Good and Easy; Hard is allowed to reduce short-term stability.
        if (grade == 3 || grade == 4) increase = max(increase, 1.0)
        return (stability * increase).coerceAtLeast(0.001)
    }

    private fun nextForgetStability(difficulty: Double, stability: Double, retrievability: Double): Double {
        val minimum = stability / exp(weights[17] * weights[18])
        val calculated = weights[11] * difficulty.pow(-weights[12]) *
            ((stability + 1.0).pow(weights[13]) - 1.0) *
            exp((1.0 - retrievability) * weights[14])
        return minOf(calculated, minimum).coerceAtLeast(0.001)
    }

    private fun nextRecallStability(difficulty: Double, stability: Double, retrievability: Double, grade: Int): Double {
        val hardPenalty = if (grade == 2) weights[15] else 1.0
        val easyBonus = if (grade == 4) weights[16] else 1.0
        val growth = exp(weights[8]) * (11.0 - difficulty) * stability.pow(-weights[9]) *
            (exp((1.0 - retrievability) * weights[10]) - 1.0) * hardPenalty * easyBonus
        return (stability * (1.0 + growth)).coerceAtLeast(0.001)
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
