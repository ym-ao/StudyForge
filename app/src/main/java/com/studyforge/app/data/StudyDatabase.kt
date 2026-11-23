package com.studyforge.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.migration.Migration
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val answer: String,
    val explanation: String = "",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val type: String = "Flashcard",
    val optionsJson: String = "[]",
    val latex: String = "",
    val tagsCsv: String = "",
    val status: String = "Draft",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val lastReviewedAt: Long? = null,
    val dueAt: Long? = null,
    val intervalDays: Double = 0.0,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val lapseCount: Int = 0,
    val learningState: String = "New",
    @ColumnInfo(defaultValue = "-1") val learningStepIndex: Int = -1,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    // FSRS memory state. Zero means this card has not yet been converted/reviewed with FSRS.
    @ColumnInfo(defaultValue = "0.0") val fsrsStability: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0") val fsrsDifficulty: Double = 0.0,
    @ColumnInfo(defaultValue = "'Unrated'") val difficulty: String = "Unrated",
    @ColumnInfo(defaultValue = "''") val exam: String = ""
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "question_collection",
    primaryKeys = ["questionId", "collectionId"],
    foreignKeys = [
        ForeignKey(entity = QuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CollectionEntity::class, parentColumns = ["id"], childColumns = ["collectionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("collectionId")]
)
data class QuestionCollectionCrossRef(val questionId: String, val collectionId: String)

@Entity(
    tableName = "review_logs",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId"), Index("reviewedAt")]
)
data class ReviewLogEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val reviewedAt: Long,
    val rating: String,
    val wasCorrect: Boolean? = null,
    val mistakeCategory: String = "",
    val reviewMode: String = "Review",
    val scheduledDueAt: Long? = null
)

@Entity(tableName = "study_settings")
data class StudySettingsEntity(
    @PrimaryKey val id: String = "default",
    val desiredRetention: Double = 0.90,
    val maxIntervalDays: Int = 36500,
    val newCardsPerDay: Int = 20,
    val reviewCardsPerDay: Int = 200,
    val reviewOrder: String = "Due first",
    val learningStepsMinutesCsv: String = "10",
    val relearningStepsMinutesCsv: String = "10",
    val examQuestionCount: Int = 20,
    val examDurationMinutes: Int = 30,
    val examRandomize: Boolean = true
) {
    companion object {
        val DEFAULT = StudySettingsEntity()
    }
}

@Entity(
    tableName = "saved_filter_presets",
    foreignKeys = [ForeignKey(
        entity = CollectionEntity::class,
        parentColumns = ["id"],
        childColumns = ["collectionId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("collectionId")]
)
data class SavedFilterPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val query: String = "",
    val type: String = "All",
    val status: String = "All",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val tag: String = "",
    val collectionId: String? = null,
    val difficulty: String = "All",
    val exam: String = "",
    val performance: String = "All",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toCriteria() = LibraryFilterCriteria(
        query = query, type = type, status = status, subject = subject,
        chapter = chapter, topic = topic, tag = tag, collectionId = collectionId,
        difficulty = difficulty, exam = exam, performance = performance
    )

    companion object {
        fun fromCriteria(
            id: String,
            name: String,
            criteria: LibraryFilterCriteria,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis()
        ) = SavedFilterPresetEntity(
            id = id, name = name.trim(), query = criteria.query, type = criteria.type,
            status = criteria.status, subject = criteria.subject, chapter = criteria.chapter,
            topic = criteria.topic, tag = criteria.tag, collectionId = criteria.collectionId,
            difficulty = criteria.difficulty, exam = criteria.exam,
            performance = criteria.performance, createdAt = createdAt, updatedAt = updatedAt
        )
    }
}


@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(
    tableName = "question_content_blocks",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId"), Index(value = ["questionId", "position"], unique = true)]
)
data class QuestionContentBlockEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val position: Int,
    val kind: String,
    val text: String = "",
    val mediaBase64: String = "",
    val mediaMimeType: String = "",
    val altText: String = ""
)

@Entity(
    tableName = "question_parts",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId"), Index(value = ["questionId", "position"], unique = true)]
)
data class QuestionPartEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val position: Int,
    val prompt: String,
    val answer: String,
    val explanation: String = "",
    val points: Double = 1.0,
    val optionsJson: String = "[]",
    @ColumnInfo(defaultValue = "0.0") val absoluteTolerance: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0") val relativeTolerance: Double = 0.0,
    @ColumnInfo(defaultValue = "") val acceptedAnswers: String = ""
)

@Entity(tableName = "study_session_presets")
data class StudySessionPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mode: String = "Practice",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val tag: String = "",
    val difficulty: String = "All",
    val exam: String = "",
    @ColumnInfo(defaultValue = "'All'") val questionType: String = "All",
    @ColumnInfo(defaultValue = "'All'") val status: String = "All",
    @ColumnInfo(defaultValue = "'All'") val performance: String = "All",
    val questionLimit: Int = 20,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface StudyDao {
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE prompt LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%' OR exam LIKE '%' || :query || '%' OR difficulty LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestion(id: String): QuestionEntity?

    @Upsert
    suspend fun upsertQuestion(question: QuestionEntity)

    @Upsert
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestion(id: String)

    @Query("UPDATE questions SET reviewCount = :reviewCount, lastReviewedAt = :reviewedAt, dueAt = :dueAt, intervalDays = :intervalDays, easeFactor = :easeFactor, repetitions = :repetitions, lapseCount = :lapseCount, learningState = :learningState, learningStepIndex = :learningStepIndex, correctCount = :correctCount, incorrectCount = :incorrectCount, fsrsStability = :fsrsStability, fsrsDifficulty = :fsrsDifficulty WHERE id = :id")
    suspend fun updateReviewSchedule(
        id: String,
        reviewCount: Int,
        reviewedAt: Long,
        dueAt: Long,
        intervalDays: Double,
        easeFactor: Double,
        repetitions: Int,
        lapseCount: Int,
        learningState: String,
        learningStepIndex: Int,
        correctCount: Int,
        incorrectCount: Int,
        fsrsStability: Double,
        fsrsDifficulty: Double
    )

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections")
    suspend fun getAllCollections(): List<CollectionEntity>

    @Upsert
    suspend fun upsertCollection(collection: CollectionEntity)

    @Upsert
    suspend fun upsertCollections(collections: List<CollectionEntity>)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: String)

    @Query("SELECT * FROM question_collection")
    suspend fun getAllLinks(): List<QuestionCollectionCrossRef>

    @Query("SELECT * FROM question_collection")
    fun observeLinks(): Flow<List<QuestionCollectionCrossRef>>

    @Query("SELECT collectionId FROM question_collection WHERE questionId = :questionId")
    suspend fun getCollectionIds(questionId: String): List<String>

    @Query("DELETE FROM question_collection WHERE questionId = :questionId")
    suspend fun clearQuestionCollections(questionId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinks(links: List<QuestionCollectionCrossRef>)

    @Query("SELECT q.* FROM questions q INNER JOIN question_collection qc ON qc.questionId = q.id WHERE qc.collectionId = :collectionId ORDER BY q.updatedAt DESC")
    fun observeCollectionQuestions(collectionId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM review_logs ORDER BY reviewedAt DESC")
    fun observeReviewLogs(): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs ORDER BY reviewedAt DESC")
    suspend fun getAllReviewLogs(): List<ReviewLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLogs(logs: List<ReviewLogEntity>)

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    suspend fun getAllTags(): List<TagEntity>

    @Upsert
    suspend fun upsertTag(tag: TagEntity)

    @Upsert
    suspend fun upsertTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: String)

    @Query("SELECT * FROM saved_filter_presets ORDER BY name COLLATE NOCASE")
    fun observeSavedFilterPresets(): Flow<List<SavedFilterPresetEntity>>

    @Query("SELECT * FROM saved_filter_presets ORDER BY name COLLATE NOCASE")
    suspend fun getAllSavedFilterPresets(): List<SavedFilterPresetEntity>

    @Upsert
    suspend fun upsertSavedFilterPreset(preset: SavedFilterPresetEntity)

    @Upsert
    suspend fun upsertSavedFilterPresets(presets: List<SavedFilterPresetEntity>)

    @Query("DELETE FROM saved_filter_presets WHERE id = :id")
    suspend fun deleteSavedFilterPreset(id: String)

    @Query("SELECT * FROM study_settings WHERE id = 'default' LIMIT 1")
    fun observeSettings(): Flow<StudySettingsEntity?>

    @Query("SELECT * FROM study_settings WHERE id = 'default' LIMIT 1")
    suspend fun getSettings(): StudySettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: StudySettingsEntity)


    @Query("SELECT * FROM question_content_blocks ORDER BY questionId, position")
    suspend fun getAllContentBlocks(): List<QuestionContentBlockEntity>

    @Query("SELECT * FROM question_content_blocks ORDER BY questionId, position")
    fun observeContentBlocks(): Flow<List<QuestionContentBlockEntity>>

    @Query("SELECT * FROM question_content_blocks WHERE questionId = :questionId ORDER BY position")
    suspend fun getContentBlocks(questionId: String): List<QuestionContentBlockEntity>

    @Upsert
    suspend fun upsertContentBlocks(blocks: List<QuestionContentBlockEntity>)

    @Query("DELETE FROM question_content_blocks WHERE questionId = :questionId")
    suspend fun deleteContentBlocks(questionId: String)

    @Query("SELECT * FROM question_parts ORDER BY questionId, position")
    suspend fun getAllQuestionParts(): List<QuestionPartEntity>

    @Query("SELECT * FROM question_parts ORDER BY questionId, position")
    fun observeQuestionParts(): Flow<List<QuestionPartEntity>>

    @Query("SELECT * FROM question_parts WHERE questionId = :questionId ORDER BY position")
    suspend fun getQuestionParts(questionId: String): List<QuestionPartEntity>

    @Upsert
    suspend fun upsertQuestionParts(parts: List<QuestionPartEntity>)

    @Query("DELETE FROM question_parts WHERE questionId = :questionId")
    suspend fun deleteQuestionParts(questionId: String)

    @Query("SELECT * FROM study_session_presets ORDER BY name COLLATE NOCASE")
    fun observeStudySessionPresets(): Flow<List<StudySessionPresetEntity>>

    @Query("SELECT * FROM study_session_presets ORDER BY name COLLATE NOCASE")
    suspend fun getAllStudySessionPresets(): List<StudySessionPresetEntity>

    @Upsert
    suspend fun upsertStudySessionPreset(preset: StudySessionPresetEntity)

    @Upsert
    suspend fun upsertStudySessionPresets(presets: List<StudySessionPresetEntity>)

    @Query("DELETE FROM study_session_presets WHERE id = :id")
    suspend fun deleteStudySessionPreset(id: String)

}

