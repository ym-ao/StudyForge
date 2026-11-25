package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class QuestionVersionRulesTest {
    @Test fun comparesScalarAndNestedFieldsAndReportsAddedOrRemovedValues() {
        val before = JSONObject().put("prompt", "old").put("sourceTitle", "Book")
            .put("parts", org.json.JSONArray().put(JSONObject().put("id", "p1").put("answer", "2")))
        val after = JSONObject().put("prompt", "new").put("parts", org.json.JSONArray()
            .put(JSONObject().put("id", "p1").put("answer", "3"))
            .put(JSONObject().put("id", "p2").put("answer", "4")))
        val changes = QuestionVersionRules.compare(before.toString(), after.toString()).associateBy { it.path }
        assertEquals("old", changes["prompt"]?.previous)
        assertEquals("new", changes["prompt"]?.current)
        assertEquals("Book", changes["sourceTitle"]?.previous)
        assertEquals("<not present>", changes["sourceTitle"]?.current)
        assertEquals("2", changes["parts[id=p1].answer"]?.previous)
        assertEquals("3", changes["parts[id=p1].answer"]?.current)
        assertEquals("<not present>", changes["parts[id=p2].answer"]?.previous)
    }

    @Test fun ignoresObjectKeyOrderAndSummarizesEmbeddedMediaWithoutExposingPayload() {
        val before = """{"contentBlocks":[{"id":"b1","mediaBase64":"YWJj","kind":"IMAGE"}]}"""
        val same = """{"contentBlocks":[{"kind":"IMAGE","mediaBase64":"YWJj","id":"b1"}]}"""
        val changed = """{"contentBlocks":[{"id":"b1","mediaBase64":"YWJjZA==","kind":"IMAGE"}]}"""
        assertTrue(QuestionVersionRules.compare(before, same).isEmpty())
        val change = QuestionVersionRules.compare(before, changed).single()
        assertTrue(change.path.endsWith("mediaBase64"))
        assertTrue(change.previous.contains("embedded media"))
        assertTrue(!change.previous.contains("YWJj"))
    }
}
