package com.studyforge.app.data

/** Pure parsers and validation shared by the structured table and graph blocks. */
object ContentBlockParsing {
    fun tableRows(source: String): List<List<String>> = source.lineSequence()
        .filter { it.isNotBlank() }
        .take(30)
        .map { line -> line.trim().trim('|').split('|').take(8).map(String::trim) }
        .toList()

    fun validateTable(source: String): String? {
        val lines = source.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return "Add at least one table row."
        if (lines.size > 30) return "A table can contain no more than 30 rows."
        if (lines.any { it.trim().trim('|').split('|').size > 8 }) {
            return "A table can contain no more than 8 columns per row."
        }
        return null
    }

    fun graphPoints(source: String): List<Pair<Float, Float>> = source.lineSequence()
        .mapNotNull { line ->
            val pair = line.trim().split(',', ';', '\t')
            if (pair.size != 2) null else runCatching {
                val x = pair[0].trim().toFloat()
                val y = pair[1].trim().toFloat()
                if (x.isFinite() && y.isFinite()) x to y else null
            }.getOrNull()?.let { it }
        }
        .take(200)
        .toList()

    fun validateGraph(source: String): String? {
        val lines = source.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.size < 2) return "A graph needs at least two numeric x,y rows."
        if (lines.size > 200) return "A graph can contain no more than 200 data rows."
        lines.forEachIndexed { index, line ->
            val pair = line.trim().split(',', ';', '\t')
            if (pair.size != 2) return "Graph row ${index + 1} must contain exactly two values: x,y."
            val x = pair[0].trim().toFloatOrNull()
            val y = pair[1].trim().toFloatOrNull()
            if (x == null || y == null || !x.isFinite() || !y.isFinite()) {
                return "Graph row ${index + 1} must contain two finite numbers."
            }
        }
        return null
    }
}
