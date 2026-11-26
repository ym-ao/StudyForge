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
import androidx.paging.PagingSource
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["updatedAt", "id"], orders = [Index.Order.DESC, Index.Order.ASC]),
        Index(value = ["dueAt", "id"])
    ]
)
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
    @ColumnInfo(defaultValue = "''") val exam: String = "",
    @ColumnInfo(defaultValue = "''") val sourceTitle: String = "",
    @ColumnInfo(defaultValue = "''") val sourceDetail: String = ""
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
    @ColumnInfo(defaultValue = "''") val sourceTitle: String = "",
    @ColumnInfo(defaultValue = "''") val sourceDetail: String = "",
    @ColumnInfo(defaultValue = "''") val createdAfter: String = "",
    @ColumnInfo(defaultValue = "''") val createdBefore: String = "",
    @ColumnInfo(defaultValue = "''") val updatedAfter: String = "",
    @ColumnInfo(defaultValue = "''") val updatedBefore: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toCriteria() = LibraryFilterCriteria(
        query = query, type = type, status = status, subject = subject,
        chapter = chapter, topic = topic, tag = tag, collectionId = collectionId,
        difficulty = difficulty, exam = exam, performance = performance,
        sourceTitle = sourceTitle, sourceDetail = sourceDetail, createdAfter = createdAfter,
        createdBefore = createdBefore, updatedAfter = updatedAfter, updatedBefore = updatedBefore
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
            performance = criteria.performance, sourceTitle = criteria.sourceTitle, sourceDetail = criteria.sourceDetail,
            createdAfter = criteria.createdAfter, createdBefore = criteria.createdBefore, updatedAfter = criteria.updatedAfter, updatedBefore = criteria.updatedBefore,
            createdAt = createdAt, updatedAt = updatedAt
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
    @ColumnInfo(defaultValue = "''") val collectionId: String = "",
    val questionLimit: Int = 20,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_attempts", indices = [Index("completedAt")])
data class ExamAttemptEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val completedAt: Long,
    val durationSeconds: Int,
    val questionCount: Int,
    val answeredCount: Int,
    val markedCount: Int,
    val earnedPoints: Double,
    val possiblePoints: Double,
    val scorePercent: Double,
    val itemsJson: String,
    val label: String = "Timed exam"
)


@Entity(tableName = "question_versions", indices = [Index(value = ["questionId", "versionNumber"], unique = true)])
data class QuestionVersionEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val versionNumber: Int,
    val savedAt: Long,
    val snapshotJson: String
)

/** Lightweight row used by paged Library lists; excludes answer, explanation, LaTeX and rich payloads. */
data class QuestionLibraryRow(
    val id: String,
    val prompt: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val exam: String,
    val type: String,
    val tagsCsv: String,
    val status: String,
    val difficulty: String,
    val createdAt: Long,
    val updatedAt: Long,
    val reviewCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val fsrsStability: Double,
    val fsrsDifficulty: Double,
    val dueAt: Long?
)

data class HomeDashboardStats(
    val questionCount: Int,
    val collectionCount: Int,
    val reviewCount: Int,
    val dueCount: Int
)

data class ProgressCoreStats(
    val questionCount: Int,
    val dueCount: Int,
    val newCount: Int,
    val learningCount: Int,
    val masteredCount: Int,
    val totalReviews: Int,
    val scoredReviews: Int,
    val correctReviews: Int,
    val recentScoredReviews: Int,
    val recentCorrectReviews: Int,
    val lastSevenDays: Int,
    val activeReviewDays: Int
)

data class ProgressCountRow(val label: String, val count: Int)
data class ProgressAccuracyRow(val label: String, val scored: Int, val correct: Int)
data class ProgressRecentReviewRow(
    val id: String, val questionId: String, val reviewedAt: Long, val rating: String,
    val wasCorrect: Boolean?, val mistakeCategory: String, val reviewMode: String,
    val prompt: String?
)
data class ProgressDailyReviewRow(val dayKey: String, val totalReviews: Int, val scoredReviews: Int, val correctReviews: Int)
data class StudyDailyQuotaStats(val newReviewed: Int, val reviewCount: Int)
data class ProgressExamResponseStats(val scored: Int, val correct: Int)
data class ProgressExamStats(
    val attemptCount: Int, val averageScore: Double, val bestScore: Double, val medianScore: Double,
    val recentTrend: Double?, val band0To39: Int, val band40To59: Int,
    val band60To79: Int, val band80To100: Int
)

