package com.studyforge.app.data

object ManualScheduleRules {
    fun setDue(question: QuestionEntity, dueAt: Long): QuestionEntity = question.copy(
        dueAt = dueAt.coerceAtLeast(0L), updatedAt = System.currentTimeMillis()
    )

    /** Reset scheduling memory without deleting review logs or lifetime performance counters. */
    fun reset(question: QuestionEntity): QuestionEntity = question.copy(
        dueAt = null, intervalDays = 0.0, easeFactor = 2.5, repetitions = 0,
        lapseCount = 0, learningState = "New", learningStepIndex = -1,
        fsrsStability = 0.0, fsrsDifficulty = 0.0, updatedAt = System.currentTimeMillis()
    )
}
