#!/usr/bin/env python3
"""Static guard: every exported StudyForge table is read in one Room transaction."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
start = source.index("private suspend fun exportBackup(")
end = source.index("private suspend fun importBackup(", start)
exporter = source[start:end]
assert "val snapshot = database.withTransaction" in exporter
transaction_start = exporter.index("val snapshot = database.withTransaction")
transaction_end = exporter.index("\n    }\n    val questions = snapshot.questions", transaction_start)
transaction = exporter[transaction_start:transaction_end]
expected = (
    "getAllQuestions()", "getAllCollections()", "getAllLinks()", "getAllReviewLogs()",
    "getSettings()", "getAllSavedFilterPresets()", "getAllTags()", "getAllContentBlocks()",
    "getAllQuestionParts()", "getAllStudySessionPresets()", "getAllExamAttempts()",
    "getAllQuestionVersions()",
)
for call in expected:
    assert call in transaction, f"Backup snapshot omits or reads outside transaction: {call}"
assert "openOutputStream(uri)" in exporter
print("PASS: all 12 backup data sources are captured in one Room transaction")
