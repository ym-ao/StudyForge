package com.studyforge.app.data

/** Pure parsers shared by the structured table and graph block previews. */
object ContentBlockParsing {
    fun tableRows(source: String): List<List<String>> = source.lineSequence()
        .filter { it.isNotBlank() }
        .take(30)
        .map { line -> line.trim().trim('|').split('|').take(8).map(String::trim) }
        .toList()

    fun graphPoints(source: String): List<Pair<Float, Float>> = source.lineSequence()
        .mapNotNull { line ->
            val pair = line.trim().split(',', ';', '\t')
            if (pair.size < 2) null else runCatching {
                val x = pair[0].trim().toFloat()
                val y = pair[1].trim().toFloat()
                if (x.isFinite() && y.isFinite()) x to y else null
            }.getOrNull()?.let { it }
        }
        .take(200)
        .toList()
}