@Dao
interface StudyDao {
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    /** Small dashboard projection: aggregate values are computed in SQLite, not from a full question list. */
    @Query("""
        SELECT
          (SELECT COUNT(*) FROM questions) AS questionCount,
          (SELECT COUNT(*) FROM collections) AS collectionCount,
          (SELECT COALESCE(SUM(reviewCount), 0) FROM questions) AS reviewCount,
          (SELECT COUNT(*) FROM questions WHERE dueAt IS NULL OR dueAt <= :now) AS dueCount
    """)
    fun observeHomeDashboardStats(now: Long): Flow<HomeDashboardStats>

    /** Compact Progress headline metrics; review logs remain the source of truth for accuracy. */
    @Query("""
        SELECT
          (SELECT COUNT(*) FROM questions) AS questionCount,
          (SELECT COUNT(*) FROM questions WHERE dueAt IS NULL OR dueAt <= :now) AS dueCount,
          (SELECT COUNT(*) FROM questions WHERE learningState = 'New') AS newCount,
          (SELECT COUNT(*) FROM questions WHERE learningState IN ('Learning', 'Relearning')) AS learningCount,
          (SELECT COUNT(*) FROM questions WHERE fsrsStability >= 21.0 AND learningState = 'Review') AS masteredCount,
          (SELECT COUNT(*) FROM review_logs) AS totalReviews,
          (SELECT COUNT(*) FROM review_logs WHERE wasCorrect IS NOT NULL) AS scoredReviews,
          (SELECT COUNT(*) FROM review_logs WHERE wasCorrect = 1) AS correctReviews,
          (SELECT COUNT(*) FROM review_logs WHERE reviewedAt >= :thirtyDayCutoff AND wasCorrect IS NOT NULL) AS recentScoredReviews,
          (SELECT COUNT(*) FROM review_logs WHERE reviewedAt >= :thirtyDayCutoff AND wasCorrect = 1) AS recentCorrectReviews,
          (SELECT COUNT(*) FROM review_logs WHERE reviewedAt >= :sevenDayCutoff) AS lastSevenDays,
          (SELECT COUNT(DISTINCT date(reviewedAt / 1000, 'unixepoch', 'localtime')) FROM review_logs) AS activeReviewDays
    """)
    fun observeProgressCoreStats(now: Long, thirtyDayCutoff: Long, sevenDayCutoff: Long): Flow<ProgressCoreStats>


    /** Detailed Progress projections stay in SQLite; no complete question/review-log lists are emitted. */
    @Query("""SELECT CASE WHEN q.id IS NULL THEN 'Deleted question' ELSE COALESCE(NULLIF(trim(q.type), ''), 'Unspecified') END AS label,
        COUNT(*) AS scored, SUM(CASE WHEN l.wasCorrect = 1 THEN 1 ELSE 0 END) AS correct
        FROM review_logs l LEFT JOIN questions q ON q.id = l.questionId
        WHERE l.wasCorrect IS NOT NULL GROUP BY label ORDER BY MAX(l.reviewedAt) DESC""")
    fun observeProgressTypeAccuracy(): Flow<List<ProgressAccuracyRow>>

    @Query("""SELECT CASE WHEN q.id IS NULL THEN 'Deleted question'
        WHEN trim(q.topic) != '' THEN trim(q.topic) WHEN trim(q.chapter) != '' THEN trim(q.chapter)
        WHEN trim(q.subject) != '' THEN trim(q.subject) ELSE 'Uncategorized' END AS label, COUNT(*) AS count
        FROM review_logs l LEFT JOIN questions q ON q.id = l.questionId
        WHERE l.wasCorrect = 0 GROUP BY label ORDER BY count DESC, label COLLATE NOCASE LIMIT 6""")
    fun observeProgressWeakTopics(): Flow<List<ProgressCountRow>>

