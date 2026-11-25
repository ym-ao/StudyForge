package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentBlockParsingTest {
    @Test fun parsesPipeSeparatedTableRows() {
        assertEquals(listOf(listOf("x", "y"), listOf("1", "2")), ContentBlockParsing.tableRows("x | y\n1 | 2"))
    }

    @Test fun tableParserIgnoresBlankRowsAndCapsDimensions() {
        val row = (1..10).joinToString("|") { "c$it" }
        val rows = ContentBlockParsing.tableRows("\n$row\n")
        assertEquals(1, rows.size)
        assertEquals(8, rows.single().size)
    }

    @Test fun parsesCommaSemicolonAndTabGraphPairs() {
        assertEquals(listOf(1f to 2f, 3f to 4f, 5f to 6f), ContentBlockParsing.graphPoints("1,2\n3;4\n5\\t6".replace("\\t", "\t")))
    }

    @Test fun graphParserSkipsInvalidLines() {
        val points = ContentBlockParsing.graphPoints("x,y\n1,2\n3,no\n4,5")
        assertEquals(listOf(1f to 2f, 4f to 5f), points)
    }

    @Test fun tableValidationRejectsMoreThanEightColumnsAndTooManyRows() {
        assertTrue(ContentBlockParsing.validateTable((1..9).joinToString("|") { "c$it" })!!.contains("8 columns"))
        assertTrue(ContentBlockParsing.validateTable((1..31).joinToString("\n") { "$it|value" })!!.contains("30 rows"))
    }

    @Test fun graphValidationRequiresTwoFinitePairsAndRejectsMalformedRows() {
        assertTrue(ContentBlockParsing.validateGraph("1,2")!!.contains("at least two"))
        assertTrue(ContentBlockParsing.validateGraph("1,2\n3,no")!!.contains("finite numbers"))
        assertTrue(ContentBlockParsing.validateGraph("1,2,3\n4,5")!!.contains("exactly two"))
        assertEquals(null, ContentBlockParsing.validateGraph("1,2\n3;4\n5\t6"))
    }

    @Test fun graphParserLimitsPointCount() {
        val source = (1..250).joinToString("\n") { "$it,${it * 2}" }
        assertEquals(200, ContentBlockParsing.graphPoints(source).size)
        assertTrue(ContentBlockParsing.graphPoints("1,2").size < 2)
    }
}
