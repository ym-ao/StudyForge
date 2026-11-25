package com.studyforge.app.data

/** Criteria for the local question-library filter. Empty text fields are ignored. */
data class LibraryFilterCriteria(
    val query: String = "",
    val type: String = "All",
    val status: String = "All",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val tag: String = "",
    val collectionId: String? = null,
    val difficulty: String = "All",
    val exam: String = "",
    val performance: String = "All",
    val sourceTitle: String = "",
    val sourceDetail: String = "",
    val createdAfter: String = "",
    val createdBefore: String = "",
    val updatedAfter: String = "",
    val updatedBefore: String = ""
)

/** Pure filtering logic shared by the UI and regression tests. */
object QuestionLibraryFilter {
    /** Re-evaluates a saved filter against the current question/link snapshot. */
    fun countMatches(
        questions: List<QuestionEntity>,
        criteria: LibraryFilterCriteria,
        collectionQuestionIds: Set<String> = emptySet()
    ): Int = questions.count { matches(it, criteria, collectionQuestionIds) }

    fun matches(
        question: QuestionEntity,
        criteria: LibraryFilterCriteria,
        collectionQuestionIds: Set<String> = emptySet()
    ): Boolean {
        val query = criteria.query.trim()
        val textMatches = query.isBlank() || listOf(
            question.prompt, question.answer, question.explanation, question.subject,
            question.chapter, question.topic, question.tagsCsv, question.exam, question.difficulty,
            question.sourceTitle, question.sourceDetail
        ).any { it.contains(query, ignoreCase = true) }
        val typeMatches = criteria.type == "All" || question.type.equals(criteria.type, true)
        val statusMatches = criteria.status == "All" || question.status.equals(criteria.status, true)
        val subjectMatches = criteria.subject.isBlank() || question.subject.contains(criteria.subject.trim(), true)
        val chapterMatches = criteria.chapter.isBlank() || question.chapter.contains(criteria.chapter.trim(), true)
        val topicMatches = criteria.topic.isBlank() || question.topic.contains(criteria.topic.trim(), true)
        val tagMatches = criteria.tag.isBlank() || question.tagsCsv.split(',').any { it.trim().contains(criteria.tag.trim(), true) }
        val collectionMatches = criteria.collectionId == null || question.id in collectionQuestionIds
        val difficultyMatches = criteria.difficulty == "All" || question.difficulty.equals(criteria.difficulty, true)
        val examMatches = criteria.exam.isBlank() || question.exam.contains(criteria.exam.trim(), true)
        val sourceTitleMatches = criteria.sourceTitle.isBlank() || question.sourceTitle.contains(criteria.sourceTitle.trim(), true)
        val sourceDetailMatches = criteria.sourceDetail.isBlank() || question.sourceDetail.contains(criteria.sourceDetail.trim(), true)
        fun dateValue(timestamp: Long): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date(timestamp))
        fun validDate(value: String): Boolean = value.isBlank() || (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).apply { isLenient = false }.parse(value)
        }.getOrNull() != null)
        val dateCriteriaValid = listOf(criteria.createdAfter, criteria.createdBefore, criteria.updatedAfter, criteria.updatedBefore).all(::validDate)
        val createdDate = dateValue(question.createdAt)
        val updatedDate = dateValue(question.updatedAt)
        val createdDateMatches = (criteria.createdAfter.isBlank() || createdDate >= criteria.createdAfter) &&
            (criteria.createdBefore.isBlank() || createdDate <= criteria.createdBefore)
        val updatedDateMatches = (criteria.updatedAfter.isBlank() || updatedDate >= criteria.updatedAfter) &&
            (criteria.updatedBefore.isBlank() || updatedDate <= criteria.updatedBefore)
        val performanceMatches = when (criteria.performance) {
            "Unattempted" -> question.reviewCount == 0
            "Weak" -> question.reviewCount > 0 && question.incorrectCount > question.correctCount
            "Strong" -> question.reviewCount > 0 && question.correctCount > 0 && question.correctCount >= question.incorrectCount
            else -> true
        }
        return textMatches && typeMatches && statusMatches && subjectMatches && chapterMatches &&
            topicMatches && tagMatches && collectionMatches && difficultyMatches && examMatches && sourceTitleMatches && sourceDetailMatches && dateCriteriaValid && createdDateMatches && updatedDateMatches && performanceMatches
    }
}
