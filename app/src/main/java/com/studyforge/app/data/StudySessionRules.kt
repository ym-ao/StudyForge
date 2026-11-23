package com.studyforge.app.data

object StudySessionRules {
    val supportedModes = setOf("Due", "All", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam")

    fun filterQuestions(questions: List<QuestionEntity>, preset: StudySessionPresetEntity): List<QuestionEntity> =
        questions.filter { question ->
            (preset.subject.isBlank() || question.subject.equals(preset.subject, true)) &&
            (preset.chapter.isBlank() || question.chapter.equals(preset.chapter, true)) &&
            (preset.topic.isBlank() || question.topic.equals(preset.topic, true)) &&
            (preset.tag.isBlank() || question.tagsCsv.split(',').any { it.trim().equals(preset.tag, true) }) &&
            (preset.difficulty == "All" || question.difficulty.equals(preset.difficulty, true)) &&
            (preset.exam.isBlank() || question.exam.equals(preset.exam, true)) &&
            (preset.questionType == "All" || question.type.equals(preset.questionType, true)) &&
            (preset.status == "All" || question.status.equals(preset.status, true)) &&
            when (preset.performance) {
                "Unattempted" -> question.reviewCount == 0
                "Weak" -> question.incorrectCount > question.correctCount
                "Strong" -> question.correctCount > question.incorrectCount && question.reviewCount > 0
                else -> true
            }
        }.take(preset.questionLimit.coerceIn(1, 500))
}
