package com.studyforge.app.data

/** Helpers for the current comma-separated question tag format. */
object TagText {
    fun parse(csv: String): List<String> = csv.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }

    fun rename(csv: String, oldName: String, newName: String): String = parse(csv)
        .map { if (it.equals(oldName.trim(), ignoreCase = true)) newName.trim() else it }
        .distinctBy { it.lowercase() }
        .joinToString(", ")

    fun add(csv: String, name: String): String {
        val normalized = name.trim()
        if (normalized.isEmpty() || normalized.contains(',')) return parse(csv).joinToString(", ")
        return (parse(csv) + normalized).distinctBy { it.lowercase() }.joinToString(", ")
    }

    fun remove(csv: String, name: String): String = parse(csv)
        .filterNot { it.equals(name.trim(), ignoreCase = true) }
        .joinToString(", ")

    fun names(questions: List<QuestionEntity>): List<String> = questions
        .flatMap { parse(it.tagsCsv) }
        .distinctBy { it.lowercase() }
}
