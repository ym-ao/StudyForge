#!/usr/bin/env python3
"""SQLite regression test for additive Room 9->10 exam-attempt migration."""
import sqlite3

db = sqlite3.connect(":memory:")
db.execute("PRAGMA foreign_keys=ON")
db.execute("CREATE TABLE questions (id TEXT PRIMARY KEY, prompt TEXT NOT NULL)")
db.execute("CREATE TABLE review_logs (id TEXT PRIMARY KEY, questionId TEXT NOT NULL, reviewedAt INTEGER NOT NULL, FOREIGN KEY(questionId) REFERENCES questions(id) ON DELETE CASCADE)")
db.execute("INSERT INTO questions VALUES ('q-old','Existing question')")
db.execute("INSERT INTO review_logs VALUES ('r-old','q-old',123)")
# Exact SQL from MIGRATION_9_10.
db.execute("CREATE TABLE IF NOT EXISTS exam_attempts (id TEXT NOT NULL, startedAt INTEGER NOT NULL, completedAt INTEGER NOT NULL, durationSeconds INTEGER NOT NULL, questionCount INTEGER NOT NULL, answeredCount INTEGER NOT NULL, markedCount INTEGER NOT NULL, earnedPoints REAL NOT NULL, possiblePoints REAL NOT NULL, scorePercent REAL NOT NULL, itemsJson TEXT NOT NULL, label TEXT NOT NULL, PRIMARY KEY(id))")
db.execute("CREATE INDEX IF NOT EXISTS index_exam_attempts_completedAt ON exam_attempts(completedAt)")
db.execute("INSERT INTO exam_attempts VALUES ('a1',100,200,100,2,1,1,1.5,2.0,75.0,'[]','Timed exam')")
assert db.execute("SELECT prompt FROM questions WHERE id='q-old'").fetchone() == ('Existing question',)
assert db.execute("SELECT id,questionId,reviewedAt FROM review_logs").fetchall() == [('r-old','q-old',123)]
assert db.execute("SELECT scorePercent,itemsJson FROM exam_attempts WHERE id='a1'").fetchone() == (75.0,'[]')
# No question FK: snapshots survive library deletion.
db.execute("DELETE FROM questions WHERE id='q-old'")
assert db.execute("SELECT COUNT(*) FROM exam_attempts").fetchone()[0] == 1
assert not db.execute("PRAGMA foreign_key_list(exam_attempts)").fetchall()
print("PASS: migration 9->10 creates indexed attempt history, preserves existing rows, and keeps attempts independent of question deletion")
