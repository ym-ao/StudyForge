#!/usr/bin/env python3
"""Execute the exact Room migration 12 -> 13 SQL against SQLite."""
import re, sqlite3
from pathlib import Path
source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_12_13 = object : Migration\(12, 13\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 12->13 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 2

db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE questions (id TEXT NOT NULL PRIMARY KEY, prompt TEXT NOT NULL)")
db.execute("INSERT INTO questions VALUES ('q1','preserved question')")
for statement in sql: db.execute(statement)
columns = {row[1] for row in db.execute("PRAGMA table_info(questions)")}
assert {"sourceTitle", "sourceDetail"}.issubset(columns)
row = db.execute("SELECT prompt, sourceTitle, sourceDetail FROM questions WHERE id='q1'").fetchone()
assert row == ("preserved question", "", "")
db.execute("UPDATE questions SET sourceTitle='Textbook', sourceDetail='Chapter 2 page 14' WHERE id='q1'")
assert db.execute("SELECT sourceTitle, sourceDetail FROM questions WHERE id='q1'").fetchone() == ("Textbook", "Chapter 2 page 14")
print("PASS: exact migration 12->13 adds source fields, preserves existing question data, and supports provenance values")
