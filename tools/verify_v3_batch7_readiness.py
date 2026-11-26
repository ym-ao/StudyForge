#!/usr/bin/env python3
"""Static source gate for V3.0 Batch 7 lazy rich-content hydration and benchmarks."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
def read(path): return (root / path).read_text()
db = read('app/src/main/java/com/studyforge/app/data/StudyDatabase.kt')
app = read('app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt')
screens = read('app/src/main/java/com/studyforge/app/ui/StudyScreens.kt')
tests = read('app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt')
workflow = read('.github/workflows/android-apk.yml')
benchmark = read('tools/benchmark_v3_scalability.py')
checks = [
    ('root app no longer observes all rich content blocks', 'observeContentBlocks()' not in app and 'allContentBlocks' not in app),
    ('root app no longer observes all multipart question parts', 'observeQuestionParts()' not in app and 'allQuestionParts' not in app),
    ('active Study card loads only its own ordered rich blocks', 'loadContentBlocks = { questionId -> dao.getContentBlocks(questionId) }' in app and 'activeQuestionBlocks = runCatching { loadContentBlocks(q.id).sortedBy { it.position } }' in screens),
    ('exam multipart parts are hydrated only for bounded active queue IDs', 'getQuestionPartsForQuestions(questionIds: List<String>)' in db and 'loadQuestionParts(questions.map { it.id })' in screens),
    ('exam start waits for multipart hydration and exposes load errors', 'enabled = !partsLoading && partsLoadError == null && eligible.isNotEmpty()' in screens),
    ('Room runtime test verifies per-question and bounded multi-question content reads', 'richContentHydrationIsScopedToActiveQuestionAndExamSnapshot' in tests and 'getQuestionPartsForQuestions(listOf("lazy-b"))' in tests),
    ('synthetic 10k/50k SQLite benchmark covers paging, queue, collection, recent history and rich content', all(x in benchmark for x in ('10_000, 50_000', 'library_first_page', 'due_queue_snapshot', 'collection_first_page', 'recent_reviews', 'active_card_content'))),
    ('benchmark reports EXPLAIN QUERY PLAN and labels host-only limitations', 'EXPLAIN QUERY PLAN' in benchmark and 'NOT an Android/Room/device' in benchmark),
    ('CI executes the benchmark and Batch 7 source gate', 'tools/benchmark_v3_scalability.py' in workflow and 'tools/verify_v3_batch7_readiness.py' in workflow),
]
for label, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + label)
assert all(ok for _, ok in checks), 'V3 Batch 7 readiness failed'
print(f'V3 Batch 7 source readiness: {len(checks)}/{len(checks)} checks passed')
