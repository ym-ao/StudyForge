#!/usr/bin/env python3
"""Static contract checks for saved study-session preset editing."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text(encoding="utf-8")
rules = (ROOT / "app/src/main/java/com/studyforge/app/data/StudySessionPresetRules.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/StudySessionPresetRulesTest.kt").read_text(encoding="utf-8")

required = [
    "var editingSessionPresetId by rememberSaveable",
    'title = { Text(if (editingSessionPresetId != null) "Edit study session" else "Save study session") }',
    "val existing = StudySessionPresetRules.existingForSave(sessionPresets, editingSessionPresetId, newSessionName)",
    "editingSessionPresetId = preset.id",
    "newSessionName = preset.name",
    "newSessionSubject = preset.subject",
    "newSessionChapter = preset.chapter",
    "newSessionTag = preset.tag",
    "newSessionDifficulty = preset.difficulty",
    "newSessionExam = preset.exam",
    "newSessionType = preset.questionType",
    "newSessionStatus = preset.status",
    "newSessionPerformance = preset.performance",
    "newSessionCollectionId = preset.collectionId",
    "newSessionLimit = preset.questionLimit.coerceIn(1, 500).toString()",
    'Text("Study mode", style = MaterialTheme.typography.labelMedium)',
    'FilterChip(selected = mode == option, onClick = { mode = option }',
    "editingSessionPresetId = null",
]
for item in required:
    assert item in source, f"Saved-session edit workflow missing: {item}"

assert source.count('Text("Edit")') >= 1, "Saved-session rows must expose an Edit action"
assert "sessionPresetNameConflict" in source and "!sessionPresetNameConflict" in source, "Conflicting rename must be blocked visibly"
assert "sessionPresetPendingDelete = preset" in source, "Delete must require confirmation"
assert "Delete saved session?" in source, "Delete confirmation dialog is missing"
assert "if (activeSessionPresetId == preset.id) activeSessionPresetId = null" in source, "Deleting active preset must clear its selection"
assert "editingPresetId == null" in rules, "New same-name saves should retain update-by-name behavior"
assert "preset.id != editingPresetId" in rules, "Renames must detect collisions excluding the edited preset"
assert "if (editingPresetId != null)" in rules, "Editing must resolve by stable ID only"
for case in ("editingCannotRenameOverAnotherPreset", "editingOwnNameIsNotAConflictAndKeepsStableId", "creatingWithExistingNameUpdatesThatPreset", "missingEditedPresetDoesNotHijackSameNamedPreset"):
    assert case in tests, f"Missing preset safety regression: {case}"
assert 'Text("Delete")' in source, "Editing must not remove existing delete support"
assert 'Text("Save current")' in source, "Creating a new preset must remain available"
print("PASS: saved study-session presets can be edited, renamed by stable ID, and retain all filter fields")
print("PASS: saved-session edit dialog exposes all saved criteria, including study mode")
print("PASS: conflicting renames are blocked and deletion requires confirmation")
print("PASS: deleting the active preset clears its active selection")
print("PASS: new preset creation and preset deletion remain available")
