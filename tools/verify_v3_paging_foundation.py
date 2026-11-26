#!/usr/bin/env python3
"""Static + SQLite execution checks for the first V3.0 paging foundation batch."""
from pathlib import Path
import re
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
db = (ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
repo = (ROOT / "app/src/main/java/com/studyforge/app/data/QuestionRepository.kt").read_text()
vm = (ROOT / "app/src/main/java/com/studyforge/app/viewmodel/LibraryViewModel.kt").read_text()
ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/PagedQuestionRows.kt").read_text()
gradle = (ROOT / "app/build.gradle.kts").read_text()
test = (ROOT / "app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt").read_text()

assert 'androidx.paging:paging-runtime-ktx:3.3.6' in gradle
assert 'androidx.paging:paging-compose:3.3.6' in gradle
assert 'androidx.room:room-paging:2.8.5' in gradle
assert 'PagingSource<Int, QuestionLibraryRow>' in db
assert 'ORDER BY updatedAt DESC, id ASC' in db
assert 'answer' not in re.search(r'data class QuestionLibraryRow\((.*?)\n\)', db, re.S).group(1)
assert 'latex' not in re.search(r'data class QuestionLibraryRow\((.*?)\n\)', db, re.S).group(1)
assert 'QuestionRepository' in vm and 'flatMapLatest' in vm and 'cachedIn(viewModelScope)' in vm
assert 'itemKey { it.id }' in ui and 'LoadState.Error' in ui
assert 'MAX_STUDY_SESSION_SIZE = 500' in repo
assert 'chunked(SQLITE_SAFE_BIND_CHUNK)' in repo
assert 'pagedLibraryUsesLightweightRowsStableOrderingAndDatabaseFilters' in test
assert 'studyQueueSnapshotHasStableIdsAndLoadsCardsInSnapshotOrder' in test
assert 'source.invalid' in test
assert 'versionCode = 28' in gradle and 'versionName = "2.1.24"' in gradle

# Execute the actual Room SQL strings in SQLite, using a schema matching the fields referenced.
queries = re.findall(r'@Query\("""\s*(.*?)\s*"""\)', db, re.S)
page_sql = next(q for q in queries if 'SELECT id, prompt, subject, chapter, topic' in q)
count_sql = next(q for q in queries if 'SELECT COUNT(*) FROM questions' in q and 'collectionId' in q)
con = sqlite3.connect(':memory:')
con.execute('''CREATE TABLE questions (
 id TEXT PRIMARY KEY, prompt TEXT, answer TEXT, explanation TEXT, subject TEXT, chapter TEXT,
 topic TEXT, type TEXT, tagsCsv TEXT, status TEXT, difficulty TEXT, exam TEXT, sourceTitle TEXT,
 sourceDetail TEXT, createdAt INTEGER, updatedAt INTEGER, reviewCount INTEGER, correctCount INTEGER,
 incorrectCount INTEGER, fsrsStability REAL, fsrsDifficulty REAL, dueAt INTEGER)''')
con.execute('CREATE TABLE question_collection (questionId TEXT, collectionId TEXT)')
con.executemany('''INSERT INTO questions VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)''', [
 ('b','Alpha','hidden answer','', 'Biology','','','Flashcard','cell,energy','Draft','Easy','','', '',1,200,0,0,0,0.0,0.0,None),
 ('a','Alpha','another hidden answer','', 'Biology','','','MCQ','cell','Draft','Easy','','', '',1,200,0,0,0,0.0,0.0,None),
 ('c','Beta','Gamma','', 'Physics','','','Flashcard','','Draft','Easy','','', '',1,100,0,0,0,0.0,0.0,None),
])
params = dict(query='', type='All', status='All', difficulty='All', subject='', chapter='', topic='', tag='', exam='', sourceTitle='', sourceDetail='', createdAfter='', createdBefore='', updatedAfter='', updatedBefore='', collectionId='', performance='All')
rows = con.execute(page_sql, params).fetchall()
assert [r[0] for r in rows] == ['a','b','c'], [r[0] for r in rows]
assert con.execute(count_sql, params).fetchone()[0] == 3
params['subject'] = 'Bio'
assert con.execute(count_sql, params).fetchone()[0] == 2
params.update(subject='', query='another hidden answer')
assert con.execute(count_sql, params).fetchone()[0] == 1
params.update(query='', tag='energy')
assert con.execute(count_sql, params).fetchone()[0] == 1
print('PASS: V3 paging dependencies, DAO/repository/ViewModel/UI contracts, stable queue tests, and SQLite page/count SQL checks')
