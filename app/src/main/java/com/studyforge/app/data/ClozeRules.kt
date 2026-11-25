package com.studyforge.app.data

/** Minimal Anki-style cloze syntax: {{c1::hidden text}}. */
object ClozeRules {
    private val pattern = Regex("\\{\\{c\\d+::(.*?)(?:::[^}]*)?}}", RegexOption.IGNORE_CASE)
    fun answers(text: String): List<String> = pattern.findAll(text).map { it.groupValues[1].trim() }.filter { it.isNotBlank() }.toList()
    fun mask(text: String, replacement: String = "[ … ]"): String = pattern.replace(text, replacement)
    fun hasCloze(text: String): Boolean = pattern.containsMatchIn(text)
}
