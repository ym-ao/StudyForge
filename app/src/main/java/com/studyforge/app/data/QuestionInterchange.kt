package com.studyforge.app.data

/** CSV and Anki TSV interchange for the simple, portable question fields.
 * Rich blocks, multipart grading, review state and collections remain in JSON backups.
 */
object QuestionInterchange {
    val headers = listOf("id", "type", "prompt", "answer", "explanation", "options", "subject", "chapter", "topic", "tags", "difficulty", "exam", "status", "latex", "sourceTitle", "sourceDetail")

    data class ImportIssue(val rowNumber: Int, val message: String)

    data class ImportPreview(
        val rows: List<Row>,
        val issues: List<ImportIssue>,
        val duplicateCount: Int,
        val updateCount: Int,
        val newCount: Int,
        val totalRows: Int
    ) {
        val skippedCount: Int get() = issues.size + duplicateCount
        val canImport: Boolean get() = rows.isNotEmpty()
    }

    data class Row(
        val id: String = "",
        val type: String = "Flashcard",
        val prompt: String,
        val answer: String,
        val explanation: String = "",
        val options: String = "",
        val subject: String = "",
        val chapter: String = "",
        val topic: String = "",
        val tags: String = "",
        val difficulty: String = "Unrated",
        val exam: String = "",
        val status: String = "Draft",
        val latex: String = "",
        val sourceTitle: String = "",
        val sourceDetail: String = ""
    )

    fun exportCsv(questions: List<QuestionEntity>): String = buildString {
        append(headers.joinToString(",") { cell(it) }).append('\n')
        questions.forEach { q ->
            val row = Row(q.id, q.type, q.prompt, q.answer, q.explanation,
                parseOptions(q.optionsJson).joinToString(" | "), q.subject, q.chapter,
                q.topic, q.tagsCsv, q.difficulty, q.exam, q.status, q.latex, q.sourceTitle, q.sourceDetail)
            append(row.values().joinToString(",") { cell(it) }).append('\n')
        }
    }


    /** Validates a CSV/Anki file before it is written. Existing IDs are updates; duplicate content is skipped. */
    fun previewCsv(source: String, existing: List<QuestionEntity>): ImportPreview =
        previewDelimited(source, ',', existing, anki = false)

    fun previewAnkiTsv(source: String, existing: List<QuestionEntity>): ImportPreview =
        previewDelimited(source, '\t', existing, anki = true)

