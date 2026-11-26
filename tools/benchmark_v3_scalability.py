#!/usr/bin/env python3
"""Reproducible SQLite workload benchmark for StudyForge V3.0.

This is a host-Python synthetic database benchmark, NOT an Android/Room/device
performance certification. It mirrors the current Room entity indexes and
representative bounded queries so query-plan regressions are visible early.
"""
from __future__ import annotations

import argparse
import json
import sqlite3
import statistics
import tempfile
import time
from pathlib import Path

DAY = 86_400_000
NOW = 1_800_000_000_000

SCHEMA = """
PRAGMA journal_mode=OFF;
PRAGMA synchronous=OFF;
PRAGMA foreign_keys=ON;
CREATE TABLE questions (
 id TEXT PRIMARY KEY, prompt TEXT NOT NULL, answer TEXT NOT NULL,
 subject TEXT NOT NULL, chapter TEXT NOT NULL, topic TEXT NOT NULL,
 type TEXT NOT NULL, status TEXT NOT NULL, difficulty TEXT NOT NULL,
 tagsCsv TEXT NOT NULL, updatedAt INTEGER NOT NULL, dueAt INTEGER,
 reviewCount INTEGER NOT NULL, correctCount INTEGER NOT NULL, incorrectCount INTEGER NOT NULL
);
CREATE INDEX index_questions_updatedAt_id ON questions(updatedAt DESC, id ASC);
CREATE INDEX index_questions_dueAt_id ON questions(dueAt ASC, id ASC);
CREATE TABLE collections (id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE question_collection (questionId TEXT NOT NULL, collectionId TEXT NOT NULL,
 PRIMARY KEY(questionId, collectionId));
CREATE INDEX index_question_collection_collectionId ON question_collection(collectionId);
CREATE TABLE review_logs (id TEXT PRIMARY KEY, questionId TEXT NOT NULL,
 reviewedAt INTEGER NOT NULL, rating TEXT NOT NULL, wasCorrect INTEGER);
CREATE INDEX index_review_logs_questionId ON review_logs(questionId);
CREATE INDEX index_review_logs_reviewedAt ON review_logs(reviewedAt);
CREATE TABLE question_content_blocks (id TEXT PRIMARY KEY, questionId TEXT NOT NULL,
 position INTEGER NOT NULL, kind TEXT NOT NULL, text TEXT NOT NULL, mediaBase64 TEXT NOT NULL);
CREATE INDEX index_question_content_blocks_questionId ON question_content_blocks(questionId);
CREATE UNIQUE INDEX index_question_content_blocks_questionId_position
 ON question_content_blocks(questionId, position);
CREATE TABLE question_parts (id TEXT PRIMARY KEY, questionId TEXT NOT NULL,
 position INTEGER NOT NULL, prompt TEXT NOT NULL, answer TEXT NOT NULL);
CREATE INDEX index_question_parts_questionId ON question_parts(questionId);
CREATE UNIQUE INDEX index_question_parts_questionId_position
 ON question_parts(questionId, position);
"""

QUERIES = {
    "library_first_page": "SELECT id,prompt,subject,chapter,topic,type,status,difficulty,updatedAt FROM questions WHERE status=? ORDER BY updatedAt DESC,id ASC LIMIT 30",
    "due_queue_snapshot": "SELECT id FROM questions WHERE dueAt IS NULL OR dueAt<=? ORDER BY dueAt ASC,id ASC LIMIT 500",
    "collection_first_page": "SELECT q.id,q.prompt FROM questions q WHERE EXISTS (SELECT 1 FROM question_collection qc WHERE qc.questionId=q.id AND qc.collectionId=?) ORDER BY q.updatedAt DESC,q.id ASC LIMIT 30",
    "recent_reviews": "SELECT l.id,l.questionId,l.reviewedAt,l.rating,q.prompt FROM review_logs l LEFT JOIN questions q ON q.id=l.questionId ORDER BY l.reviewedAt DESC,l.id DESC LIMIT 8",
    "active_card_content": "SELECT id,position,kind,text,mediaBase64 FROM question_content_blocks WHERE questionId=? ORDER BY position",
}


