#!/usr/bin/env python3
"""Static source gate for V3.0 Batch 8 query-plan-driven index migration."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
def read(path): return (root / path).read_text()
db = read('app/src/main/java/com/studyforge/app/data/StudyDatabase.kt')
workflow = read('.github/workflows/android-apk.yml')
runtime = read('app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt')
benchmark = read('tools/benchmark_v3_scalability.py')
project = read('PROJECT_PLAN.md')
checks = [
    ('Room schema advanced additively to 16', 'version = 16,' in db and 'MIGRATION_15_16' in db),
    ('questions entity declares descending Library sort index', 'Index(value = ["updatedAt", "id"], orders = [Index.Order.DESC, Index.Order.ASC])' in db),
    ('questions entity declares Due ordering index', 'Index(value = ["dueAt", "id"])' in db),
    ('migration creates both indexes without table rebuild', 'CREATE INDEX IF NOT EXISTS index_questions_updatedAt_id' in db and 'CREATE INDEX IF NOT EXISTS index_questions_dueAt_id' in db),
    ('Due snapshot preserves null-first semantics with index-compatible order', 'ORDER BY dueAt ASC, id ASC LIMIT :limit' in db and 'ORDER BY CASE WHEN dueAt IS NULL THEN 0 ELSE 1 END, dueAt ASC' not in db),
    ('host benchmark mirrors indexes and checks query plans', 'index_questions_updatedAt_id' in benchmark and 'index_questions_dueAt_id' in benchmark and 'EXPLAIN QUERY PLAN' in benchmark),
    ('Android Room test checks indexes and null-first Due order', 'currentSchemaHasQueryPlanDrivenLibraryAndDueIndexes' in runtime and 'index_questions_updatedAt_id' in runtime and 'index_questions_dueAt_id' in runtime),
    ('migration and Batch 8 gates are wired into CI', 'verify_migration_15_16.py' in workflow and 'verify_v3_batch8_readiness.py' in workflow),
    ('current project baseline records Room 16 and backup 15 separately', 'Current Room database schema version 16' in project and 'Backup JSON schema version 15' in project),
]
for label, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + label)
assert all(ok for _, ok in checks), 'V3 Batch 8 readiness failed'
print(f'V3 Batch 8 source readiness: {len(checks)}/{len(checks)} checks passed')
