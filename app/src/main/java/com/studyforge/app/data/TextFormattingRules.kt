package com.studyforge.app.data

/** Selection-preserving Markdown formatting used by question and multipart editors. */
object TextFormattingRules {
    data class Result(val text: String, val selectionStart: Int, val selectionEnd: Int)

    fun apply(text: String, selectionStart: Int, selectionEnd: Int, format: String): Result {
        val first = selectionStart.coerceIn(0, text.length)
        val second = selectionEnd.coerceIn(0, text.length)
        val start = minOf(first, second)
        val end = maxOf(first, second)
        val selected = text.substring(start, end)
        return when (format.uppercase()) {
            "BOLD" -> wrap(text, start, end, selected, "**", "**", "bold text")
            "ITALIC" -> wrap(text, start, end, selected, "*", "*", "italic text")
            "CODE" -> wrap(text, start, end, selected, "`", "`", "code")
            "UNDERLINE" -> wrap(text, start, end, selected, "__", "__", "underlined text")
            "HIGHLIGHT" -> wrap(text, start, end, selected, "==", "==", "highlighted text")
            "HEADING" -> prefixLine(text, start, end, "## ")
            "BULLET" -> prefixLine(text, start, end, "- ")
            else -> Result(text, start, end)
        }
    }

    private fun wrap(
        text: String, start: Int, end: Int, selected: String,
        prefix: String, suffix: String, placeholder: String
    ): Result {
        val content = selected.ifEmpty { placeholder }
        val replacement = prefix + content + suffix
        val updated = text.replaceRange(start, end, replacement)
        val innerStart = start + prefix.length
        return Result(updated, innerStart, innerStart + content.length)
    }

    private fun prefixLine(text: String, start: Int, end: Int, prefix: String): Result {
        val starts = mutableListOf<Int>()
        var cursor = 0
        while (cursor <= text.length) {
            starts += cursor
            val newline = text.indexOf('\n', cursor)
            if (newline < 0) break
            cursor = newline + 1
        }
        val firstLine = starts.indexOfLast { it <= start }.coerceAtLeast(0)
        val endProbe = if (end > start) end - 1 else start
        val lastLine = starts.indexOfLast { it <= endProbe }.coerceAtLeast(firstLine)
        val selectedStarts = starts.subList(firstLine, lastLine + 1)
        val allPrefixed = selectedStarts.all { lineStart -> text.startsWith(prefix, lineStart) }
        val delta = if (allPrefixed) -prefix.length else prefix.length
        val updated = StringBuilder(text)
        selectedStarts.asReversed().forEach { lineStart ->
            if (allPrefixed) updated.delete(lineStart, lineStart + prefix.length)
            else updated.insert(lineStart, prefix)
        }
        val newStart = (start + selectedStarts.count { it <= start } * delta).coerceIn(0, updated.length)
        val newEnd = (end + selectedStarts.count { it < end } * delta).coerceIn(newStart, updated.length)
        return Result(updated.toString(), newStart, newEnd)
    }
}
