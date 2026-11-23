package com.studyforge.app.data

/** Deterministic multipart grading with text alternatives and numeric tolerances. */
object MultipartExamRules {
    data class Score(val earned: Double, val possible: Double, val answeredParts: Int, val correctParts: Int) {
        val percentage: Double get() = if (possible <= 0.0) 0.0 else earned * 100.0 / possible
    }

    fun normalizeAnswer(value: String): String = value.trim().replace(Regex("\\s+"), " ").lowercase()

    fun isCorrect(part: QuestionPartEntity, response: String): Boolean {
        if (response.isBlank()) return false
        val accepted = (listOf(part.answer) + part.acceptedAnswers.split("\n", "||").map(String::trim))
            .filter(String::isNotBlank)
        if (accepted.any { normalizeAnswer(it) == normalizeAnswer(response) }) return true
        val expected = part.answer.trim().toDoubleOrNull() ?: return false
        val actual = response.trim().replace(",", "").toDoubleOrNull() ?: return false
        if (!expected.isFinite() || !actual.isFinite()) return false
        val absTol = part.absoluteTolerance.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val relTol = part.relativeTolerance.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val allowed = maxOf(absTol, kotlin.math.abs(expected) * relTol)
        return kotlin.math.abs(actual - expected) <= allowed
    }

    fun score(parts: List<QuestionPartEntity>, answers: Map<String, String>): Score {
        val validParts = parts.filter { it.points.isFinite() && it.points > 0.0 }
        val possible = validParts.sumOf { it.points }
        val answered = validParts.count { !answers[it.id].isNullOrBlank() }
        val correctParts = validParts.filter { isCorrect(it, answers[it.id].orEmpty()) }
        return Score(correctParts.sumOf { it.points }, possible, answered, correctParts.size)
    }
}
