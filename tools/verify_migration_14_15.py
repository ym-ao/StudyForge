#!/usr/bin/env python3
"""Execute exact Room migration 14 -> 15 SQL against SQLite."""
import re, sqlite3
from pathlib import Path
source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_14_15 = object : Migration\(14, 15\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 14->15 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 4
db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE saved_filter_presets (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
db.execute("INSERT INTO saved_filter_presets VALUES ('p1','Existing')")
for statement in sql: db.execute(statement)
columns = {row[1] for row in db.execute("PRAGMA table_info(saved_filter_presets)")}
expected = {"createdAfter", "createdBefore", "updatedAfter", "updatedBefore"}
assert expected.issubset(columns)
assert db.execute("SELECT name, createdAfter, createdBefore, updatedAfter, updatedBefore FROM saved_filter_presets WHERE id='p1'").fetchone() == ("Existing", "", "", "", "")
print("PASS: exact migration 14->15 preserves existing presets and adds all four date-range fields")
