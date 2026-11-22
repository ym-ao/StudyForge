package com.studyforge.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
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
    val lastReviewedAt: Long? = null
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

@Dao
interface StudyDao {
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE prompt LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestion(id: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestion(id: String)

    @Query("UPDATE questions SET reviewCount = reviewCount + 1, lastReviewedAt = :reviewedAt WHERE id = :id")
    suspend fun recordReview(id: String, reviewedAt: Long)

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections")
    suspend fun getAllCollections(): List<CollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollection(collection: CollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

}

@Database(
    entities = [QuestionEntity::class, CollectionEntity::class, QuestionCollectionCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun dao(): StudyDao

    companion object {
        @Volatile private var INSTANCE: StudyDatabase? = null
        fun get(context: Context): StudyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studyforge.db"
            ).build().also { INSTANCE = it }
        }
    }
}
