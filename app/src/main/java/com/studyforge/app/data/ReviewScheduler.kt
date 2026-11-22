package com.studyforge.app.data

import androidx.room.withTransaction
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToLong

/** A small, transparent SM-2-inspired scheduler used by StudyForge 2.0. */
object ReviewScheduler {
    const val AGAIN = "Again"
    const val HARD = "Hard"
    const val GOOD = "Good"
    const val EASY = "Easy"

    fun apply(question: QuestionEntity, rating: String, reviewedAt: Long): QuestionEntity {
        val oldEase = question.easeFactor.coerceAtLeast(1.3)
        val oldInterval = question.intervalDays
        val oldRepetitions = question.repetitions
        val ease: Double
        val interval: Double
        val repetitions: Int
        val lapses: Int
        val state: String

        when (rating) {
            AGAIN -> {
                ease = max(1.3, oldEase - 0.20)
                interval = 10.0 / (24.0 * 60.0) // 10-minute relearning step
                repetitions = 0
                lapses = question.lapseCount + if (question.learningState == "New") 0 else 1
                state = "Relearning"
            }
            HARD -> {
                ease = max(1.3, oldEase - 0.15)
                interval = if (oldRepetitions == 0) 1.0 else max(1.0, oldInterval * 1.2)
                repetitions = oldRepetitions + 1
                lapses = question.lapseCount
                state = "Review"
            }
            GOOD -> {
                ease = oldEase
                interval = when (oldRepetitions) {
                    0 -> 1.0
                    1 -> 3.0
                    else -> max(1.0, oldInterval * oldEase)
                }
                repetitions = oldRepetitions + 1
                lapses = question.lapseCount
                state = "Review"
            }
            EASY -> {
                ease = (oldEase + 0.15).coerceAtMost(3.0)
                interval = when (oldRepetitions) {
                    0 -> 4.0
                    1 -> 7.0
                    else -> max(1.0, oldInterval * oldEase * 1.3)
                }
                repetitions = oldRepetitions + 1
                lapses = question.lapseCount
                state = "Review"
            }
            else -> error("Unknown review rating: $rating")
        }

        val due = reviewedAt + (interval * 24.0 * 60.0 * 60.0 * 1000.0).roundToLong()
        return question.copy(
            reviewCount = question.reviewCount + 1,
            lastReviewedAt = reviewedAt,
            dueAt = due,
            intervalDays = interval,
            easeFactor = ease,
            repetitions = repetitions,
            lapseCount = lapses,
            learningState = state
        )
    }
}

suspend fun StudyDatabase.recordGradedReview(
    questionId: String,
    rating: String,
    wasCorrect: Boolean?,
    mistakeCategory: String = "",
    reviewMode: String = "Review",
    reviewedAt: Long = System.currentTimeMillis()
) {
    withTransaction {
        val dao = dao()
        val current = dao.getQuestion(questionId) ?: return@withTransaction
        val updated = ReviewScheduler.apply(current, rating, reviewedAt)
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
            correctCount = current.correctCount + if (wasCorrect == true) 1 else 0,
            incorrectCount = current.incorrectCount + if (wasCorrect == false) 1 else 0
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