def build_database(path: Path, count: int) -> sqlite3.Connection:
    db = sqlite3.connect(path)
    db.executescript(SCHEMA)
    db.execute("INSERT INTO collections VALUES ('collection-0','Benchmark collection')")
    questions, links, logs, blocks, parts = [], [], [], [], []
    media = "A" * 1024
    for i in range(count):
        qid = f"q-{i:07d}"
        due = None if i % 7 == 0 else NOW - ((i % 30) * DAY)
        questions.append((qid, f"Prompt {i}", f"Answer {i}", f"Subject {i % 12}",
                          f"Chapter {i % 80}", f"Topic {i % 240}",
                          ("MCQ", "Flashcard", "Numerical", "Cloze")[i % 4],
                          ("Active", "Draft", "Archived")[i % 3],
                          ("Easy", "Medium", "Hard")[i % 3],
                          f"tag-{i % 40},common", NOW - i * 1000, due,
                          i % 20, i % 11, i % 9))
        if i % 5 == 0:
            links.append((qid, "collection-0"))
        if i % 2 == 0:
            logs.append((f"r-{i:07d}", qid, NOW - (i % 365) * DAY, "Good", i % 2))
        if i % 20 == 0:
            blocks.append((f"b-{i:07d}", qid, 0, "IMAGE" if i % 40 == 0 else "LATEX", "rich payload", media if i % 40 == 0 else ""))
        if i % 50 == 0:
            parts.append((f"p-{i:07d}", qid, 0, "Part prompt", "Part answer"))
    with db:
        db.executemany("INSERT INTO questions VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", questions)
        db.executemany("INSERT INTO question_collection VALUES (?,?)", links)
        db.executemany("INSERT INTO review_logs VALUES (?,?,?,?,?)", logs)
        db.executemany("INSERT INTO question_content_blocks VALUES (?,?,?,?,?,?)", blocks)
        db.executemany("INSERT INTO question_parts VALUES (?,?,?,?,?)", parts)
    return db


def median_ms(db: sqlite3.Connection, sql: str, params: tuple, repeats: int = 7) -> tuple[float, int]:
    samples = []
    rows = 0
    for _ in range(repeats):
        start = time.perf_counter()
        result = db.execute(sql, params).fetchall()
        samples.append((time.perf_counter() - start) * 1000)
        rows = len(result)
    return statistics.median(samples), rows


def benchmark_size(count: int) -> dict:
    with tempfile.TemporaryDirectory(prefix="studyforge-v3-bench-") as temp:
        db = build_database(Path(temp) / "benchmark.db", count)
        result = {"questions": count, "checks": {}, "queries": {}, "query_plans": {}}
        actual = db.execute("SELECT COUNT(*) FROM questions").fetchone()[0]
        result["checks"]["all_questions_present"] = actual == count
        result["checks"]["unique_question_ids"] = db.execute("SELECT COUNT(DISTINCT id) FROM questions").fetchone()[0] == count
        result["checks"]["bounded_content_rows"] = db.execute("SELECT COUNT(*) FROM question_content_blocks WHERE questionId=?", ("q-0000000",)).fetchone()[0] == 1
        params = {
            "library_first_page": ("Active",),
            "due_queue_snapshot": (NOW,),
            "collection_first_page": ("collection-0",),
            "recent_reviews": (),
            "active_card_content": ("q-0000000",),
        }
        for name, sql in QUERIES.items():
            elapsed, rows = median_ms(db, sql, params[name])
            result["queries"][name] = {"median_ms": round(elapsed, 3), "rows_returned": rows}
            result["query_plans"][name] = [row[3] for row in db.execute("EXPLAIN QUERY PLAN " + sql, params[name]).fetchall()]
        # Batch 8 regression gates: the hot first-page and due queries must use
        # the intended indexes and must not allocate a temporary ORDER BY tree.
        library_plan = " ".join(result["query_plans"]["library_first_page"])
        due_plan = " ".join(result["query_plans"]["due_queue_snapshot"])
        collection_plan = " ".join(result["query_plans"]["collection_first_page"])
        result["checks"]["library_uses_sort_index"] = "index_questions_updatedAt_id" in library_plan and "TEMP B-TREE" not in library_plan.upper()
        result["checks"]["due_uses_order_index"] = "index_questions_dueAt_id" in due_plan and "TEMP B-TREE" not in due_plan.upper()
        result["checks"]["collection_uses_ordered_question_scan"] = "index_questions_updatedAt_id" in collection_plan and "TEMP B-TREE" not in collection_plan.upper()
        # Queue/page result bounds and deterministic order are functional checks.
        queue = db.execute(QUERIES["due_queue_snapshot"], (NOW,)).fetchall()
        result["checks"]["queue_is_bounded"] = len(queue) <= 500
        result["checks"]["queue_ids_unique"] = len({row[0] for row in queue}) == len(queue)
        result["checks"]["all_checks_pass"] = all(result["checks"].values())
        db.close()
        return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sizes", nargs="+", type=int, default=[10_000, 50_000])
    args = parser.parse_args()
    output = {"benchmark": "StudyForge V3.0 synthetic SQLite workload",
              "environment": "host Python sqlite3; not Android Room/device measurements",
              "results": [benchmark_size(size) for size in args.sizes]}
    print(json.dumps(output, indent=2))
    if not all(item["checks"]["all_checks_pass"] for item in output["results"]):
        raise SystemExit("Synthetic benchmark functional checks failed")


if __name__ == "__main__":
    main()
