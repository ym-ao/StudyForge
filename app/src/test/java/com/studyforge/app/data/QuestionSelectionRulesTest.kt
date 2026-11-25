package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionSelectionRulesTest {
    private fun question(id: String) = QuestionEntity(id = id, prompt = "Prompt $id", answer = "Answer")

    @Test fun resolvesSelectedQuestionsAcrossTheCompleteLibraryInLibraryOrder() {
        val library = listOf(question("a"), question("b"), question("c"))
        val selected = QuestionSelectionRules.resolve(library, setOf("c", "a"))
        assertEquals(listOf("a", "c"), selected.map { it.id })
    }

    @Test fun ignoresSelectedIdsThatNoLongerExist() {
        val library = listOf(question("a"), question("b"))
        assertEquals(listOf("b"), QuestionSelectionRules.resolve(library, setOf("missing", "b" )).map { it.id })
    }

    @Test fun emptySelectionResolvesToNoQuestions() {
        assertEquals(emptyList<QuestionEntity>(), QuestionSelectionRules.resolve(listOf(question("a")), emptySet()))
    }
}
