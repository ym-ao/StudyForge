package com.studyforge.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Migration
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
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
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

@Dao
interface StudyDao {
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE prompt LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestion(id: String): QuestionEntity?

    @Upsert
    suspend fun upsertQuestion(question: QuestionEntity)

    @Upsert
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestion(id: String)

    @Query("UPDATE questions SET reviewCount = :reviewCount, lastReviewedAt = :reviewedAt, dueAt = :dueAt, intervalDays = :intervalDays, easeFactor = :easeFactor, repetitions = :repetitions, lapseCount = :lapseCount, learningState = :learningState, correctCount = :correctCount, incorrectCount = :incorrectCount WHERE id = :id")
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
        correctCount: Int,
        incorrectCount: Int
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
}

@Database(
    entities = [QuestionEntity::class, CollectionEntity::class, QuestionCollectionCrossRef::class, ReviewLogEntity::class],
    version = 2,
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

        @Volatile private var INSTANCE: StudyDatabase? = null
        fun get(context: Context): StudyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studyforge.db"
            ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
        }
    }
}
