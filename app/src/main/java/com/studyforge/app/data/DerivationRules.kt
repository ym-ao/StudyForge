package com.studyforge.app.data

/** A derivation is stored in an existing DERIVATION content block; each line is title|equation|reason. */
object DerivationRules {
    data class Step(val title: String, val equation: String, val explanation: String)
    fun parse(source: String): List<Step> = source.lineSequence().mapNotNull { line ->
        val parts = line.split('|', limit = 3)
        if (line.isBlank() || parts.size < 2) null else Step(parts[0].trim(), parts[1].trim(), parts.getOrNull(2).orEmpty().trim())
    }.toList()
    fun validate(steps: List<Step>): String? = when {
        steps.isEmpty() -> "Add at least one derivation step."
        steps.size > 40 -> "A derivation can contain at most 40 steps."
        steps.any { it.equation.isBlank() } -> "Every derivation step needs an equation."
        steps.any { it.title.length > 120 || it.equation.length > 2000 || it.explanation.length > 2000 } -> "A derivation step is too long."
        else -> null
    }
}