    private fun previewDelimited(source: String, delimiter: Char, existing: List<QuestionEntity>, anki: Boolean): ImportPreview {
        val table = try { parseDelimited(source, delimiter) } catch (error: Exception) {
            return ImportPreview(emptyList(), listOf(ImportIssue(0, error.message ?: "Unable to parse file")), 0, 0, 0, 0)
        }
        if (table.isEmpty()) return ImportPreview(emptyList(), listOf(ImportIssue(0, "The file is empty")), 0, 0, 0, 0)
        val header = table.first().map { it.trim().lowercase().replace(Regex("[^a-z0-9]"), "") }
        val hasHeader = if (anki) header.any { it in setOf("front", "back", "tags") }
            else header.any { it in setOf("prompt", "front", "question") }
        val data = if (hasHeader) table.drop(1) else table
        val startRow = if (hasHeader) 2 else 1
        val existingById = existing.associateBy { it.id }
        val existingContent = existing.associateBy { contentKey(it.prompt, it.answer) }
        val seenIds = mutableSetOf<String>()
        val seenContent = mutableSetOf<String>()
        val valid = mutableListOf<Row>()
        val issues = mutableListOf<ImportIssue>()
        var duplicates = 0
        var updates = 0
        var creates = 0
        data.forEachIndexed { index, cells ->
            val rowNumber = startRow + index
            fun field(vararg names: String, fallback: Int = -1): String {
                val position = names.firstNotNullOfOrNull { name -> header.indexOf(name).takeIf { hasHeader && it >= 0 } } ?: fallback
                return cells.getOrNull(position).orEmpty()
            }
            val parsedRow = if (anki) {
                val frontIndex = if (hasHeader) header.indexOf("front").takeIf { it >= 0 } ?: 0 else 0
                val backIndex = if (hasHeader) header.indexOf("back").takeIf { it >= 0 } ?: 1 else 1
                val tagsIndex = if (hasHeader) header.indexOf("tags").takeIf { it >= 0 } ?: 2 else 2
                val front = cells.getOrNull(frontIndex).orEmpty().trim()
                val back = cells.getOrNull(backIndex).orEmpty().trim()
                if (front.isBlank() || back.isBlank()) null else Row(prompt = front, answer = back,
                    tags = cells.getOrNull(tagsIndex).orEmpty().replace(' ', ','))
            } else {
                val prompt = field("prompt", "front", "question", fallback = 0).trim()
                val answer = field("answer", "back", fallback = 1).trim()
                val type = field("type", "questiontype").ifBlank { "Flashcard" }.trim()
                if (prompt.isBlank() || (answer.isBlank() && type != "Cloze")) null else Row(
                    id = field("id", "guid").trim(), type = type,
                    prompt = prompt, answer = answer, explanation = field("explanation", "notes"),
                    options = field("options", "choices"), subject = field("subject"), chapter = field("chapter"),
                    topic = field("topic"), tags = field("tags", "tag"),
                    difficulty = field("difficulty").ifBlank { "Unrated" }, exam = field("exam", "syllabus"),
                    status = field("status").ifBlank { "Draft" }, latex = field("latex", "equation"),
                    sourceTitle = field("sourcetitle", "source", "book"), sourceDetail = field("sourcedetail", "page", "reference")
                )
            }
            if (parsedRow == null) {
                issues += ImportIssue(rowNumber, "Missing required prompt/front or answer/back")
                return@forEachIndexed
            }
            val row = if (parsedRow.type == "Cloze") {
                val clozeAnswers = ClozeRules.answers(parsedRow.prompt)
                if (clozeAnswers.isEmpty()) {
                    issues += ImportIssue(rowNumber, "Cloze rows need at least one valid {{c1::hidden text}} deletion")
                    return@forEachIndexed
                }
                // The deletion text is the source of truth, matching the Question Editor save path.
                parsedRow.copy(answer = clozeAnswers.joinToString("; "))
            } else parsedRow
            if (row.prompt.length > 100_000 || row.answer.length > 100_000 || row.explanation.length > 100_000 || row.options.length > 100_000) {
                issues += ImportIssue(rowNumber, "Prompt, answer, explanation or options exceed the 100,000 character limit")
                return@forEachIndexed
            }
            if (row.subject.length > 120 || row.chapter.length > 120 || row.topic.length > 120 || row.exam.length > 120 ||
                row.tags.length > 1000 || row.latex.length > 20_000 || row.sourceTitle.length > 300 || row.sourceDetail.length > 1000) {
                issues += ImportIssue(rowNumber, "A metadata field exceeds its supported length limit")
                return@forEachIndexed
            }
            if (row.type !in setOf("Flashcard", "MCQ", "True/False", "Numerical", "Cloze")) {
                issues += ImportIssue(rowNumber, "Unsupported question type: ${row.type}")
                return@forEachIndexed
            }
            if (row.status !in setOf("Draft", "Verified", "Unverified", "Incorrect", "Archived")) {
                issues += ImportIssue(rowNumber, "Unsupported question status: ${row.status}")
                return@forEachIndexed
            }
            if (row.difficulty !in setOf("Unrated", "Easy", "Medium", "Hard")) {
                issues += ImportIssue(rowNumber, "Unsupported difficulty: ${row.difficulty}")
                return@forEachIndexed
            }
            if (row.type == "MCQ" || row.type == "True/False") {
                val options = row.options.split('|').map { it.trim() }.filter { it.isNotBlank() }
                if (options.size < 2 || options.none { it == row.answer.trim() }) {
                    issues += ImportIssue(rowNumber, "MCQ/True-False rows need at least two options and an answer matching one option")
                    return@forEachIndexed
                }
            }
            val key = contentKey(row.prompt, row.answer)
            if ((row.id.isNotBlank() && !seenIds.add(row.id)) || !seenContent.add(key)) {
                duplicates++
                return@forEachIndexed
            }
            val matchingId = row.id.takeIf { it.isNotBlank() }?.let(existingById::get)
            if (matchingId != null) {
                updates++
            } else if (existingContent.containsKey(key)) {
                duplicates++
                return@forEachIndexed
            } else {
                creates++
            }
            valid += row
        }
        return ImportPreview(valid, issues, duplicates, updates, creates, data.size)
    }

    private fun contentKey(prompt: String, answer: String): String =
        (prompt.trim().lowercase().replace(Regex("\\s+"), " ") + "\u0000" + answer.trim().lowercase().replace(Regex("\\s+"), " "))

    fun importCsv(source: String): List<Row> = parseDelimited(source, ',').let { table ->
        require(table.isNotEmpty()) { "The CSV file is empty" }
        val normalized = table.first().map { it.trim().lowercase().replace(Regex("[^a-z0-9]"), "") }
        val hasHeader = normalized.any { it in setOf("prompt", "front", "question") }
        val rows = if (hasHeader) table.drop(1) else table
        val index = if (hasHeader) normalized.mapIndexedNotNull { i, key -> key to i }.toMap() else emptyMap()
        rows.mapNotNull { cells ->
            fun field(vararg keys: String, fallback: Int = -1): String {
                val i = keys.firstNotNullOfOrNull { index[it] } ?: fallback
                return cells.getOrNull(i).orEmpty()
            }
            val prompt = field("prompt", "front", "question", fallback = 0).trim()
            val answer = field("answer", "back", fallback = 1).trim()
            if (prompt.isBlank() || answer.isBlank()) return@mapNotNull null
            Row(
                id = field("id", "guid"), type = field("type", "questiontype").ifBlank { "Flashcard" },
                prompt = prompt, answer = answer,
                explanation = field("explanation", "notes"), options = field("options", "choices"),
                subject = field("subject"), chapter = field("chapter"), topic = field("topic"),
                tags = field("tags", "tag"), difficulty = field("difficulty").ifBlank { "Unrated" },
                exam = field("exam", "syllabus"), status = field("status").ifBlank { "Draft" }, latex = field("latex", "equation"),
                sourceTitle = field("sourcetitle", "source", "book"), sourceDetail = field("sourcedetail", "page", "reference")
            )
        }
    }