@Database(
    entities = [QuestionEntity::class, CollectionEntity::class, QuestionCollectionCrossRef::class, ReviewLogEntity::class, StudySettingsEntity::class, SavedFilterPresetEntity::class, TagEntity::class, QuestionContentBlockEntity::class, QuestionPartEntity::class, StudySessionPresetEntity::class],
    version = 9,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun dao(): StudyDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN dueAt INTEGER")
                db.execSQL("ALTER TABLE questions ADD COLUMN intervalDays REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE questions ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5")
                db.execSQL("ALTER TABLE questions ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN lapseCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN learningState TEXT NOT NULL DEFAULT 'New'")
                db.execSQL("ALTER TABLE questions ADD COLUMN correctCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN incorrectCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS review_logs (id TEXT NOT NULL, questionId TEXT NOT NULL, reviewedAt INTEGER NOT NULL, rating TEXT NOT NULL, wasCorrect INTEGER, mistakeCategory TEXT NOT NULL, reviewMode TEXT NOT NULL, scheduledDueAt INTEGER, PRIMARY KEY(id), FOREIGN KEY(questionId) REFERENCES questions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_questionId ON review_logs(questionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_reviewedAt ON review_logs(reviewedAt)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN fsrsStability REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE questions ADD COLUMN fsrsDifficulty REAL NOT NULL DEFAULT 0.0")
                db.execSQL("CREATE TABLE IF NOT EXISTS study_settings (id TEXT NOT NULL, desiredRetention REAL NOT NULL, maxIntervalDays INTEGER NOT NULL, newCardsPerDay INTEGER NOT NULL, reviewCardsPerDay INTEGER NOT NULL, reviewOrder TEXT NOT NULL, learningStepsMinutesCsv TEXT NOT NULL, relearningStepsMinutesCsv TEXT NOT NULL, examQuestionCount INTEGER NOT NULL, examDurationMinutes INTEGER NOT NULL, examRandomize INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'Unrated'")
                db.execSQL("ALTER TABLE questions ADD COLUMN exam TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN learningStepIndex INTEGER NOT NULL DEFAULT -1")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS saved_filter_presets (id TEXT NOT NULL, name TEXT NOT NULL, query TEXT NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, subject TEXT NOT NULL, chapter TEXT NOT NULL, topic TEXT NOT NULL, tag TEXT NOT NULL, collectionId TEXT, difficulty TEXT NOT NULL, exam TEXT NOT NULL, performance TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(collectionId) REFERENCES collections(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_filter_presets_collectionId ON saved_filter_presets(collectionId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS tags (id TEXT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS question_content_blocks (id TEXT NOT NULL, questionId TEXT NOT NULL, position INTEGER NOT NULL, kind TEXT NOT NULL, text TEXT NOT NULL, mediaBase64 TEXT NOT NULL, mediaMimeType TEXT NOT NULL, altText TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(questionId) REFERENCES questions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_question_content_blocks_questionId ON question_content_blocks(questionId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_question_content_blocks_questionId_position ON question_content_blocks(questionId, position)")
                db.execSQL("CREATE TABLE IF NOT EXISTS question_parts (id TEXT NOT NULL, questionId TEXT NOT NULL, position INTEGER NOT NULL, prompt TEXT NOT NULL, answer TEXT NOT NULL, explanation TEXT NOT NULL, points REAL NOT NULL, optionsJson TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(questionId) REFERENCES questions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_question_parts_questionId ON question_parts(questionId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_question_parts_questionId_position ON question_parts(questionId, position)")
                db.execSQL("CREATE TABLE IF NOT EXISTS study_session_presets (id TEXT NOT NULL, name TEXT NOT NULL, mode TEXT NOT NULL, subject TEXT NOT NULL, chapter TEXT NOT NULL, topic TEXT NOT NULL, tag TEXT NOT NULL, difficulty TEXT NOT NULL, exam TEXT NOT NULL, questionLimit INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE question_parts ADD COLUMN absoluteTolerance REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE question_parts ADD COLUMN relativeTolerance REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE question_parts ADD COLUMN acceptedAnswers TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE study_session_presets ADD COLUMN questionType TEXT NOT NULL DEFAULT 'All'")
                db.execSQL("ALTER TABLE study_session_presets ADD COLUMN status TEXT NOT NULL DEFAULT 'All'")
                db.execSQL("ALTER TABLE study_session_presets ADD COLUMN performance TEXT NOT NULL DEFAULT 'All'")
            }
        }

        @Volatile private var INSTANCE: StudyDatabase? = null
        fun get(context: Context): StudyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studyforge.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9).build().also { INSTANCE = it }
        }
    }
}
