package com.studyforge.app.data

/** Accuracy by question type, using only scored review-log records. */
object QuestionTypeAnalytics {
    data class Summary(val type: String, val scored: Int, val correct: Int) {
        val accuracyPercent: Int get() = if (scored == 0) 0 else correct * 100 / scored
    }

    fun summarize(questions: List<QuestionEntity>, logs: List<ReviewLogEntity>): List<Summary> {
        val typeById = questions.associate { it.id to it.type.ifBlank { "Unspecified" } }
        // Preserve first-seen review order so the analytics list is stable and
        // deleted-question history remains in its natural position.
        val grouped = linkedMapOf<String, MutableList<ReviewLogEntity>>()
        logs.asSequence().filter { it.wasCorrect != null }.forEach { log ->
            val type = typeById[log.questionId] ?: "Deleted question"
            grouped.getOrPut(type) { mutableListOf() }.add(log)
        }
        return grouped.map { (type, entries) ->
            Summary(type, entries.size, entries.count { it.wasCorrect == true })
        }
    }
}