    @Query("SELECT COALESCE(NULLIF(trim(subject), ''), 'Uncategorized') AS label, SUM(incorrectCount) AS count FROM questions GROUP BY label HAVING count > 0 ORDER BY count DESC, label COLLATE NOCASE LIMIT 6")
    fun observeProgressWeakSubjects(): Flow<List<ProgressCountRow>>

    @Query("SELECT COALESCE(NULLIF(trim(chapter), ''), 'Unspecified chapter') || ' · ' || COALESCE(NULLIF(trim(subject), ''), 'Uncategorized') AS label, SUM(incorrectCount) AS count FROM questions GROUP BY chapter, subject HAVING count > 0 ORDER BY count DESC, label COLLATE NOCASE LIMIT 6")
    fun observeProgressWeakChapters(): Flow<List<ProgressCountRow>>

    @Query("SELECT mistakeCategory AS label, COUNT(*) AS count FROM review_logs WHERE trim(mistakeCategory) != '' GROUP BY mistakeCategory ORDER BY count DESC, label COLLATE NOCASE LIMIT 8")
    fun observeProgressMistakeCategories(): Flow<List<ProgressCountRow>>

    @Query("""SELECT l.id, l.questionId, l.reviewedAt, l.rating, l.wasCorrect, l.mistakeCategory, l.reviewMode, q.prompt
        FROM review_logs l LEFT JOIN questions q ON q.id = l.questionId
        ORDER BY l.reviewedAt DESC, l.id DESC LIMIT 8""")
    fun observeProgressRecentReviews(): Flow<List<ProgressRecentReviewRow>>

    @Query("""SELECT date(reviewedAt / 1000, 'unixepoch', 'localtime') AS dayKey, COUNT(*) AS totalReviews,
        SUM(CASE WHEN wasCorrect IS NOT NULL THEN 1 ELSE 0 END) AS scoredReviews,
        SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END) AS correctReviews
        FROM review_logs WHERE reviewedAt >= :startMillis GROUP BY dayKey ORDER BY dayKey ASC""")
    fun observeProgressDailyReviews(startMillis: Long): Flow<List<ProgressDailyReviewRow>>

    @Query("""SELECT COUNT(*) AS attemptCount, COALESCE(AVG(scorePercent), 0.0) AS averageScore,
        COALESCE(MAX(scorePercent), 0.0) AS bestScore,
        COALESCE((SELECT AVG(scorePercent) FROM (SELECT scorePercent FROM exam_attempts ORDER BY scorePercent ASC LIMIT (2 - (SELECT COUNT(*) FROM exam_attempts) % 2) OFFSET (SELECT CASE WHEN COUNT(*) = 0 THEN 0 ELSE (COUNT(*) - 1) / 2 END FROM exam_attempts))), 0.0) AS medianScore,
        CASE WHEN COUNT(*) >= 10 THEN
          (SELECT AVG(scorePercent) FROM (SELECT scorePercent FROM exam_attempts ORDER BY completedAt DESC LIMIT 5)) -
          (SELECT AVG(scorePercent) FROM (SELECT scorePercent FROM exam_attempts ORDER BY completedAt DESC LIMIT 5 OFFSET 5))
        ELSE NULL END AS recentTrend,
        COALESCE(SUM(CASE WHEN scorePercent < 40 THEN 1 ELSE 0 END), 0) AS band0To39,
        COALESCE(SUM(CASE WHEN scorePercent >= 40 AND scorePercent < 60 THEN 1 ELSE 0 END), 0) AS band40To59,
        COALESCE(SUM(CASE WHEN scorePercent >= 60 AND scorePercent < 80 THEN 1 ELSE 0 END), 0) AS band60To79,
        COALESCE(SUM(CASE WHEN scorePercent >= 80 THEN 1 ELSE 0 END), 0) AS band80To100
        FROM exam_attempts""")
    fun observeProgressExamStats(): Flow<ProgressExamStats>

