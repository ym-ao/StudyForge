#!/usr/bin/env python3
"""SQLite smoke test for the exact additive Room 8->9 migration SQL."""
import sqlite3

db = sqlite3.connect(":memory:")
db.execute("CREATE TABLE question_parts (id TEXT NOT NULL PRIMARY KEY, questionId TEXT NOT NULL, position INTEGER NOT NULL, prompt TEXT NOT NULL, answer TEXT NOT NULL, explanation TEXT NOT NULL, points REAL NOT NULL, optionsJson TEXT NOT NULL)")
db.execute("CREATE TABLE study_session_presets (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, mode TEXT NOT NULL, subject TEXT NOT NULL, chapter TEXT NOT NULL, topic TEXT NOT NULL, tag TEXT NOT NULL, difficulty TEXT NOT NULL, exam TEXT NOT NULL, questionLimit INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
db.execute("INSERT INTO question_parts VALUES ('p1','q1',0,'Prompt','42','',2.0,'[]')")
db.execute("INSERT INTO study_session_presets VALUES ('s1','Session','Practice','','','','','All','',20,1,1)")
for sql in [
    "ALTER TABLE question_parts ADD COLUMN absoluteTolerance REAL NOT NULL DEFAULT 0.0",
    "ALTER TABLE question_parts ADD COLUMN relativeTolerance REAL NOT NULL DEFAULT 0.0",
    "ALTER TABLE question_parts ADD COLUMN acceptedAnswers TEXT NOT NULL DEFAULT ''",
    "ALTER TABLE study_session_presets ADD COLUMN questionType TEXT NOT NULL DEFAULT 'All'",
    "ALTER TABLE study_session_presets ADD COLUMN status TEXT NOT NULL DEFAULT 'All'",
    "ALTER TABLE study_session_presets ADD COLUMN performance TEXT NOT NULL DEFAULT 'All'",
]: db.execute(sql)
assert db.execute("SELECT answer, points, absoluteTolerance, relativeTolerance, acceptedAnswers FROM question_parts WHERE id='p1'").fetchone() == ('42', 2.0, 0.0, 0.0, '')
assert db.execute("SELECT questionType,status,performance FROM study_session_presets WHERE id='s1'").fetchone() == ('All','All','All')
db.execute("UPDATE question_parts SET absoluteTolerance=0.05, relativeTolerance=0.01, acceptedAnswers='forty two' WHERE id='p1'")
assert db.execute("SELECT absoluteTolerance,relativeTolerance,acceptedAnswers FROM question_parts WHERE id='p1'").fetchone() == (0.05,0.01,'forty two')
print('PASS: migration 8->9 preserves rows and adds all six grading/session criteria columns')
