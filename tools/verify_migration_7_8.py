#!/usr/bin/env python3
"""SQLite smoke test for StudyForge Room migration 7->8 (no Android SDK required)."""
from pathlib import Path
import re
import sqlite3

source = Path(__file__).parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt"
text = source.read_text(encoding="utf-8")
match = re.search(r"private val MIGRATION_7_8\s*=\s*object\s*:\s*Migration\(7,\s*8\).*?override fun migrate\(db: SupportSQLiteDatabase\)\s*\{(.*?)\n\s*\}\n\s*\}", text, re.S)
if not match:
    raise SystemExit("Could not locate MIGRATION_7_8 in StudyDatabase.kt")
statements = re.findall(r'db\.execSQL\("((?:\\.|[^"\\])*)"\)', match.group(1))
if len(statements) != 7:
    raise SystemExit(f"Expected 7 migration SQL statements, found {len(statements)}")
statements = [bytes(sql, "utf-8").decode("unicode_escape") for sql in statements]

db = sqlite3.connect(":memory:")
db.execute("PRAGMA foreign_keys = ON")
db.execute("CREATE TABLE questions (id TEXT NOT NULL PRIMARY KEY)")
db.execute("INSERT INTO questions(id) VALUES ('q1')")
for sql in statements:
    db.execute(sql)

# 1–3: tables and expected indexes exist.
for table in ("question_content_blocks", "question_parts", "study_session_presets"):
    assert db.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()
assert db.execute("SELECT 1 FROM sqlite_master WHERE type='index' AND name='index_question_content_blocks_questionId_position'").fetchone()
assert db.execute("SELECT 1 FROM sqlite_master WHERE type='index' AND name='index_question_parts_questionId_position'").fetchone()
# 4–5: question-owned records insert and cascade-delete correctly.
db.execute("INSERT INTO question_content_blocks VALUES ('b1','q1',0,'text','hello','','','')")
db.execute("INSERT INTO question_parts VALUES ('p1','q1',0,'part','answer','',2.0,'[]')")
db.execute("DELETE FROM questions WHERE id='q1'")
assert db.execute("SELECT COUNT(*) FROM question_content_blocks").fetchone()[0] == 0
assert db.execute("SELECT COUNT(*) FROM question_parts").fetchone()[0] == 0
# 6: duplicate positions for one question are rejected.
db.execute("INSERT INTO questions(id) VALUES ('q2')")
db.execute("INSERT INTO question_content_blocks VALUES ('b2','q2',0,'text','a','','','')")
try:
    db.execute("INSERT INTO question_content_blocks VALUES ('b3','q2',0,'text','b','','','')")
    raise AssertionError("Duplicate content-block position was accepted")
except sqlite3.IntegrityError:
    pass
# 7: session preset can be stored.
db.execute("INSERT INTO study_session_presets VALUES ('s1','MDCAT practice','Practice','','','','','All','MDCAT',20,1,1)")
assert db.execute("SELECT COUNT(*) FROM study_session_presets").fetchone()[0] == 1
print("Migration 7->8 SQLite smoke test: 7 checks passed")
