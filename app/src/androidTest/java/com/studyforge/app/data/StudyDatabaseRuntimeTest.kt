package com.studyforge.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
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

    private fun openDatabase(): StudyDatabase {
        return Room.databaseBuilder(context, StudyDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
            .also { database = it }
    }
}
