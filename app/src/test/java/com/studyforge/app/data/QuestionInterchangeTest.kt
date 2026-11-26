package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class QuestionInterchangeTest {
    @Test fun csvRoundTripsCommasQuotesAndMultilineText() {
        val question = QuestionEntity("id-1", "What, exactly?\nSay \"yes\"", "A, B", explanation = "line 1\nline 2", subject = "Physics", tagsCsv = "vectors, motion", sourceTitle = "Physics Textbook", sourceDetail = "Chapter 2, page 17")
        val encoded = QuestionInterchange.exportCsv(listOf(question))
        val decoded = QuestionInterchange.importCsv(encoded).single()
        assertEquals(question.id, decoded.id)
        assertEquals(question.prompt, decoded.prompt)
        assertEquals(question.answer, decoded.answer)
        assertEquals(question.explanation, decoded.explanation)
        assertEquals(question.tagsCsv, decoded.tags)
        assertEquals(question.sourceTitle, decoded.sourceTitle)
        assertEquals(question.sourceDetail, decoded.sourceDetail)
    }

    @Test fun csvImportsHeaderlessTwoColumnRows() {
        val row = QuestionInterchange.importCsv("front,back\nQuestion,Answer").single()
        assertEquals("Question", row.prompt)
        assertEquals("Answer", row.answer)
    }

    @Test fun ankiTsvSupportsQuotedTabsAndHeaderlessCards() {
        val rows = QuestionInterchange.importAnkiTsv("\"Front\tfield\"\t\"Back\nfield\"\ttag1 tag2")
        assertEquals(1, rows.size)
        assertEquals("Front\tfield", rows.single().prompt)
        assertEquals("Back\nfield", rows.single().answer)
        assertEquals("tag1,tag2", rows.single().tags)
    }


    @Test fun previewReportsInvalidRowsAndKeepsValidRows() {
        val preview = QuestionInterchange.previewCsv("front,back\nGood question,Good answer\nMissing answer,", emptyList())
        assertEquals(2, preview.totalRows)
        assertEquals(1, preview.rows.size)
        assertEquals(1, preview.issues.size)
        assertEquals(3, preview.issues.single().rowNumber)
        assertTrue(preview.canImport)
    }

    @Test fun previewSkipsExistingAndWithinFileDuplicates() {
        val existing = listOf(QuestionEntity("existing", "Same prompt", "Same answer"))
        val preview = QuestionInterchange.previewCsv(
            "front,back\nSame prompt,Same answer\nNew prompt,New answer\nNew prompt,New answer",
            existing
        )
        assertEquals(1, preview.rows.size)
        assertEquals("New prompt", preview.rows.single().prompt)
        assertEquals(2, preview.duplicateCount)
        assertEquals(1, preview.newCount)
    }

    @Test fun matchingStableIdIsClassifiedAsUpdate() {
        val existing = listOf(QuestionEntity("stable-id", "Old prompt", "Old answer"))
        val preview = QuestionInterchange.previewCsv(
            "id,prompt,answer\nstable-id,Changed prompt,Changed answer", existing
        )
        assertEquals(1, preview.updateCount)
        assertEquals(0, preview.newCount)
        assertEquals("stable-id", preview.rows.single().id)
    }

    @Test fun ankiPreviewDetectsDuplicateCards() {
        val preview = QuestionInterchange.previewAnkiTsv(
            "Front\tBack\tTags\nQuestion\tAnswer\ttag\nQuestion\tAnswer\tother", emptyList()
        )
        assertEquals(1, preview.rows.size)
        assertEquals(1, preview.duplicateCount)
    }

    @Test fun rejectsUnclosedQuotedField() {
        assertThrows(IllegalArgumentException::class.java) { QuestionInterchange.importCsv("front,back\n\"oops,answer") }
    }
    @Test fun previewRejectsUnsupportedTypesAndInvalidMcqOptions() {
        val invalidType = QuestionInterchange.previewCsv("type,prompt,answer\nEssay,Question,Answer", emptyList())
        assertEquals(0, invalidType.rows.size)
        assertTrue(invalidType.issues.single().message.contains("Unsupported question type"))
        val invalidMcq = QuestionInterchange.previewCsv("type,prompt,answer,options\nMCQ,Question,A,B | C", emptyList())
        assertEquals(0, invalidMcq.rows.size)
        assertTrue(invalidMcq.issues.single().message.contains("MCQ/True-False"))
        val validMcq = QuestionInterchange.previewCsv("type,prompt,answer,options\nMCQ,Question,A,A | B", emptyList())
        assertEquals(1, validMcq.rows.size)
    }

    @Test fun clozeCsvRoundTripsWithTheDeletionTextAsTheCanonicalAnswer() {
        val source = QuestionEntity(
            id = "cloze-csv-1",
            prompt = "The {{c1::mitochondrion}} makes {{c2::ATP}}.",
            answer = "old answer",
            type = "Cloze"
        )
        val csv = QuestionInterchange.exportCsv(listOf(source))
        val preview = QuestionInterchange.previewCsv(csv, emptyList())
        assertEquals(1, preview.rows.size)
        assertEquals("mitochondrion; ATP", preview.rows.single().answer)
        assertEquals(source.prompt, preview.rows.single().prompt)
    }

    @Test fun clozeCsvImportValidatesMarkupAndDerivesAnswerFromPrompt() {
        val valid = QuestionInterchange.previewCsv(
            "type,prompt,answer\nCloze,The {{c1::mitochondrion}} makes {{c2::ATP}},incorrect spreadsheet answer",
            emptyList()
        )
        assertEquals(1, valid.rows.size)
        assertEquals("mitochondrion; ATP", valid.rows.single().answer)

        val blankBack = QuestionInterchange.previewCsv(
            "type,prompt,answer\nCloze,The {{c1::nucleus}} stores DNA,",
            emptyList()
        )
        assertEquals(1, blankBack.rows.size)
        assertEquals("nucleus", blankBack.rows.single().answer)

        val malformed = QuestionInterchange.previewCsv(
            "type,prompt,answer\nCloze,No deletion here,answer",
            emptyList()
        )
        assertTrue(malformed.rows.isEmpty())
        assertTrue(malformed.issues.single().message.contains("Cloze rows need"))
    }

    @Test fun rejectsQuoteInsideUnquotedField() {
        assertThrows(IllegalArgumentException::class.java) {
            QuestionInterchange.importCsv("front,back\nQuestion with \"quote,Answer")
        }
    }

    @Test fun rejectsCharactersAfterClosingQuotedField() {
        assertThrows(IllegalArgumentException::class.java) {
            QuestionInterchange.importCsv("front,back\n\"Question\"suffix,Answer")
        }
    }

    @Test fun acceptsQuotedFieldFollowedByDelimiterAndCrLf() {
        val rows = QuestionInterchange.parseDelimited("\"Front, part\",Back\r\nNext,Answer\r\n", ',')
        assertEquals(listOf(listOf("Front, part", "Back"), listOf("Next", "Answer")), rows)
    }

}
