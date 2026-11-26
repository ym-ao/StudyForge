#!/usr/bin/env python3
"""Static guard for the V3.0 production Library migration boundary.

This intentionally fails while the production Library still consumes the full
QuestionEntity list. It is a readiness gate, not a substitute for Gradle,
Room, Compose, emulator, or device tests.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt"
PAGED_UI = ROOT / "app/src/main/java/com/studyforge/app/ui/PagedLibraryScreen.kt"
REPO = ROOT / "app/src/main/java/com/studyforge/app/data/QuestionRepository.kt"
VM = ROOT / "app/src/main/java/com/studyforge/app/viewmodel/LibraryViewModel.kt"
DAO = ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt"

app = APP.read_text(encoding="utf-8")
paged_ui = PAGED_UI.read_text(encoding="utf-8")
repo = REPO.read_text(encoding="utf-8")
vm = VM.read_text(encoding="utf-8")
dao = DAO.read_text(encoding="utf-8")
checks = {
    "Room PagingSource query exists": "fun pageLibraryRows(" in dao and "PagingSource<Int, QuestionLibraryRow>" in dao,
    "database count query exists": "suspend fun countLibraryRows(" in dao,
    "repository exposes Pager": "fun pageLibrary(query: LibraryPageQuery)" in repo and "Pager(" in repo,
    "criteria-driven ViewModel exists": "flatMapLatest" in vm and "cachedIn(viewModelScope)" in vm,
    "production Library no longer accepts full question list": not re.search(r"private fun LibraryScreen\(\s*questions:\s*List<QuestionEntity>", app),
    "production Library destination is wired to paged screen": "1 -> PagedLibraryScreen(" in app,
    "production Library collects LazyPagingItems": "collectAsLazyPagingItems" in paged_ui,
    "production Library no longer filters full list in Compose": "questions.filter { q -> QuestionLibraryFilter.matches" not in paged_ui,
    "Library saved-preset counts use Room count queries": "viewModel.countMatches(preset.toCriteria())" in paged_ui,
    "bulk selection stores stable IDs across loaded pages": "mutableStateOf(setOf<String>())" in paged_ui and "onBulkUpdate(selectedIds" in paged_ui,
    "global tag manager CRUD is restored": all(token in paged_ui for token in ("onAddTag", "onRenameTag", "onDeleteTag", "showTagManager")),
    "tag usage counts are database-backed": "countQuestionsUsingTag" in dao and "countTagUsage" in repo,
}
for label, ok in checks.items():
    print(f"{'PASS' if ok else 'BLOCKED'}: {label}")
failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("\nProduction Library migration is not complete. This is expected until the UI migration lands.")
    sys.exit(2)
print("\nProduction Library paging integration static gate passed.")
