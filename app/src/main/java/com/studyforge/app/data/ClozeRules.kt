package com.studyforge.app.data

/**
 * Parser for Anki-style cloze deletions: {{c1::hidden text}} and
 * {{c1::hidden text::optional hint}}.
 *
 * This implementation deliberately avoids a backtracking regular expression. Imported
 * prompts can be large or malformed; scanning keeps parsing bounded and leaves malformed
 * marker text untouched instead of throwing.
 */
object ClozeRules {
    data class Deletion(
        val index: Int,
        val answer: String,
        val hint: String?,
        val start: Int,
        val endExclusive: Int
    )

    private const val MAX_PROMPT_LENGTH = 100_000

    /** Returns every valid deletion in source order. Malformed markers are ignored. */
    fun parse(text: String): List<Deletion> {
        if (text.isEmpty() || text.length > MAX_PROMPT_LENGTH) return emptyList()
        val result = mutableListOf<Deletion>()
        var cursor = 0
        while (cursor < text.length - 4) {
            if (!text.regionMatches(cursor, "{{c", 0, 3, ignoreCase = true)) {
                cursor++
                continue
            }

            var numberEnd = cursor + 3
            while (numberEnd < text.length && text[numberEnd].isDigit()) numberEnd++
            if (numberEnd == cursor + 3 || numberEnd + 1 >= text.length ||
                text[numberEnd] != ':' || text[numberEnd + 1] != ':') {
                cursor++
                continue
            }
            val clozeIndex = text.substring(cursor + 3, numberEnd).toIntOrNull()
            if (clozeIndex == null || clozeIndex <= 0) {
                cursor++
                continue
            }

            val contentStart = numberEnd + 2
            val close = text.indexOf("}}", startIndex = contentStart)
            if (close < 0) {
                cursor++
                continue
            }
            val content = text.substring(contentStart, close)
            val hintSeparator = content.indexOf("::")
            val answer = (if (hintSeparator >= 0) content.substring(0, hintSeparator) else content).trim()
            val hint = if (hintSeparator >= 0) content.substring(hintSeparator + 2).trim().takeIf { it.isNotEmpty() } else null
            if (answer.isNotEmpty()) {
                result += Deletion(clozeIndex, answer, hint, cursor, close + 2)
                cursor = close + 2
            } else {
                cursor++
            }
        }
        return result
    }

    fun answers(text: String): List<String> = parse(text).map { it.answer }

    /** All cloze numbers, in first-appearance order (useful for Anki-style card generation). */
    fun indices(text: String): List<Int> = parse(text).map { it.index }.distinct()

    /** Masks every valid deletion. Malformed marker text remains visible and unchanged. */
    fun mask(text: String, replacement: String = "[ … ]"): String = replaceMatches(text, replacement) { true }

    /** Masks only one cloze number, leaving the other numbered deletions visible. */
    fun mask(text: String, clozeIndex: Int, replacement: String = "[ … ]"): String =
        replaceMatches(text, replacement) { it.index == clozeIndex }

    fun hasCloze(text: String): Boolean = parse(text).isNotEmpty()

    private inline fun replaceMatches(
        text: String,
        replacement: String,
        shouldMask: (Deletion) -> Boolean
    ): String {
        val matches = parse(text)
        if (matches.isEmpty()) return text
        return buildString(text.length) {
            var copiedUntil = 0
            for (match in matches) {
                append(text, copiedUntil, match.start)
                if (shouldMask(match)) append(replacement) else append(match.answer)
                copiedUntil = match.endExclusive
            }
            append(text, copiedUntil, text.length)
        }
    }
}
