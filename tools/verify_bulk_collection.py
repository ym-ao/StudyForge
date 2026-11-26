#!/usr/bin/env python3
"""Static contract check for transactional, versioned bulk collection membership edits."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
rules = (ROOT / "app/src/main/java/com/studyforge/app/data/CollectionMembershipRules.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/CollectionMembershipRulesTest.kt").read_text(encoding="utf-8")
assert "onBulkCollection = { selected, collectionId, add -> scope.launch" in ui
assert "val validCollectionIds = dao.getAllCollections().map { it.id }.toSet()" in ui
assert "val oldIds = dao.getCollectionIds(current.id)" in ui
assert "val safeOldIds = oldIds.filter { it in validCollectionIds }" in ui
assert "CollectionMembershipRules.update(safeOldIds, collectionId, add)" in ui
assert "Collection update failed:" in ui
assert "fun update(existingIds: List<String>, collectionId: String, add: Boolean)" in rules
for case in ("addAppendsWithoutDuplicatingAndPreservesExistingOrder", "addingExistingCollectionIsNoOp", "removeDeletesOnlyRequestedCollection", "removeMissingCollectionIsNoOp", "blankIdsAreIgnoredAndWhitespaceIsTrimmed"):
    assert case in tests, f"Missing collection membership regression case: {case}"
assert "QuestionVersionRules.createSnapshot(current, dao.getContentBlocks(current.id), dao.getQuestionParts(current.id), oldIds)" in ui
assert "dao.clearQuestionCollections(current.id)" in ui
assert "dao.insertLinks(newIds.map { QuestionCollectionCrossRef(current.id, it) })" in ui
assert "dao.upsertQuestion(current.copy(updatedAt = System.currentTimeMillis()))" in ui
assert "database.withTransaction" in ui
assert 'Text("Add to collection")' in ui and 'Text("Remove from collection")' in ui
assert "showBulkCollectionDialog" in ui and "Choose collection" in ui
assert "clozeQuestionCanBeVersionedAndAddedToCollection" in (ROOT / "app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt").read_text(encoding="utf-8")
print("PASS: bulk collection add/remove validates targets, sanitizes stale links, snapshots prior membership, and commits atomically")
print("PASS: pure membership rules cover duplicate, no-op, removal, whitespace and ordering cases")