    fun exportAnkiTsv(questions: List<QuestionEntity>): String = buildString {
        questions.forEach { q ->
            append(tsvCell(q.prompt)).append('\t').append(tsvCell(q.answer + if (q.explanation.isBlank()) "" else "\n\n${q.explanation}"))
                .append('\t').append(tsvCell(q.tagsCsv.replace(',', ' '))).append('\n')
        }
    }

    fun importAnkiTsv(source: String): List<Row> {
        val rows = parseDelimited(source, '\t')
        require(rows.isNotEmpty()) { "The Anki file is empty" }
        val first = rows.first().map { it.trim().lowercase() }
        val header = first.any { it in setOf("front", "back", "tags") }
        val data = if (header) rows.drop(1) else rows
        return data.mapNotNull { cells ->
            val front = cells.getOrNull(if (header) first.indexOf("front").takeIf { it >= 0 } ?: 0 else 0).orEmpty().trim()
            val back = cells.getOrNull(if (header) first.indexOf("back").takeIf { it >= 0 } ?: 1 else 1).orEmpty().trim()
            if (front.isBlank() || back.isBlank()) null else Row(prompt = front, answer = back,
                tags = cells.getOrNull(if (header) first.indexOf("tags").takeIf { it >= 0 } ?: 2 else 2).orEmpty().replace(' ', ','))
        }
    }

    private fun Row.values() = listOf(id, type, prompt, answer, explanation, options, subject, chapter, topic, tags, difficulty, exam, status, latex, sourceTitle, sourceDetail)
    private fun cell(value: String): String = if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"${value.replace("\"", "\"\"")}\"" else value
    private fun tsvCell(value: String): String = if (value.any { it == '\t' || it == '"' || it == '\n' || it == '\r' }) "\"${value.replace("\"", "\"\"")}\"" else value

    private fun parseOptions(raw: String): List<String> {
        val source = raw.trim()
        if (!source.startsWith("[") || !source.endsWith("]")) return emptyList()
        val values = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var escaped = false
        for (ch in source.substring(1, source.length - 1)) {
            when {
                escaped -> { field.append(when (ch) { 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; else -> ch }); escaped = false }
                quoted && ch == '\\' -> escaped = true
                ch == '"' -> quoted = !quoted
                ch == ',' && !quoted -> { values += field.toString(); field.setLength(0) }
                else -> if (quoted) field.append(ch)
            }
        }
        if (field.isNotEmpty()) values += field.toString()
        return values
    }

    /** RFC-4180-style parser. Quotes may only begin a field; after a closing quote,
     * only a delimiter, record separator, or end-of-input is valid.
     */
    internal fun parseDelimited(source: String, delimiter: Char): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var afterQuote = false
        var i = 0

        fun finishField() {
            row += field.toString()
            field.setLength(0)
            afterQuote = false
        }

        fun finishRow() {
            finishField()
            if (row.any { it.isNotEmpty() }) result += row
            row = mutableListOf()
        }

        while (i < source.length) {
            val ch = source[i]
            when {
                inQuotes && ch == '"' && i + 1 < source.length && source[i + 1] == '"' -> {
                    field.append('"')
                    i++
                }
                inQuotes && ch == '"' -> {
                    inQuotes = false
                    afterQuote = true
                }
                inQuotes -> field.append(ch)
                afterQuote && ch == delimiter -> finishField()
                afterQuote && (ch == '\n' || ch == '\r') -> {
                    finishRow()
                    if (ch == '\r' && i + 1 < source.length && source[i + 1] == '\n') i++
                }
                afterQuote -> throw IllegalArgumentException("Unexpected character after a closing quote")
                ch == '"' && field.isEmpty() -> inQuotes = true
                ch == '"' -> throw IllegalArgumentException("Quote found inside an unquoted field")
                ch == delimiter -> finishField()
                ch == '\n' || ch == '\r' -> {
                    finishRow()
                    if (ch == '\r' && i + 1 < source.length && source[i + 1] == '\n') i++
                }
                else -> field.append(ch)
            }
            i++
        }
        require(!inQuotes) { "The file contains an unclosed quoted field" }
        finishField()
        if (row.any { it.isNotEmpty() }) result += row
        return result
    }
}
