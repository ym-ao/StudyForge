#!/usr/bin/env python3
"""Static source gate for V3.0 Batch 6 Progress query migration."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
def read(path): return (root / path).read_text()
db = read('app/src/main/java/com/studyforge/app/data/StudyDatabase.kt')
app = read('app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt')
screens = read('app/src/main/java/com/studyforge/app/ui/StudyScreens.kt')
tests = read('app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt')
checks = [
    ('type accuracy grouped in Room', 'observeProgressTypeAccuracy' in db and 'GROUP BY label' in db),
    ('weak topic/subject/chapter aggregates in Room', all(x in db for x in ('observeProgressWeakTopics', 'observeProgressWeakSubjects', 'observeProgressWeakChapters'))),
    ('mistake category counts in Room', 'observeProgressMistakeCategories' in db),
    ('recent review history bounded to eight joined prompt rows', 'observeProgressRecentReviews' in db and 'ORDER BY l.reviewedAt DESC, l.id DESC LIMIT 8' in db),
    ('daily trend grouped in SQLite', 'observeProgressDailyReviews' in db and "date(reviewedAt / 1000, 'unixepoch', 'localtime')" in db),
    ('exam aggregates and median/trend in Room', all(x in db for x in ('observeProgressExamStats', 'medianScore', 'recentTrend', 'band80To100'))),
    ('exam history bounded to ten attempts', 'observeRecentExamAttempts' in db and 'ORDER BY completedAt DESC LIMIT 10' in db),
    ('daily Study quotas are scalar counts, not review-log lists', 'observeStudyDailyQuotaStats' in db and 'newReviewedToday: Int' in screens),
    ('root UI no longer observes full questions, review logs or exam attempts', all(x not in app for x in ('observeQuestions()', 'observeReviewLogs()', 'observeExamAttempts()'))),
    ('legacy CSV tags are discovered by a recursive Room query', 'observeQuestionTagNames' in db and 'WITH RECURSIVE tag_tokens' in db and 'TagText.names(questions)' not in app),
    ('Progress UI no longer receives full questions/review logs/exam history', 'fun ProgressScreen(\n    coreStats:' in screens and 'reviewLogs: List<ReviewLogEntity>' not in screens[screens.index('fun ProgressScreen('):screens.index('@Composable\nprivate fun ProgressStat')]),
    ('Room runtime test covers analytics, exam projections and SQL tag discovery', all(x in tests for x in ('progressAnalyticsUseBoundedDatabaseProjections', 'rootTagDiscoverySplitsLegacyCsvInDatabaseWithoutQuestionList'))),
    ('Study daily quota query is wired into production', 'observeStudyDailyQuotaStats(localCalendar)' in app),
]
for label, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + label)
assert all(ok for _, ok in checks), 'V3 Batch 6 readiness failed'
print(f'V3 Batch 6 source readiness: {len(checks)}/{len(checks)} checks passed')
