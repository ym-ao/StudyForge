#!/usr/bin/env python3
"""Execute the exact additive Room 15 -> 16 index migration on populated SQLite data."""
import re
import sqlite3
from pathlib import Path

source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_15_16 = object : Migration\(15, 16\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 15->16 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 2, f"expected two additive index statements, found {len(sql)}"

db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE questions (id TEXT NOT NULL PRIMARY KEY, updatedAt INTEGER NOT NULL, dueAt INTEGER)")
rows = [(f"q-{i:05d}", 1_800_000_000_000 - i, None if i % 7 == 0 else 1_700_000_000_000 + i) for i in range(250)]
db.executemany("INSERT INTO questions VALUES (?, ?, ?)", rows)
for statement in sql:
    db.execute(statement)

indexes = {row[1] for row in db.execute("PRAGMA index_list('questions')")}
assert {"index_questions_updatedAt_id", "index_questions_dueAt_id"}.issubset(indexes)
assert db.execute("SELECT COUNT(*) FROM questions").fetchone()[0] == len(rows)
assert db.execute("SELECT COUNT(DISTINCT id) FROM questions").fetchone()[0] == len(rows)
ordered = db.execute("SELECT id FROM questions ORDER BY updatedAt DESC, id ASC LIMIT 5").fetchall()
assert ordered == [(f"q-{i:05d}",) for i in range(5)]
due = db.execute("SELECT id FROM questions WHERE dueAt IS NULL OR dueAt <= ? ORDER BY dueAt ASC, id ASC LIMIT 20", (1_800_000_000_000,)).fetchall()
assert len(due) == 20
plan_library = " ".join(row[3] for row in db.execute("EXPLAIN QUERY PLAN SELECT id FROM questions ORDER BY updatedAt DESC, id ASC LIMIT 30"))
plan_due = " ".join(row[3] for row in db.execute("EXPLAIN QUERY PLAN SELECT id FROM questions WHERE dueAt IS NULL OR dueAt <= ? ORDER BY dueAt ASC, id ASC LIMIT 20", (1_800_000_000_000,)))
assert "index_questions_updatedAt_id" in plan_library and "TEMP B-TREE" not in plan_library.upper(), plan_library
assert "index_questions_dueAt_id" in plan_due and "TEMP B-TREE" not in plan_due.upper(), plan_due
print("PASS: exact migration 15->16 adds both indexes without losing populated question rows")
print("PASS: Library and Due query plans use their intended indexes without temporary sorting")
