package com.studyforge.app.data

/** Resolves multi-selection against the complete Library, not only its current filtered view. */
object QuestionSelectionRules {
    /** Preserves Library order and ignores IDs that no longer exist. */
    fun resolve(questions: List<QuestionEntity>, selectedIds: Set<String>): List<QuestionEntity> =
        if (selectedIds.isEmpty()) emptyList() else questions.filter { it.id in selectedIds }
}
