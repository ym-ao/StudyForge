package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TextFormattingRulesTest {
    @Test fun boldWrapsSelectedTextAndKeepsSelectionInsideMarkers() {
        val result = TextFormattingRules.apply("alpha beta", 6, 10, "BOLD")
        assertEquals("alpha **beta**", result.text)
        assertEquals(8, result.selectionStart)
        assertEquals(12, result.selectionEnd)
    }

    @Test fun emptySelectionInsertsPlaceholderAndSelectsIt() {
        val result = TextFormattingRules.apply("abc", 1, 1, "ITALIC")
        assertEquals("a*italic text*bc", result.text)
        assertEquals(2, result.selectionStart)
        assertEquals(13, result.selectionEnd)
    }

    @Test fun headingPrefixesCurrentLineAndPreservesCaret() {
        val result = TextFormattingRules.apply("first\\nsecond", 8, 8, "HEADING")
        assertEquals("first\\n## second", result.text)
        assertEquals(11, result.selectionStart)
        assertEquals(11, result.selectionEnd)
    }

    @Test fun bulletCanBeRemovedFromAlreadyBulletedLine() {
        val result = TextFormattingRules.apply("- item", 4, 4, "BULLET")
        assertEquals("item", result.text)
    }

    @Test fun underlineAndHighlightWrapSelectedText() {
        assertEquals("__term__", TextFormattingRules.apply("term", 0, 4, "UNDERLINE").text)
        assertEquals("==term==", TextFormattingRules.apply("term", 0, 4, "HIGHLIGHT").text)
    }

    @Test fun headingPrefixesEverySelectedLineAndCanToggleAllOff() {
        val added = TextFormattingRules.apply("one\ntwo\nthree", 1, 8, "HEADING")
        assertEquals("## one\n## two\nthree", added.text)
        val removed = TextFormattingRules.apply("## one\n## two", 0, 13, "HEADING")
        assertEquals("one\ntwo", removed.text)
    }

    @Test fun bulletPrefixesEveryLineInSelection() {
        val result = TextFormattingRules.apply("first\nsecond", 0, 12, "BULLET")
        assertEquals("- first\n- second", result.text)
    }

    @Test fun reversedSelectionIsNormalized() {
        val result = TextFormattingRules.apply("abc", 3, 1, "CODE")
        assertEquals("a`bc`", result.text)
        assertEquals(2, result.selectionStart)
        assertEquals(4, result.selectionEnd)
    }
}
