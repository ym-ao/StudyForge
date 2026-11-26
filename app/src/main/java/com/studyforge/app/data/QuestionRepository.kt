package com.studyforge.app.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/** Immutable criteria snapshot; changing any field creates a fresh Room PagingSource. */
data class LibraryPageQuery(
    val query: String = "",
    val type: String = "All",
    val status: String = "All",
    val difficulty: String = "All",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val tag: String = "",
    val exam: String = "",
    val sourceTitle: String = "",
    val sourceDetail: String = "",
    val createdAfter: String = "",
    val createdBefore: String = "",
    val updatedAfter: String = "",
    val updatedBefore: String = "",
    val collectionId: String = "",
    val performance: String = "All"
) {
    companion object {
        fun from(criteria: LibraryFilterCriteria) = LibraryPageQuery(
            query = criteria.query.trim(), type = criteria.type, status = criteria.status,
            difficulty = criteria.difficulty, subject = criteria.subject.trim(),
            chapter = criteria.chapter.trim(), topic = criteria.topic.trim(), tag = criteria.tag.trim(),
            exam = criteria.exam.trim(), sourceTitle = criteria.sourceTitle.trim(),
            sourceDetail = criteria.sourceDetail.trim(), createdAfter = criteria.createdAfter,
            createdBefore = criteria.createdBefore, updatedAfter = criteria.updatedAfter,
            updatedBefore = criteria.updatedBefore, collectionId = criteria.collectionId.orEmpty(),
            performance = criteria.performance
        )
    }
}

/** Criteria captured once when a Study session begins; changing criteria creates a new snapshot. */
data class StudyQueueQuery(
    val mode: String = "Due",
    val topic: String = "",
    val subject: String = "",
    val chapter: String = "",
    val presetTopic: String = "",
    val tag: String = "",
    val difficulty: String = "All",
    val exam: String = "",
    val questionType: String = "All",
    val status: String = "All",
    val collectionId: String = "",
    val performance: String = "All",
    val reviewOrder: String = "Oldest",
    val randomize: Boolean = false,
    val limit: Int = 20
) {
    fun validate() {
        require(mode in setOf("Due", "DueNew", "DueReview", "All", "New", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam"))
        require(reviewOrder in setOf("Oldest", "Weakest", "Random", "Due first"))
        require(limit in 1..QuestionRepository.MAX_STUDY_SESSION_SIZE)
    }
}

/** Database boundary for paged Library access and bounded Study-session snapshots. */
class QuestionRepository(private val dao: StudyDao) {
    fun pageLibrary(query: LibraryPageQuery): Flow<PagingData<QuestionLibraryRow>> = Pager(
        config = PagingConfig(
            pageSize = LIBRARY_PAGE_SIZE,
            initialLoadSize = LIBRARY_PAGE_SIZE,
            prefetchDistance = LIBRARY_PREFETCH_DISTANCE,
            enablePlaceholders = false,
            maxSize = LIBRARY_MAX_LOADED_ROWS
        ),
        pagingSourceFactory = {
            dao.pageLibraryRows(
                query.query, query.type, query.status, query.difficulty,
                query.subject, query.chapter, query.topic, query.tag, query.exam,
                query.sourceTitle, query.sourceDetail, query.createdAfter, query.createdBefore,
                query.updatedAfter, query.updatedBefore, query.collectionId, query.performance
            )
        }
    ).flow

    suspend fun countLibrary(query: LibraryPageQuery): Int = dao.countLibraryRows(
        query.query, query.type, query.status, query.difficulty,
        query.subject, query.chapter, query.topic, query.tag, query.exam,
        query.sourceTitle, query.sourceDetail, query.createdAfter, query.createdBefore,
        query.updatedAfter, query.updatedBefore, query.collectionId, query.performance
    )

    suspend fun countTagUsage(tag: String): Int = dao.countQuestionsUsingTag(tag)

    /** Saves template metadata in its own structured-content block; legacy question fields remain untouched. */
    suspend fun saveNumericalTemplate(questionId: String, template: NumericalTemplateEngine.Template) {
        require(questionId.isNotBlank())
        val blocks = dao.getContentBlocks(questionId)
        val existing = blocks.singleOrNull { it.kind == NumericalTemplateCodec.CONTENT_BLOCK_KIND }
        require(blocks.count { it.kind == NumericalTemplateCodec.CONTENT_BLOCK_KIND } <= 1) {
            "This question contains duplicate numerical template blocks."
        }
        val position = existing?.position ?: ((blocks.maxOfOrNull { it.position } ?: -1) + 1)
        dao.upsertContentBlocks(listOf(NumericalTemplateCodec.toContentBlock(
            questionId = questionId, template = template, position = position,
            blockId = existing?.id ?: "numerical-template:$questionId"
        )))
    }

    suspend fun loadNumericalTemplate(questionId: String): Result<NumericalTemplateEngine.Template> =
        NumericalTemplateCodec.fromContentBlocks(questionId, dao.getContentBlocks(questionId))

    /** Captures a bounded, stable order of IDs; ratings cannot shift offsets mid-session. */
    suspend fun createStudyQueueSnapshot(mode: String, now: Long, limit: Int): List<String> {
        require(limit in 1..MAX_STUDY_SESSION_SIZE) { "Study session size must be between 1 and $MAX_STUDY_SESSION_SIZE" }
        require(mode in setOf("All", "Due", "New")) { "Unsupported queue mode: $mode" }
        return dao.snapshotStudyQueueIds(mode, now, limit)
    }

    /** Creates a bounded ID snapshot in SQLite, preserving mode and saved-preset filters. */
    suspend fun createStudyQueueSnapshot(query: StudyQueueQuery, now: Long): List<String> {
        query.validate()
        return dao.snapshotStudyQueueIds(
            query.mode, now, query.topic, query.subject, query.chapter, query.presetTopic,
            query.tag, query.difficulty, query.exam, query.questionType, query.status,
            query.collectionId, query.performance, query.reviewOrder, query.randomize, query.limit
        )
    }

    /** Loads only the requested card records; caller retains the snapshot order. */
    suspend fun loadStudyQueueCards(ids: List<String>): List<QuestionEntity> {
        if (ids.isEmpty()) return emptyList()
        val byId = ids.chunked(SQLITE_SAFE_BIND_CHUNK).flatMap { dao.getQuestionsByIds(it) }.associateBy { it.id }
        return ids.mapNotNull(byId::get)
    }

    companion object {
        const val LIBRARY_PAGE_SIZE = 30
        const val LIBRARY_PREFETCH_DISTANCE = 10
        const val LIBRARY_MAX_LOADED_ROWS = 90
        const val MAX_STUDY_SESSION_SIZE = 500
        private const val SQLITE_SAFE_BIND_CHUNK = 900
    }
}
