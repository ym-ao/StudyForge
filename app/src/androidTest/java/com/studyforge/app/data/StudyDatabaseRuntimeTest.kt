package com.studyforge.app.data

import android.content.Context
import androidx.room.Room
import androidx.paging.PagingSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the generated Room schema against a real Android SQLite runtime. */
@RunWith(AndroidJUnit4::class)
class StudyDatabaseRuntimeTest {
    private lateinit var context: Context
    private var database: StudyDatabase? = null
    private val databaseName = "studyforge-runtime-smoke.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun currentSchemaHasQueryPlanDrivenLibraryAndDueIndexes() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestion(QuestionEntity(id = "index-future", prompt = "Future", answer = "A", dueAt = 9_000L, updatedAt = 30L))
        dao.upsertQuestion(QuestionEntity(id = "index-overdue", prompt = "Overdue", answer = "B", dueAt = 1_000L, updatedAt = 20L))
        dao.upsertQuestion(QuestionEntity(id = "index-new", prompt = "New", answer = "C", dueAt = null, updatedAt = 10L))

        val db = database!!.openHelper.readableDatabase
        val indexNames = mutableSetOf<String>()
        db.query("PRAGMA index_list('questions')").use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) indexNames += cursor.getString(nameColumn)
        }
        assertTrue(indexNames.contains("index_questions_updatedAt_id"))
        assertTrue(indexNames.contains("index_questions_dueAt_id"))

        assertEquals(
            listOf("index-new", "index-overdue"),
            dao.snapshotStudyQueueIds("Due", 5_000L, 10)
        )
    }

    @Test
    fun currentSchemaPersistsAndReadsEveryCoreStudyRecord() = runBlocking {
        val firstOpen = openDatabase()
        val dao = firstOpen.dao()
        val question = QuestionEntity(
            id = "runtime-q1", prompt = "What is 2 + 2?", answer = "4",
            subject = "Mathematics", sourceTitle = "Runtime test", sourceDetail = "page 1"
        )
        dao.upsertQuestion(question)
        dao.upsertCollection(CollectionEntity("runtime-c1", "Runtime collection"))
        dao.insertLinks(listOf(QuestionCollectionCrossRef(question.id, "runtime-c1")))
        dao.upsertSettings(StudySettingsEntity(desiredRetention = 0.92))
        dao.upsertTag(TagEntity("runtime-t1", "runtime"))
        dao.upsertContentBlocks(listOf(QuestionContentBlockEntity(
            id = "runtime-b1", questionId = question.id, position = 0,
            kind = "TEXT", text = "A persisted block"
        )))
        dao.upsertQuestionParts(listOf(QuestionPartEntity(
            id = "runtime-p1", questionId = question.id, position = 0,
            prompt = "Part A", answer = "4", points = 2.0,
            absoluteTolerance = 0.01, acceptedAnswers = "four"
        )))
        dao.insertReviewLog(ReviewLogEntity(
            id = "runtime-r1", questionId = question.id, reviewedAt = 100L,
            rating = "Good", wasCorrect = true
        ))
        dao.upsertStudySessionPreset(StudySessionPresetEntity(
            id = "runtime-s1", name = "Runtime session", collectionId = "runtime-c1"
        ))
        dao.upsertExamAttempt(ExamAttemptEntity(
            id = "runtime-e1", startedAt = 100L, completedAt = 200L,
            durationSeconds = 100, questionCount = 1, answeredCount = 1,
            markedCount = 0, earnedPoints = 2.0, possiblePoints = 2.0,
            scorePercent = 100.0, itemsJson = "[]"
        ))
        dao.insertQuestionVersion(QuestionVersionEntity(
            id = "runtime-v1", questionId = question.id, versionNumber = 1,
            savedAt = 200L, snapshotJson = "{}"
        ))
        dao.upsertSavedFilterPreset(SavedFilterPresetEntity(
            id = "runtime-f1", name = "Runtime filter", sourceTitle = "Runtime test",
            createdAfter = "2026-01-01"
        ))

        assertNotNull(dao.getQuestion(question.id))
        assertEquals(listOf("runtime-c1"), dao.getCollectionIds(question.id))
        assertEquals(1, dao.getContentBlocks(question.id).size)
        assertEquals("A persisted block", dao.getContentBlocks(question.id).single().text)
        assertEquals(0.01, dao.getQuestionParts(question.id).single().absoluteTolerance, 0.0)
        assertEquals(1, dao.getAllReviewLogs().size)
        assertEquals(1, dao.getAllExamAttempts().size)
        assertEquals(1, dao.getAllQuestionVersions().size)
        assertEquals(1, dao.getAllTags().size)
        assertEquals(1, dao.getAllSavedFilterPresets().size)
        assertEquals(0.92, dao.getSettings()?.desiredRetention ?: 0.0, 0.0)

        firstOpen.close()
        database = null

        val reopened = openDatabase()
        val reopenedDao = reopened.dao()
        assertEquals("What is 2 + 2?", reopenedDao.getQuestion(question.id)?.prompt)
        assertEquals("Runtime collection", reopenedDao.getAllCollections().single().name)
        assertEquals("runtime", reopenedDao.getAllTags().single().name)
        assertTrue(reopenedDao.getAllExamAttempts().single().scorePercent == 100.0)
    }

    @Test
    fun dashboardAndProgressAggregatesAreComputedByRoom() = runBlocking {
        val dao = openDatabase().dao()
        val now = System.currentTimeMillis()
        dao.upsertCollection(CollectionEntity("aggregate-c1", "Aggregate collection"))
        dao.upsertQuestion(QuestionEntity(
            id = "aggregate-new", prompt = "New card", answer = "A",
            learningState = "New", dueAt = null, reviewCount = 2
        ))
        dao.upsertQuestion(QuestionEntity(
            id = "aggregate-mastered", prompt = "Mastered card", answer = "B",
            learningState = "Review", dueAt = now + 86_400_000L, fsrsStability = 30.0, reviewCount = 3
        ))
        dao.upsertQuestion(QuestionEntity(
            id = "aggregate-learning", prompt = "Learning card", answer = "C",
            learningState = "Learning", dueAt = now + 86_400_000L
        ))
        dao.insertReviewLog(ReviewLogEntity(
            id = "aggregate-log-1", questionId = "aggregate-new", reviewedAt = now,
            rating = "Good", wasCorrect = true
        ))
        dao.insertReviewLog(ReviewLogEntity(
            id = "aggregate-log-2", questionId = "aggregate-new", reviewedAt = now - 2L * 86_400_000L,
            rating = "Again", wasCorrect = false
        ))

        val home = dao.observeHomeDashboardStats(now).first()
        assertEquals(3, home.questionCount)
        assertEquals(1, home.collectionCount)
        assertEquals(5, home.reviewCount)
        assertEquals(1, home.dueCount)

        val progress = dao.observeProgressCoreStats(
            now, now - 30L * 86_400_000L, now - 7L * 86_400_000L
        ).first()
        assertEquals(3, progress.questionCount)
        assertEquals(1, progress.newCount)
        assertEquals(1, progress.learningCount)
        assertEquals(1, progress.masteredCount)
        assertEquals(2, progress.totalReviews)
        assertEquals(2, progress.scoredReviews)
        assertEquals(1, progress.correctReviews)
        assertEquals(2, progress.recentScoredReviews)
        assertEquals(1, progress.recentCorrectReviews)
        assertEquals(2, progress.lastSevenDays)
        assertEquals(2, progress.activeReviewDays)
    }

    @Test
    fun structuredBlockKindsAndOfflineImagePayloadPersistAcrossReopen() = runBlocking {
        val firstOpen = openDatabase()
        val dao = firstOpen.dao()
        val questionId = "rich-runtime-q"
        dao.upsertQuestion(QuestionEntity(id = questionId, prompt = "Rich content", answer = "Saved"))
        val blocks = listOf(
            QuestionContentBlockEntity(
                id = "rich-text", questionId = questionId, position = 0,
                kind = "TEXT", text = "A paragraph"
            ),
            QuestionContentBlockEntity(
                id = "rich-hint", questionId = questionId, position = 1,
                kind = "HINT", text = "A learner-revealed hint"
            ),
            QuestionContentBlockEntity(
                id = "rich-image", questionId = questionId, position = 2,
                kind = "IMAGE", mediaBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/pXcAAAAASUVORK5CYII=", mediaMimeType = "image/png",
                altText = "Offline diagram"
            ),
            QuestionContentBlockEntity(
                id = "rich-table", questionId = questionId, position = 3,
                kind = "TABLE", text = "x | y\n1 | 2"
            ),
            QuestionContentBlockEntity(
                id = "rich-graph", questionId = questionId, position = 4,
                kind = "GRAPH", text = "0,1\n1,2"
            )
        )
        dao.upsertContentBlocks(blocks)

        assertEquals(5, dao.getContentBlocks(questionId).size)
        firstOpen.close()
        database = null

        val reopened = openDatabase()
        val restored = reopened.dao().getContentBlocks(questionId).sortedBy { it.position }
        assertEquals(listOf("TEXT", "HINT", "IMAGE", "TABLE", "GRAPH"), restored.map { it.kind })
        assertEquals("A learner-revealed hint", restored[1].text)
        assertEquals("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/pXcAAAAASUVORK5CYII=", restored[2].mediaBase64)
        assertEquals("image/png", restored[2].mediaMimeType)
        assertEquals("Offline diagram", restored[2].altText)
        assertEquals("x | y\n1 | 2", restored[3].text)
        assertEquals("0,1\n1,2", restored[4].text)
    }


    @Test
    fun richContentHydrationIsScopedToActiveQuestionAndExamSnapshot() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestion(QuestionEntity(id = "lazy-a", prompt = "A", answer = "A"))
        dao.upsertQuestion(QuestionEntity(id = "lazy-b", prompt = "B", answer = "B"))
        dao.upsertContentBlocks(listOf(
            QuestionContentBlockEntity(id = "lazy-block-a", questionId = "lazy-a", position = 0, kind = "TEXT", text = "A only"),
            QuestionContentBlockEntity(id = "lazy-block-b", questionId = "lazy-b", position = 0, kind = "TEXT", text = "B only")
        ))
        dao.upsertQuestionParts(listOf(
            QuestionPartEntity(id = "lazy-part-a", questionId = "lazy-a", position = 0, prompt = "Part A", answer = "A"),
            QuestionPartEntity(id = "lazy-part-b", questionId = "lazy-b", position = 0, prompt = "Part B", answer = "B")
        ))

        assertEquals(listOf("lazy-block-a"), dao.getContentBlocks("lazy-a").map { it.id })
        assertEquals(listOf("lazy-part-a", "lazy-part-b"), dao.getQuestionPartsForQuestions(listOf("lazy-a", "lazy-b")).map { it.id })
        assertEquals(listOf("lazy-part-b"), dao.getQuestionPartsForQuestions(listOf("lazy-b")).map { it.id })
    }

    @Test
    fun clozeQuestionCanBeVersionedAndAddedToCollection() = runBlocking {
        val dao = openDatabase().dao()
        val question = QuestionEntity(
            id = "cloze-collection-q",
            prompt = "The powerhouse is {{c1::mitochondrion::organelle}} and it makes {{c2::ATP}}.",
            answer = "mitochondrion; ATP",
            type = "Cloze",
            latex = "E = mc^2"
        )
        dao.upsertQuestion(question)
        dao.upsertCollection(CollectionEntity("cloze-collection", "Cloze collection"))

        val oldIds = dao.getCollectionIds(question.id)
        val snapshot = QuestionVersionRules.createSnapshot(question, emptyList(), emptyList(), oldIds)
        dao.insertQuestionVersion(QuestionVersionEntity(
            id = "cloze-collection-version", questionId = question.id, versionNumber = 1,
            savedAt = System.currentTimeMillis(), snapshotJson = snapshot
        ))
        val newIds = CollectionMembershipRules.update(oldIds, "cloze-collection", add = true)
        dao.clearQuestionCollections(question.id)
        dao.insertLinks(newIds.map { QuestionCollectionCrossRef(question.id, it) })
        dao.upsertQuestion(question.copy(updatedAt = System.currentTimeMillis()))

        assertEquals(listOf("cloze-collection"), dao.getCollectionIds(question.id))
        val persisted = dao.getQuestion(question.id) ?: error("Cloze question was not persisted")
        assertEquals("Cloze", persisted.type)
        assertEquals(question.prompt, persisted.prompt)
        assertEquals(listOf("cloze-collection-version"), dao.getAllQuestionVersions().map { it.id })
        val restored = QuestionVersionRules.restore(snapshot, persisted)
        assertEquals(question.prompt, restored.question.prompt)
        assertEquals(listOf("cloze-collection"), newIds)
    }

    @Test
    fun deletingQuestionCascadesDependentContentAndReviewRows() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestion(QuestionEntity(id = "cascade-q", prompt = "p", answer = "a"))
        dao.upsertContentBlocks(listOf(QuestionContentBlockEntity(
            id = "cascade-b", questionId = "cascade-q", position = 0, kind = "TEXT"
        )))
        dao.upsertQuestionParts(listOf(QuestionPartEntity(
            id = "cascade-p", questionId = "cascade-q", position = 0, prompt = "p", answer = "a"
        )))
        dao.insertReviewLog(ReviewLogEntity(
            id = "cascade-r", questionId = "cascade-q", reviewedAt = 1L, rating = "Good"
        ))

        dao.deleteQuestion("cascade-q")

        assertTrue(dao.getContentBlocks("cascade-q").isEmpty())
        assertTrue(dao.getQuestionParts("cascade-q").isEmpty())
        assertTrue(dao.getAllReviewLogs().isEmpty())
    }

    @Test
    fun deletingCollectionClearsLinksAndBothKindsOfSavedPresetReferences() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestion(QuestionEntity(id = "collection-q", prompt = "p", answer = "a"))
        dao.upsertCollection(CollectionEntity("collection-c", "Collection to delete"))
        dao.insertLinks(listOf(QuestionCollectionCrossRef("collection-q", "collection-c")))
        dao.upsertSavedFilterPreset(SavedFilterPresetEntity(
            id = "collection-filter", name = "Filter", collectionId = "collection-c"
        ))
        dao.upsertStudySessionPreset(StudySessionPresetEntity(
            id = "collection-session", name = "Session", collectionId = "collection-c"
        ))

        // Mirrors the transaction used by the collection-delete UI. Saved-filter
        // references use Room's SET_NULL foreign key; session presets store a
        // string criterion and therefore require explicit cleanup first.
        dao.clearStudySessionCollection("collection-c")
        dao.deleteCollection("collection-c")

        assertTrue(dao.getCollectionIds("collection-q").isEmpty())
        assertTrue(dao.getAllSavedFilterPresets().single().collectionId == null)
        assertEquals("", dao.getAllStudySessionPresets().single().collectionId)
        assertTrue(dao.getAllCollections().isEmpty())
    }


    @Test
    fun pagedLibraryUsesLightweightRowsStableOrderingAndDatabaseFilters() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "page-b", prompt = "Alpha prompt", answer = "hidden answer", subject = "Biology", type = "Flashcard", updatedAt = 200L, tagsCsv = "cell,energy"),
            QuestionEntity(id = "page-a", prompt = "Alpha prompt", answer = "another hidden answer", subject = "Biology", type = "MCQ", updatedAt = 200L, tagsCsv = "cell"),
            QuestionEntity(id = "page-c", prompt = "Beta prompt", answer = "Gamma", subject = "Physics", type = "Flashcard", updatedAt = 100L)
        ))
        val source = dao.pageLibraryRows(
            query = "", type = "All", status = "All", difficulty = "All",
            subject = "", chapter = "", topic = "", tag = "", exam = "",
            sourceTitle = "", sourceDetail = "", createdAfter = "", createdBefore = "",
            updatedAfter = "", updatedBefore = "", collectionId = "", performance = "All"
        )
        val result = source.load(PagingSource.LoadParams.Refresh(null, 2, false))
        assertTrue(result is PagingSource.LoadResult.Page)
        val rows = (result as PagingSource.LoadResult.Page).data
        assertEquals(listOf("page-a", "page-b"), rows.map { it.id })
        assertEquals("Alpha prompt", rows.first().prompt)
        assertEquals(3, dao.countLibraryRows(
            "", "All", "All", "All", "", "", "", "", "", "", "",
            "", "", "", "", "", "All"
        ))
        assertEquals(2, dao.countLibraryRows(
            "", "flashcard", "All", "All", "", "", "", "", "", "", "",
            "", "", "", "", "", "All"
        ))
        assertEquals(2, dao.countLibraryRows(
            "", "All", "All", "All", "Biology", "", "", "", "", "", "",
            "", "", "", "", "", "All"
        ))
        assertEquals(1, dao.countLibraryRows(
            "another hidden answer", "All", "All", "All", "", "", "", "", "", "", "",
            "", "", "", "", "", "All"
        ))
        assertEquals(1, dao.countLibraryRows(
            "", "All", "All", "All", "", "", "", "energy", "", "", "",
            "", "", "", "", "", "All"
        ))
        // Preserve the previous Library's case-insensitive substring tag match.
        assertEquals(1, dao.countLibraryRows(
            "", "All", "All", "All", "", "", "", "NER", "", "", "",
            "", "", "", "", "", "All"
        ))
        assertEquals(2, dao.countQuestionsUsingTag("cell"))
        assertEquals("page-b", dao.getQuestionsUsingTag("energy").single().id)
        dao.upsertQuestion(QuestionEntity(id = "page-new", prompt = "Newest", answer = "answer", updatedAt = 300L))
        assertTrue("Room must invalidate a page when its observed table changes", source.invalid)
    }

    @Test
    fun studyQueueSnapshotHasStableIdsAndLoadsCardsInSnapshotOrder() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "queue-c", prompt = "C", answer = "C", dueAt = 300L),
            QuestionEntity(id = "queue-a", prompt = "A", answer = "A", dueAt = 100L),
            QuestionEntity(id = "queue-b", prompt = "B", answer = "B", dueAt = 200L)
        ))
        val repository = QuestionRepository(dao)
        val snapshot = repository.createStudyQueueSnapshot("Due", now = 250L, limit = 10)
        assertEquals(listOf("queue-a", "queue-b"), snapshot)
        // A review changes due/sort values after session creation; the snapshot order remains authoritative.
        dao.upsertQuestion(dao.getQuestion("queue-a")!!.copy(dueAt = 900L, updatedAt = 999L))
        val cards = repository.loadStudyQueueCards(snapshot)
        assertEquals(snapshot, cards.map { it.id })
        assertEquals(2, cards.size)
    }

    @Test
    fun studyQueueSnapshotAppliesPresetFiltersAndModeOrderingInDatabase() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertCollection(CollectionEntity("queue-collection", "Queue collection"))
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "sq-weak", prompt = "Weak", answer = "A", subject = "Math", chapter = "Algebra", topic = "Linear", tagsCsv = "exam,core", difficulty = "Hard", exam = "Board", type = "MCQ", status = "Verified", reviewCount = 3, correctCount = 0, incorrectCount = 3, dueAt = 100L, updatedAt = 100L),
            QuestionEntity(id = "sq-strong", prompt = "Strong", answer = "B", subject = "Math", chapter = "Algebra", topic = "Linear", tagsCsv = "core", difficulty = "Hard", exam = "Board", type = "MCQ", status = "Verified", reviewCount = 3, correctCount = 3, incorrectCount = 0, dueAt = 200L, updatedAt = 200L),
            QuestionEntity(id = "sq-other", prompt = "Other", answer = "C", subject = "Physics", chapter = "Waves", topic = "Sound", tagsCsv = "exam", difficulty = "Easy", exam = "School", type = "Flashcard", status = "Draft", dueAt = 50L, updatedAt = 300L)
        ))
        dao.insertLinks(listOf(QuestionCollectionCrossRef("sq-weak", "queue-collection"), QuestionCollectionCrossRef("sq-strong", "queue-collection")))
        val repository = QuestionRepository(dao)
        val weak = repository.createStudyQueueSnapshot(
            StudyQueueQuery(
                mode = "Weakest", subject = "Math", chapter = "Algebra", presetTopic = "Linear",
                tag = "core", difficulty = "Hard", exam = "Board", questionType = "MCQ",
                status = "Verified", collectionId = "queue-collection", performance = "Weak", limit = 20
            ), now = 250L
        )
        assertEquals(listOf("sq-weak"), weak)
        val due = repository.createStudyQueueSnapshot(StudyQueueQuery(mode = "Due", limit = 20), now = 150L)
        assertEquals(listOf("sq-other", "sq-weak"), due)
        val ordered = repository.createStudyQueueSnapshot(StudyQueueQuery(mode = "Ordered", limit = 20), now = 250L)
        assertEquals(listOf("sq-strong", "sq-weak", "sq-other"), ordered)
    }


    @Test
    fun dueStudyQueueSeparatesNewAndReviewCapsAndPreservesReviewOrder() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "due-new-a", prompt = "New A", answer = "A", learningState = "New", dueAt = 10L),
            QuestionEntity(id = "due-new-b", prompt = "New B", answer = "B", learningState = "New", dueAt = 20L),
            QuestionEntity(id = "due-review-weak", prompt = "Weak", answer = "W", learningState = "Review", incorrectCount = 5, dueAt = 30L),
            QuestionEntity(id = "due-review-strong", prompt = "Strong", answer = "S", learningState = "Review", incorrectCount = 1, dueAt = 5L),
            QuestionEntity(id = "not-due", prompt = "Future", answer = "F", learningState = "Review", dueAt = 900L)
        ))
        val repository = QuestionRepository(dao)
        val newIds = repository.createStudyQueueSnapshot(StudyQueueQuery(mode = "DueNew", limit = 1), now = 100L)
        val reviewIds = repository.createStudyQueueSnapshot(StudyQueueQuery(mode = "DueReview", reviewOrder = "Weakest", limit = 1), now = 100L)
        assertEquals(1, newIds.size)
        assertEquals(listOf("due-review-weak"), reviewIds)
        assertTrue(newIds.none { it in reviewIds })
    }


    @Test
    fun examQueueSelectsOnlyEligibleQuestionTypesAndMultipartQuestions() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "exam-flashcard", prompt = "Flashcard", answer = "A", type = "Flashcard", createdAt = 1L),
            QuestionEntity(id = "exam-mcq", prompt = "MCQ", answer = "B", type = "MCQ", optionsJson = "[\"B\",\"C\"]", createdAt = 2L),
            QuestionEntity(id = "exam-numerical", prompt = "Numerical", answer = "2", type = "Numerical", createdAt = 3L),
            QuestionEntity(id = "exam-multipart", prompt = "Multipart", answer = "", type = "Flashcard", createdAt = 4L)
        ))
        dao.upsertQuestionParts(listOf(QuestionPartEntity(id = "exam-part", questionId = "exam-multipart", position = 0, prompt = "Part A", answer = "A")))
        val ids = QuestionRepository(dao).createStudyQueueSnapshot(StudyQueueQuery(mode = "Exam", limit = 20), now = 100L)
        assertEquals(listOf("exam-mcq", "exam-numerical", "exam-multipart"), ids)
    }

    @Test
    fun rootTagDiscoverySplitsLegacyCsvInDatabaseWithoutQuestionList() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "tag-q1", prompt = "One", answer = "A", tagsCsv = "Physics, mechanics,  Exam"),
            QuestionEntity(id = "tag-q2", prompt = "Two", answer = "B", tagsCsv = "physics, Algebra")
        ))
        assertEquals(listOf("Algebra", "Exam", "mechanics", "Physics"), dao.observeQuestionTagNames().first())
    }

    @Test
    fun progressAnalyticsUseBoundedDatabaseProjections() = runBlocking {
        val dao = openDatabase().dao()
        dao.upsertQuestions(listOf(
            QuestionEntity(id = "pa-q1", prompt = "Algebra prompt", answer = "x", subject = "Math", chapter = "Algebra", topic = "Linear", type = "Numerical", incorrectCount = 2),
            QuestionEntity(id = "pa-q2", prompt = "History prompt", answer = "y", subject = "History", chapter = "Modern", topic = "Reform", type = "MCQ", incorrectCount = 1)
        ))
        dao.insertReviewLogs(listOf(
            ReviewLogEntity("pa-r1", "pa-q1", 1_700_000_000_000L, "Again", false, "Concept gap", "Review"),
            ReviewLogEntity("pa-r2", "pa-q1", 1_700_000_100_000L, "Good", true, "", "Exam"),
            ReviewLogEntity("pa-r3", "pa-q2", 1_700_000_200_000L, "Again", false, "Misread question", "Review")
        ))
        for (score in 10..100 step 10) {
            dao.upsertExamAttempt(ExamAttemptEntity(
                id = "pa-exam-$score", startedAt = score.toLong(), completedAt = score.toLong(),
                durationSeconds = 60, questionCount = 1, answeredCount = 1, markedCount = 0,
                earnedPoints = score.toDouble(), possiblePoints = 100.0, scorePercent = score.toDouble(), itemsJson = "[]"
            ))
        }

        val types = dao.observeProgressTypeAccuracy().first().associateBy { it.label }
        assertEquals(2, types.getValue("Numerical").scored)
        assertEquals(1, types.getValue("Numerical").correct)
        assertEquals(1, types.getValue("MCQ").scored)
        assertEquals("Linear", dao.observeProgressWeakTopics().first().first().label)
        assertEquals("Math", dao.observeProgressWeakSubjects().first().first().label)
        assertEquals("Algebra · Math", dao.observeProgressWeakChapters().first().first().label)
        assertEquals(1, dao.observeProgressMistakeCategories().first().first { it.label == "Concept gap" }.count)
        assertEquals(3, dao.observeProgressRecentReviews().first().size)
        assertEquals("History prompt", dao.observeProgressRecentReviews().first().first().prompt)

        val exams = dao.observeProgressExamStats().first()
        assertEquals(10, exams.attemptCount)
        assertEquals(55.0, exams.averageScore, 0.001)
        assertEquals(100.0, exams.bestScore, 0.001)
        assertEquals(55.0, exams.medianScore, 0.001)
        assertEquals(50.0, exams.recentTrend ?: Double.NaN, 0.001)
        assertEquals(3, exams.band0To39)
        assertEquals(2, exams.band40To59)
        assertEquals(2, exams.band60To79)
        assertEquals(3, exams.band80To100)
        assertEquals(10, dao.observeRecentExamAttempts().first().size)
        val quota = dao.observeStudyDailyQuotaStats(0L).first()
        assertEquals(0, quota.newReviewed)
        assertEquals(3, quota.reviewCount)
    }

    private fun openDatabase(): StudyDatabase {
        return Room.databaseBuilder(context, StudyDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
            .also { database = it }
    }
}
