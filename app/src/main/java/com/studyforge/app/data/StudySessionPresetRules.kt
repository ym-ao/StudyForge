package com.studyforge.app.data

/** Rules for safely resolving saved study-session preset edits. */
object StudySessionPresetRules {
    fun hasNameConflict(
        presets: List<StudySessionPresetEntity>,
        proposedName: String,
        editingPresetId: String?
    ): Boolean {
        val cleanName = proposedName.trim()
        if (cleanName.isBlank() || editingPresetId == null) return false
        return presets.any { preset ->
            preset.id != editingPresetId && preset.name.trim().equals(cleanName, ignoreCase = true)
        }
    }

    /** Editing always targets its original ID; creating with an existing name updates that preset. */
    fun existingForSave(
        presets: List<StudySessionPresetEntity>,
        editingPresetId: String?,
        proposedName: String
    ): StudySessionPresetEntity? = if (editingPresetId != null) {
        presets.firstOrNull { it.id == editingPresetId }
    } else {
        presets.firstOrNull { it.name.trim().equals(proposedName.trim(), ignoreCase = true) }
    }
}
