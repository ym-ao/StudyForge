#!/usr/bin/env python3
"""Execute exact Room migration 13 -> 14 SQL against SQLite."""
import re, sqlite3
from pathlib import Path
source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
body = re.search(r"private val MIGRATION_13_14 = object : Migration\(13, 14\).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n            }", source, re.S)
assert body, "migration 13->14 not found"
sql = re.findall(r'db\.execSQL\("([^"]+)"\)', body.group(1))
assert len(sql) == 2
db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE saved_filter_presets (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
db.execute("INSERT INTO saved_filter_presets VALUES ('p1','Existing')")
for statement in sql: db.execute(statement)
columns = {row[1] for row in db.execute("PRAGMA table_info(saved_filter_presets)")}
assert {"sourceTitle", "sourceDetail"}.issubset(columns)
assert db.execute("SELECT name, sourceTitle, sourceDetail FROM saved_filter_presets WHERE id='p1'").fetchone() == ("Existing", "", "")
db.execute("UPDATE saved_filter_presets SET sourceTitle='Biology', sourceDetail='Page 82' WHERE id='p1'")
assert db.execute("SELECT sourceTitle, sourceDetail FROM saved_filter_presets WHERE id='p1'").fetchone() == ("Biology", "Page 82")
print("PASS: exact migration 13->14 preserves existing presets and adds source filter fields")
