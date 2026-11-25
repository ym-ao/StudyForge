#!/usr/bin/env python3
"""Execute the exact Room migration 10 -> 11 SQL against SQLite."""
import re, sqlite3
from pathlib import Path
source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_10_11 = object : Migration\(10, 11\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 10->11 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 1

db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE study_session_presets (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, mode TEXT NOT NULL, subject TEXT NOT NULL, chapter TEXT NOT NULL, topic TEXT NOT NULL, tag TEXT NOT NULL, difficulty TEXT NOT NULL, exam TEXT NOT NULL, questionLimit INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, questionType TEXT NOT NULL DEFAULT 'All', status TEXT NOT NULL DEFAULT 'All', performance TEXT NOT NULL DEFAULT 'All')")
db.execute("INSERT INTO study_session_presets VALUES ('s1','Old session','Practice','','','','','All','',20,1,1,'All','All','All')")
for statement in sql: db.execute(statement)
assert db.execute("SELECT name, collectionId FROM study_session_presets WHERE id='s1'").fetchone() == ("Old session", "")
db.execute("UPDATE study_session_presets SET collectionId='c1' WHERE id='s1'")
assert db.execute("SELECT collectionId FROM study_session_presets WHERE id='s1'").fetchone()[0] == "c1"
print("PASS: exact migration 10->11 SQL preserves sessions and adds collection filtering")
