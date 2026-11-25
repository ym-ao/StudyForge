package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudySessionPresetRulesTest {
    private val first = StudySessionPresetEntity("first", "Morning")
    private val second = StudySessionPresetEntity("second", "Evening")
    private val presets = listOf(first, second)

    @Test fun editingCannotRenameOverAnotherPreset() {
        assertTrue(StudySessionPresetRules.hasNameConflict(presets, " evening ", "first"))
        assertEquals(first, StudySessionPresetRules.existingForSave(presets, "first", "Evening"))
    }

    @Test fun editingOwnNameIsNotAConflictAndKeepsStableId() {
        assertFalse(StudySessionPresetRules.hasNameConflict(presets, " morning ", "first"))
        assertEquals(first, StudySessionPresetRules.existingForSave(presets, "first", "Morning"))
    }

    @Test fun creatingWithExistingNameUpdatesThatPreset() {
        assertFalse(StudySessionPresetRules.hasNameConflict(presets, "EVENING", null))
        assertEquals(second, StudySessionPresetRules.existingForSave(presets, null, "EVENING"))
    }

    @Test fun missingEditedPresetDoesNotHijackSameNamedPreset() {
        assertTrue(StudySessionPresetRules.hasNameConflict(presets, "Evening", "deleted-id"))
        assertNull(StudySessionPresetRules.existingForSave(presets, "deleted-id", "Evening"))
    }
}
