package com.studyforge.app.data

/** Accuracy by question type, using only scored review-log records. */
object QuestionTypeAnalytics {
    data class Summary(val type: String, val scored: Int, val correct: Int) {
        val accuracyPercent: Int get() = if (scored == 0) 0 else correct * 100 / scored
    }

    fun summarize(questions: List<QuestionEntity>, logs: List<ReviewLogEntity>): List<Summary> {
        val typeById = questions.associate { it.id to it.type.ifBlank { "Unspecified" } }
        return logs.asSequence()
            .filter { it.wasCorrect != null }
            .groupBy { typeById[it.questionId] ?: "Deleted question" }
            .map { (type, entries) -> Summary(type, entries.size, entries.count { it.wasCorrect == true }) }
            .sortedWith(compareByDescending<Summary> { it.scored }.thenBy { it.type })
    }
}
