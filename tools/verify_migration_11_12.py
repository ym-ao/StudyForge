#!/usr/bin/env python3
"""Execute the exact Room migration 11 -> 12 SQL against SQLite."""
import re, sqlite3
from pathlib import Path
source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_11_12 = object : Migration\(11, 12\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 11->12 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 2

db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE questions (id TEXT NOT NULL PRIMARY KEY, prompt TEXT NOT NULL)")
db.execute("INSERT INTO questions VALUES ('q1','current')")
db.execute("CREATE TABLE study_session_presets (id TEXT NOT NULL PRIMARY KEY, collectionId TEXT NOT NULL DEFAULT '')")
db.execute("INSERT INTO study_session_presets VALUES ('s1','c1')")
for statement in sql: db.execute(statement)
db.execute("INSERT INTO question_versions VALUES ('v1','q1',1,1,'{}')")
assert db.execute("SELECT COUNT(*) FROM question_versions").fetchone()[0] == 1
assert db.execute("SELECT prompt FROM questions WHERE id='q1'").fetchone()[0] == 'current'
assert db.execute("SELECT collectionId FROM study_session_presets WHERE id='s1'").fetchone()[0] == 'c1'
print("PASS: exact migration 11->12 SQL adds indexed version history without changing existing tables")
