package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavedFilterPresetTest {
    @Test
    fun criteriaRoundTripPreservesEveryFilterField() {
        val criteria = LibraryFilterCriteria(
            query = "centripetal", type = "MCQ", status = "Verified",
            subject = "Physics", chapter = "Circular Motion", topic = "Acceleration",
            tag = "MDCAT", collectionId = "collection-123", difficulty = "Hard",
            exam = "MDCAT 2027", performance = "Weak",
            sourceTitle = "Biology Textbook", sourceDetail = "Chapter 4 page 82",
            createdAfter = "2026-01-01", createdBefore = "2026-10-31", updatedAfter = "2026-02-01", updatedBefore = "2026-11-01"
        )
        val saved = SavedFilterPresetEntity.fromCriteria(
            id = "preset-1", name = "  Hard Physics  ", criteria = criteria,
            createdAt = 100L, updatedAt = 200L
        )

        assertEquals("Hard Physics", saved.name)
        assertEquals(criteria, saved.toCriteria())
        assertEquals(100L, saved.createdAt)
        assertEquals(200L, saved.updatedAt)
    }

    @Test
    fun emptyPresetUsesAllFilterDefaults() {
        val saved = SavedFilterPresetEntity.fromCriteria(
            id = "preset-empty", name = "Everything", criteria = LibraryFilterCriteria()
        )
        assertEquals(LibraryFilterCriteria(), saved.toCriteria())
        assertNull(saved.collectionId)
    }

    @Test
    fun editingPresetRetainsIdentityAndCreationTimestamp() {
        val original = SavedFilterPresetEntity.fromCriteria(
            id = "preset-2", name = "Original", criteria = LibraryFilterCriteria(query = "wave"),
            createdAt = 123L, updatedAt = 124L
        )
        val edited = SavedFilterPresetEntity.fromCriteria(
            id = original.id, name = "Updated", criteria = LibraryFilterCriteria(query = "optics"),
            createdAt = original.createdAt, updatedAt = 456L
        )

        assertEquals(original.id, edited.id)
        assertEquals(original.createdAt, edited.createdAt)
        assertEquals(456L, edited.updatedAt)
        assertEquals("optics", edited.toCriteria().query)
    }
}