    @Query("SELECT * FROM exam_attempts ORDER BY completedAt DESC LIMIT 10")
    fun observeRecentExamAttempts(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN reviewMode = 'New' THEN 1 ELSE 0 END), 0) AS newReviewed, COALESCE(SUM(CASE WHEN reviewMode != 'New' THEN 1 ELSE 0 END), 0) AS reviewCount FROM review_logs WHERE reviewedAt >= :midnight")
    fun observeStudyDailyQuotaStats(midnight: Long): Flow<StudyDailyQuotaStats>

    @Query("SELECT COUNT(*) AS scored, COALESCE(SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END), 0) AS correct FROM review_logs WHERE lower(reviewMode) = 'exam' AND wasCorrect IS NOT NULL")
    fun observeProgressExamResponseStats(): Flow<ProgressExamResponseStats>

    @Query("""
        SELECT id, prompt, subject, chapter, topic, exam, type, tagsCsv, status, difficulty,
               createdAt, updatedAt, reviewCount, correctCount, incorrectCount,
               fsrsStability, fsrsDifficulty, dueAt
        FROM questions
        WHERE (:query = '' OR prompt LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%'
               OR explanation LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%'
               OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%'
               OR tagsCsv LIKE '%' || :query || '%' OR exam LIKE '%' || :query || '%'
               OR difficulty LIKE '%' || :query || '%' OR sourceTitle LIKE '%' || :query || '%'
               OR sourceDetail LIKE '%' || :query || '%')
          AND (:type = 'All' OR lower(type) = lower(:type))
          AND (:status = 'All' OR lower(status) = lower(:status))
          AND (:difficulty = 'All' OR lower(difficulty) = lower(:difficulty))
          AND (:subject = '' OR subject LIKE '%' || :subject || '%')
          AND (:chapter = '' OR chapter LIKE '%' || :chapter || '%')
          AND (:topic = '' OR topic LIKE '%' || :topic || '%')
          AND (:tag = '' OR lower(tagsCsv) LIKE '%' || lower(:tag) || '%')
          AND (:exam = '' OR exam LIKE '%' || :exam || '%')
          AND (:sourceTitle = '' OR sourceTitle LIKE '%' || :sourceTitle || '%')
          AND (:sourceDetail = '' OR sourceDetail LIKE '%' || :sourceDetail || '%')
          AND (:createdAfter = '' OR date(createdAt / 1000, 'unixepoch', 'localtime') >= :createdAfter)
          AND (:createdBefore = '' OR date(createdAt / 1000, 'unixepoch', 'localtime') <= :createdBefore)
          AND (:updatedAfter = '' OR date(updatedAt / 1000, 'unixepoch', 'localtime') >= :updatedAfter)
          AND (:updatedBefore = '' OR date(updatedAt / 1000, 'unixepoch', 'localtime') <= :updatedBefore)
          AND (:collectionId = '' OR EXISTS (SELECT 1 FROM question_collection qc
                 WHERE qc.questionId = questions.id AND qc.collectionId = :collectionId))
          AND (:performance = 'All'
               OR (:performance = 'Unattempted' AND reviewCount = 0)
               OR (:performance = 'Weak' AND reviewCount > 0 AND incorrectCount > correctCount)
               OR (:performance = 'Strong' AND reviewCount > 0 AND correctCount > 0 AND correctCount >= incorrectCount))
        ORDER BY updatedAt DESC, id ASC
    """)
    fun pageLibraryRows(
        query: String, type: String, status: String, difficulty: String,
        subject: String, chapter: String, topic: String, tag: String, exam: String,
        sourceTitle: String, sourceDetail: String, createdAfter: String, createdBefore: String,
        updatedAfter: String, updatedBefore: String, collectionId: String, performance: String
    ): PagingSource<Int, QuestionLibraryRow>

    @Query("""
        SELECT COUNT(*) FROM questions
        WHERE (:query = '' OR prompt LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%'
               OR explanation LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%'
               OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%'
               OR tagsCsv LIKE '%' || :query || '%' OR exam LIKE '%' || :query || '%'
               OR difficulty LIKE '%' || :query || '%' OR sourceTitle LIKE '%' || :query || '%'
               OR sourceDetail LIKE '%' || :query || '%')
          AND (:type = 'All' OR lower(type) = lower(:type))
          AND (:status = 'All' OR lower(status) = lower(:status))
          AND (:difficulty = 'All' OR lower(difficulty) = lower(:difficulty))
          AND (:subject = '' OR subject LIKE '%' || :subject || '%')
          AND (:chapter = '' OR chapter LIKE '%' || :chapter || '%')
          AND (:topic = '' OR topic LIKE '%' || :topic || '%')
          AND (:tag = '' OR lower(tagsCsv) LIKE '%' || lower(:tag) || '%')
          AND (:exam = '' OR exam LIKE '%' || :exam || '%')
          AND (:sourceTitle = '' OR sourceTitle LIKE '%' || :sourceTitle || '%')
          AND (:sourceDetail = '' OR sourceDetail LIKE '%' || :sourceDetail || '%')
          AND (:createdAfter = '' OR date(createdAt / 1000, 'unixepoch', 'localtime') >= :createdAfter)
          AND (:createdBefore = '' OR date(createdAt / 1000, 'unixepoch', 'localtime') <= :createdBefore)
          AND (:updatedAfter = '' OR date(updatedAt / 1000, 'unixepoch', 'localtime') >= :updatedAfter)
          AND (:updatedBefore = '' OR date(updatedAt / 1000, 'unixepoch', 'localtime') <= :updatedBefore)
          AND (:collectionId = '' OR EXISTS (SELECT 1 FROM question_collection qc
                 WHERE qc.questionId = questions.id AND qc.collectionId = :collectionId))
          AND (:performance = 'All'
               OR (:performance = 'Unattempted' AND reviewCount = 0)
               OR (:performance = 'Weak' AND reviewCount > 0 AND incorrectCount > correctCount)
               OR (:performance = 'Strong' AND reviewCount > 0 AND correctCount > 0 AND correctCount >= incorrectCount))
    """)
    suspend fun countLibraryRows(
        query: String, type: String, status: String, difficulty: String,
        subject: String, chapter: String, topic: String, tag: String, exam: String,
        sourceTitle: String, sourceDetail: String, createdAfter: String, createdBefore: String,
        updatedAfter: String, updatedBefore: String, collectionId: String, performance: String
    ): Int

    @Query("SELECT id FROM questions WHERE (:mode = 'All' OR (:mode = 'Due' AND (dueAt IS NULL OR dueAt <= :now)) OR (:mode = 'New' AND learningState = 'New')) ORDER BY dueAt ASC, id ASC LIMIT :limit")
    suspend fun snapshotStudyQueueIds(mode: String, now: Long, limit: Int): List<String>

    /** Bounded, deterministic session-start ID snapshot with the same filters as saved Study presets. */
    @Query("""
        SELECT q.id FROM questions q
        WHERE (:mode NOT IN ('Due', 'DueNew', 'DueReview') OR q.dueAt IS NULL OR q.dueAt <= :now)
          AND (:mode NOT IN ('New', 'DueNew') OR q.learningState = 'New')
          AND (:mode != 'DueReview' OR q.learningState != 'New')
          AND (:mode != 'Topic' OR :topic = '' OR lower(q.topic) = lower(:topic))
          AND (:subject = '' OR lower(q.subject) = lower(:subject))
          AND (:chapter = '' OR lower(q.chapter) = lower(:chapter))
          AND (:presetTopic = '' OR lower(q.topic) = lower(:presetTopic))
          AND (:tag = '' OR instr(',' || lower(replace(q.tagsCsv, ', ', ',')) || ',', ',' || lower(:tag) || ',') > 0)
          AND (:difficulty = 'All' OR lower(q.difficulty) = lower(:difficulty))
          AND (:exam = '' OR lower(q.exam) = lower(:exam))
          AND (:questionType = 'All' OR lower(q.type) = lower(:questionType))
          AND (:status = 'All' OR lower(q.status) = lower(:status))
          AND (:collectionId = '' OR EXISTS (SELECT 1 FROM question_collection qc WHERE qc.questionId = q.id AND qc.collectionId = :collectionId))
          AND (:performance = 'All'
               OR (:performance = 'Unattempted' AND q.reviewCount = 0)
               OR (:performance = 'Weak' AND q.incorrectCount > q.correctCount)
               OR (:performance = 'Strong' AND q.correctCount > q.incorrectCount AND q.reviewCount > 0))
          AND (:mode != 'Exam' OR q.type IN ('MCQ', 'True/False', 'Numerical')
               OR EXISTS (SELECT 1 FROM question_parts qp WHERE qp.questionId = q.id))
        ORDER BY
          CASE WHEN :mode IN ('Due', 'DueNew', 'DueReview') AND :reviewOrder != 'Random' AND :reviewOrder != 'Weakest' THEN CASE WHEN q.dueAt IS NULL THEN 0 ELSE 1 END END ASC,
          CASE WHEN :mode IN ('Due', 'DueNew', 'DueReview') AND :reviewOrder != 'Random' AND :reviewOrder != 'Weakest' THEN q.dueAt END ASC,
          CASE WHEN :mode = 'DueReview' AND :reviewOrder = 'Weakest' THEN q.incorrectCount END DESC,
          CASE WHEN :mode = 'DueReview' AND :reviewOrder = 'Weakest' THEN q.dueAt END ASC,
          CASE WHEN :mode IN ('DueNew', 'DueReview') AND :reviewOrder = 'Random' THEN random() END,
          CASE WHEN :mode = 'Ordered' THEN q.subject END COLLATE NOCASE ASC,
          CASE WHEN :mode = 'Ordered' THEN q.chapter END COLLATE NOCASE ASC,
          CASE WHEN :mode = 'Ordered' THEN q.topic END COLLATE NOCASE ASC,
          CASE WHEN :mode = 'Ordered' THEN q.createdAt END ASC,
          CASE WHEN :mode = 'Weakest' THEN q.incorrectCount END DESC,
          CASE WHEN :mode = 'Weakest' THEN q.fsrsDifficulty END DESC,
          CASE WHEN :mode = 'Random' OR (:mode = 'Exam' AND :randomize) THEN random() END,
          CASE WHEN :mode = 'Exam' AND NOT :randomize THEN q.createdAt END ASC,
          CASE WHEN :mode NOT IN ('Due', 'DueNew', 'DueReview', 'Ordered', 'Weakest', 'Random', 'Exam') THEN q.updatedAt END DESC,
          q.id ASC
        LIMIT :limit
    """)
    suspend fun snapshotStudyQueueIds(
        mode: String, now: Long, topic: String, subject: String, chapter: String,
        presetTopic: String, tag: String, difficulty: String, exam: String,
        questionType: String, status: String, collectionId: String,
        performance: String, reviewOrder: String, randomize: Boolean, limit: Int
    ): List<String>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE instr(',' || lower(replace(tagsCsv, ', ', ',')) || ',', ',' || lower(:tag) || ',') > 0 ORDER BY updatedAt DESC")
    suspend fun getQuestionsUsingTag(tag: String): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE instr(',' || lower(replace(tagsCsv, ', ', ',')) || ',', ',' || lower(:tag) || ',') > 0")
    suspend fun countQuestionsUsingTag(tag: String): Int


    @Query("SELECT * FROM questions WHERE prompt LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR answer LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%' OR chapter LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%' OR tagsCsv LIKE '%' || :query || '%' OR exam LIKE '%' || :query || '%' OR difficulty LIKE '%' || :query || '%' OR sourceTitle LIKE '%' || :query || '%' OR sourceDetail LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT DISTINCT trim(topic) FROM questions WHERE trim(topic) != '' ORDER BY trim(topic) COLLATE NOCASE")
    fun observeStudyTopics(): Flow<List<String>>


    /** Splits legacy comma-separated tags in SQLite so the app root need not observe every question. */
    @Query("""WITH RECURSIVE tag_tokens(questionId, remaining, tag) AS (
        SELECT id, tagsCsv || ',', '' FROM questions WHERE trim(tagsCsv) != ''
        UNION ALL
        SELECT questionId, substr(remaining, instr(remaining, ',') + 1),
               trim(substr(remaining, 1, instr(remaining, ',') - 1))
        FROM tag_tokens WHERE remaining != ''
    )
    SELECT MIN(tag) AS tag FROM tag_tokens WHERE tag != '' GROUP BY lower(tag) ORDER BY tag COLLATE NOCASE""")
    fun observeQuestionTagNames(): Flow<List<String>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestion(id: String): QuestionEntity?

    @Upsert
    suspend fun upsertQuestion(question: QuestionEntity)

    @Upsert
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestion(id: String)

    @Query("SELECT * FROM question_versions WHERE questionId = :questionId ORDER BY versionNumber DESC")
    fun observeQuestionVersions(questionId: String): Flow<List<QuestionVersionEntity>>

    @Query("SELECT COALESCE(MAX(versionNumber), 0) + 1 FROM question_versions WHERE questionId = :questionId")
    suspend fun nextQuestionVersion(questionId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertQuestionVersion(version: QuestionVersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestionVersion(version: QuestionVersionEntity)

    @Query("SELECT * FROM question_versions WHERE id = :id LIMIT 1")
    suspend fun getQuestionVersionById(id: String): QuestionVersionEntity?

    @Query("SELECT * FROM question_versions WHERE questionId = :questionId AND versionNumber = :versionNumber LIMIT 1")
    suspend fun getQuestionVersionByNumber(questionId: String, versionNumber: Int): QuestionVersionEntity?

    @Query("SELECT * FROM question_versions")
    suspend fun getAllQuestionVersions(): List<QuestionVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionVersions(versions: List<QuestionVersionEntity>)

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

    @Query("SELECT * FROM exam_attempts ORDER BY completedAt DESC")
    fun observeExamAttempts(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts ORDER BY completedAt DESC")
    suspend fun getAllExamAttempts(): List<ExamAttemptEntity>

    @Upsert
    suspend fun upsertExamAttempt(attempt: ExamAttemptEntity)

    @Upsert
    suspend fun upsertExamAttempts(attempts: List<ExamAttemptEntity>)

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

    /** Loads only parts belonging to the bounded active exam queue (maximum 500 question IDs). */
    @Query("SELECT * FROM question_parts WHERE questionId IN (:questionIds) ORDER BY questionId ASC, position ASC")
    suspend fun getQuestionPartsForQuestions(questionIds: List<String>): List<QuestionPartEntity>

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

    @Query("UPDATE study_session_presets SET collectionId = '' WHERE collectionId = :collectionId")
    suspend fun clearStudySessionCollection(collectionId: String)

}

@Database(
    entities = [QuestionEntity::class, CollectionEntity::class, QuestionCollectionCrossRef::class, ReviewLogEntity::class, StudySettingsEntity::class, SavedFilterPresetEntity::class, TagEntity::class, QuestionContentBlockEntity::class, QuestionPartEntity::class, StudySessionPresetEntity::class, ExamAttemptEntity::class, QuestionVersionEntity::class],
    version = 16,
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

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Deliberately no FK to questions: historical attempts remain available
                // even if a question is later removed from the active library.
                db.execSQL("CREATE TABLE IF NOT EXISTS exam_attempts (id TEXT NOT NULL, startedAt INTEGER NOT NULL, completedAt INTEGER NOT NULL, durationSeconds INTEGER NOT NULL, questionCount INTEGER NOT NULL, answeredCount INTEGER NOT NULL, markedCount INTEGER NOT NULL, earnedPoints REAL NOT NULL, possiblePoints REAL NOT NULL, scorePercent REAL NOT NULL, itemsJson TEXT NOT NULL, label TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_attempts_completedAt ON exam_attempts(completedAt)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_session_presets ADD COLUMN collectionId TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS question_versions (id TEXT NOT NULL, questionId TEXT NOT NULL, versionNumber INTEGER NOT NULL, savedAt INTEGER NOT NULL, snapshotJson TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_question_versions_questionId_versionNumber ON question_versions(questionId, versionNumber)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN sourceTitle TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE questions ADD COLUMN sourceDetail TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN sourceTitle TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN sourceDetail TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN createdAfter TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN createdBefore TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN updatedAfter TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_filter_presets ADD COLUMN updatedBefore TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Add only indexes justified by Batch 7 EXPLAIN QUERY PLAN evidence. */
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_questions_updatedAt_id ON questions(updatedAt DESC, id ASC)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_questions_dueAt_id ON questions(dueAt ASC, id ASC)")
            }
        }

        @Volatile private var INSTANCE: StudyDatabase? = null
        fun get(context: Context): StudyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studyforge.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16).build().also { INSTANCE = it }
        }
    }
}
