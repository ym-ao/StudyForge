package com.studyforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.QuestionEntity
import com.studyforge.app.data.ProgressCoreStats
import com.studyforge.app.data.CollectionEntity
import com.studyforge.app.data.QuestionPartEntity
import com.studyforge.app.data.MultipartExamRules
import com.studyforge.app.data.QuestionContentBlockEntity
import com.studyforge.app.data.StudySessionPresetEntity
import com.studyforge.app.data.StudySessionRules
import com.studyforge.app.data.StudySessionPresetRules
import com.studyforge.app.data.StudyQueueQuery
import com.studyforge.app.data.DerivationRules
import com.studyforge.app.data.ProgressAccuracyRow
import com.studyforge.app.data.ProgressCountRow
import com.studyforge.app.data.ProgressRecentReviewRow
import com.studyforge.app.data.ProgressDailyReviewRow
import com.studyforge.app.data.ProgressExamStats
import com.studyforge.app.data.ProgressExamResponseStats
import com.studyforge.app.data.ReviewScheduler
import com.studyforge.app.data.StudySettingsEntity
import com.studyforge.app.data.ExamAttemptEntity
import com.studyforge.app.data.ExamAttemptRules
import com.studyforge.app.data.ClozeRules
import com.studyforge.app.data.NumericalTemplateCodec
import com.studyforge.app.data.NumericalTemplateEngine
import com.studyforge.app.data.NumericalTemplateAttemptRules
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val mistakeCategories = listOf(
    "Concept gap", "Wrong formula", "Calculation / algebra", "Unit conversion",
    "Sign / direction", "Misread question", "Forgot fact", "Guess / uncertainty"
)

@Composable
fun StudyScreen(
    topics: List<String>,
    createStudyQueueIds: suspend (StudyQueueQuery, Long) -> List<String>,
    loadStudyQueueCards: suspend (List<String>) -> List<QuestionEntity>,
    newReviewedToday: Int,
    reviewsToday: Int,
    loadContentBlocks: suspend (String) -> List<QuestionContentBlockEntity>,
    loadQuestionParts: suspend (List<String>) -> List<QuestionPartEntity>,
    sessionPresets: List<StudySessionPresetEntity>,
    collections: List<CollectionEntity>,
    settings: StudySettingsEntity,
    onSaveSessionPreset: (StudySessionPresetEntity) -> Unit,
    onDeleteSessionPreset: (StudySessionPresetEntity) -> Unit,
    onSaveExamAttempt: (ExamAttemptEntity) -> Unit,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf("Due") }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var dueCompletedCount by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var mistakeCategory by rememberSaveable { mutableStateOf("") }
    var stepByStepStage by rememberSaveable { mutableIntStateOf(0) }
    var selectedTopic by rememberSaveable { mutableStateOf("") }
    var activeSessionPresetId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSessionPresetId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSaveSessionDialog by remember { mutableStateOf(false) }
    var newSessionName by remember { mutableStateOf("") }
    var newSessionLimit by remember { mutableStateOf("20") }
    var newSessionSubject by remember { mutableStateOf("") }
    var newSessionChapter by remember { mutableStateOf("") }
    var newSessionTag by remember { mutableStateOf("") }
    var newSessionDifficulty by remember { mutableStateOf("All") }
    var newSessionExam by remember { mutableStateOf("") }
    var newSessionType by remember { mutableStateOf("All") }
    var newSessionStatus by remember { mutableStateOf("All") }
    var newSessionPerformance by remember { mutableStateOf("All") }
    var newSessionCollectionId by remember { mutableStateOf("") }
    var collectionMenuExpanded by remember { mutableStateOf(false) }
    var sessionPresetPendingDelete by remember { mutableStateOf<StudySessionPresetEntity?>(null) }
    val sessionPresetNameConflict = StudySessionPresetRules.hasNameConflict(sessionPresets, newSessionName, editingSessionPresetId)

    val activeSessionPreset = sessionPresets.firstOrNull { it.id == activeSessionPresetId }
    val now = System.currentTimeMillis()
    val remainingNew = (settings.newCardsPerDay - newReviewedToday).coerceAtLeast(0)
    val remainingReviews = (settings.reviewCardsPerDay - reviewsToday).coerceAtLeast(0)
    var activeQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var activeQueueIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var activeQueueFingerprint by rememberSaveable { mutableStateOf("") }
    var queueLoading by remember { mutableStateOf(true) }
    var queueError by remember { mutableStateOf<String?>(null) }
    var queueRefreshToken by remember { mutableIntStateOf(0) }
    val activeQueueLimit = activeSessionPreset?.questionLimit?.coerceIn(1, 500) ?: 500
    val activeNewQuota = if (mode == "Due") remainingNew else -1
    val activeReviewQuota = if (mode == "Due") remainingReviews else -1
    val activeReviewOrder = if (mode == "Due") settings.reviewOrder else "Oldest"
    val activeExamRandomize = mode == "Exam" && settings.examRandomize
    val queueFingerprint = listOf(
        mode, selectedTopic, activeSessionPreset?.toString().orEmpty(), activeQueueLimit,
        activeNewQuota, activeReviewQuota, activeReviewOrder, activeExamRandomize
    ).joinToString("|")

    LaunchedEffect(mode, selectedTopic, activeSessionPreset, activeNewQuota, activeReviewQuota, activeReviewOrder, activeExamRandomize, queueRefreshToken) {
        queueLoading = true
        queueError = null
        try {
            fun queryFor(queueMode: String, limit: Int) = StudyQueueQuery(
                mode = queueMode,
                topic = if (queueMode == "Topic") selectedTopic else "",
                subject = activeSessionPreset?.subject.orEmpty(),
                chapter = activeSessionPreset?.chapter.orEmpty(),
                presetTopic = activeSessionPreset?.topic.orEmpty(),
                tag = activeSessionPreset?.tag.orEmpty(),
                difficulty = activeSessionPreset?.difficulty ?: "All",
                exam = activeSessionPreset?.exam.orEmpty(),
                questionType = activeSessionPreset?.questionType ?: "All",
                status = activeSessionPreset?.status ?: "All",
                collectionId = activeSessionPreset?.collectionId.orEmpty(),
                performance = activeSessionPreset?.performance ?: "All",
                reviewOrder = activeReviewOrder,
                randomize = activeExamRandomize,
                limit = limit.coerceIn(1, 500)
            )
            val ids = if (activeQueueIds.isNotEmpty() && activeQueueFingerprint == queueFingerprint) {
                activeQueueIds
            } else if (mode == "Due") {
                val newIds = if (remainingNew > 0) createStudyQueueIds(queryFor("DueNew", remainingNew.coerceAtMost(activeQueueLimit)), now) else emptyList()
                val reviewIds = if (remainingReviews > 0) createStudyQueueIds(queryFor("DueReview", remainingReviews.coerceAtMost(activeQueueLimit)), now) else emptyList()
                (newIds + reviewIds).take(activeQueueLimit)
            } else {
                val queryMode = when (mode) {
                    "Practice", "Step-by-step" -> "All"
                    else -> mode
                }
                createStudyQueueIds(queryFor(queryMode, activeQueueLimit), now)
            }
            activeQueueIds = ids
            activeQueueFingerprint = queueFingerprint
            val hydratedCards = loadStudyQueueCards(ids)
            activeQuestions = hydratedCards
            if (hydratedCards.size != ids.size) activeQueueIds = hydratedCards.map { it.id }
        } catch (error: Exception) {
            queueError = error.message ?: "Unable to load the study queue"
            activeQuestions = emptyList()
        } finally {
            queueLoading = false
        }
    }

    LaunchedEffect(mode, selectedTopic, activeSessionPresetId) {
        dueCompletedCount = 0
    }

    LaunchedEffect(mode, selectedTopic, activeSessionPresetId, queueFingerprint) {
        index = 0
        revealed = false
        checked = false
        selectedAnswer = null
        mistakeCategory = ""
        stepByStepStage = 0
    }

    if (showSaveSessionDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSessionDialog = false; editingSessionPresetId = null },
            title = { Text(if (editingSessionPresetId != null) "Edit study session" else "Save study session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(newSessionName, { newSessionName = it.take(50) }, label = { Text("Session name") }, singleLine = true, isError = sessionPresetNameConflict)
                    if (sessionPresetNameConflict) Text("Another saved session already uses this name. Choose a different name.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(newSessionLimit, { newSessionLimit = it.filter(Char::isDigit).take(3) }, label = { Text("Question limit (1–500)") }, singleLine = true)
                    Text("Study mode", style = MaterialTheme.typography.labelMedium)
                    listOf("Due", "All", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam").chunked(3).forEach { rowModes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowModes.forEach { option ->
                                FilterChip(selected = mode == option, onClick = { mode = option }, label = { Text(option) })
                            }
                        }
                    }
                    Box {
                        OutlinedButton(onClick = { collectionMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(collections.firstOrNull { it.id == newSessionCollectionId }?.name ?: "All collections")
                        }
                        DropdownMenu(expanded = collectionMenuExpanded, onDismissRequest = { collectionMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("All collections") }, onClick = { newSessionCollectionId = ""; collectionMenuExpanded = false })
                            collections.forEach { collection -> DropdownMenuItem(text = { Text(collection.name) }, onClick = { newSessionCollectionId = collection.id; collectionMenuExpanded = false }) }
                        }
                    }
                    OutlinedTextField(newSessionSubject, { newSessionSubject = it.take(120) }, label = { Text("Subject (optional)") }, singleLine = true)
                    OutlinedTextField(newSessionChapter, { newSessionChapter = it.take(120) }, label = { Text("Chapter (optional)") }, singleLine = true)
                    OutlinedTextField(newSessionTag, { newSessionTag = it.take(80) }, label = { Text("Tag (optional)") }, singleLine = true)
                    OutlinedTextField(newSessionExam, { newSessionExam = it.take(120) }, label = { Text("Exam / syllabus (optional)") }, singleLine = true)
                    Text("Difficulty", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Easy", "Medium", "Hard", "Unrated").forEach { d -> FilterChip(selected = newSessionDifficulty == d, onClick = { newSessionDifficulty = d }, label = { Text(d) }) }
                    }
                    Text("Question type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Flashcard", "MCQ", "True/False", "Numerical", "Cloze").forEach { v -> FilterChip(selected = newSessionType == v, onClick = { newSessionType = v }, label = { Text(v) }) }
                    }
                    Text("Status", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Draft", "Verified").forEach { v -> FilterChip(selected = newSessionStatus == v, onClick = { newSessionStatus = v }, label = { Text(v) }) }
                    }
                    Text("Performance", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Unattempted", "Weak", "Strong").forEach { v -> FilterChip(selected = newSessionPerformance == v, onClick = { newSessionPerformance = v }, label = { Text(v) }) }
                    }
                    Text("Topic is taken from the current Topic mode selection.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val limit = newSessionLimit.toIntOrNull()?.coerceIn(1, 500) ?: 20
                    if (newSessionName.isNotBlank()) {
                        val existing = StudySessionPresetRules.existingForSave(sessionPresets, editingSessionPresetId, newSessionName)
                        onSaveSessionPreset(
                            StudySessionPresetEntity(
                                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                                name = newSessionName.trim(),
                                mode = mode,
                                subject = newSessionSubject.trim(),
                                chapter = newSessionChapter.trim(),
                                topic = selectedTopic,
                                tag = newSessionTag.trim(),
                                difficulty = newSessionDifficulty,
                                exam = newSessionExam.trim(),
                                questionType = newSessionType,
                                status = newSessionStatus,
                                performance = newSessionPerformance,
                                collectionId = newSessionCollectionId,
                                questionLimit = limit,
                                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        editingSessionPresetId = null
                        showSaveSessionDialog = false
                    }
                }, enabled = newSessionName.isNotBlank() && !sessionPresetNameConflict) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSaveSessionDialog = false; editingSessionPresetId = null }) { Text("Cancel") } }
        )
    }

    sessionPresetPendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { sessionPresetPendingDelete = null },
            title = { Text("Delete saved session?") },
            text = { Text("\"${preset.name}\" will be removed. Your questions and review history will not be affected.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSessionPreset(preset)
                    if (activeSessionPresetId == preset.id) activeSessionPresetId = null
                    if (editingSessionPresetId == preset.id) { editingSessionPresetId = null; showSaveSessionDialog = false }
                    sessionPresetPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { sessionPresetPendingDelete = null }) { Text("Cancel") } }
        )
    }

    if (mode == "Exam") {
        if (queueLoading) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Loading exam questions…")
            }
        } else if (queueError != null) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(queueError.orEmpty(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { activeQueueFingerprint = ""; queueRefreshToken++ }) { Text("Retry") }
            }
        } else {
            ExamModeScreen(questions = activeQuestions, loadContentBlocks = loadContentBlocks, loadQuestionParts = loadQuestionParts, settings = settings, onSaveExamAttempt = onSaveExamAttempt, onReview = onReview, onExit = { mode = "Due" })
        }
        return
    }

    // Keep the study workspace compact: the bottom navigation already names this tab,
    // and removing a duplicate page heading gives the question and revealed answer room.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saved sessions", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = {
                editingSessionPresetId = null
                newSessionName = ""
                newSessionLimit = "20"
                newSessionSubject = ""
                newSessionChapter = ""
                newSessionTag = ""
                newSessionDifficulty = "All"
                newSessionExam = ""
                newSessionType = "All"
                newSessionStatus = "All"
                newSessionPerformance = "All"
                newSessionCollectionId = ""
                showSaveSessionDialog = true
            }) { Text("Save current") }
        }
        if (sessionPresets.isNotEmpty()) {
            sessionPresets.forEach { preset ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = activeSessionPresetId == preset.id,
                        onClick = {
                            activeSessionPresetId = preset.id
                            mode = preset.mode.takeIf { it in StudySessionRules.supportedModes } ?: "Practice"
                            selectedTopic = preset.topic
                            index = 0
                        },
                        label = { Text("${preset.name} • ${preset.questionLimit}${preset.collectionId.takeIf { it.isNotBlank() }?.let { " • ${collections.firstOrNull { c -> c.id == it }?.name ?: "Collection"}" } ?: ""}") },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        activeSessionPresetId = preset.id
                        editingSessionPresetId = preset.id
                        mode = preset.mode.takeIf { it in StudySessionRules.supportedModes } ?: "Practice"
                        selectedTopic = preset.topic
                        newSessionName = preset.name
                        newSessionLimit = preset.questionLimit.coerceIn(1, 500).toString()
                        newSessionSubject = preset.subject
                        newSessionChapter = preset.chapter
                        newSessionTag = preset.tag
                        newSessionDifficulty = preset.difficulty
                        newSessionExam = preset.exam
                        newSessionType = preset.questionType
                        newSessionStatus = preset.status
                        newSessionPerformance = preset.performance
                        newSessionCollectionId = preset.collectionId
                        showSaveSessionDialog = true
                    }) { Text("Edit") }
                    TextButton(onClick = { sessionPresetPendingDelete = preset }) { Text("Delete") }
                }
            }
        }
        if (activeSessionPreset != null) {
            Text("Session: ${activeSessionPreset.name} • filters are applied to this study queue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Due", "All", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam").forEach { item ->
                FilterChip(selected = mode == item, onClick = { activeSessionPresetId = null; mode = item; if (item == "Topic" && selectedTopic.isBlank()) selectedTopic = topics.firstOrNull().orEmpty() }, label = { Text(item) })
            }
        }
        if (mode == "Topic" && topics.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                topics.forEach { topic -> FilterChip(selected = selectedTopic == topic, onClick = { activeSessionPresetId = null; selectedTopic = topic }, label = { Text(topic) }) }
            }
        }
        when (mode) {
            "Due" -> Text("Due: ${activeQuestions.size} in this session • New remaining today: $remainingNew • Reviews remaining today: $remainingReviews", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Practice" -> Text("Practice mode never changes your FSRS schedule or review history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Weakest" -> Text("Questions with the most recorded incorrect answers appear first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Ordered" -> Text("Questions are grouped by subject, chapter, and topic.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Topic" -> Text("Focused topic: ${selectedTopic.ifBlank { "choose a topic" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Step-by-step" -> Text("Reveal the answer first, then reveal the explanation as a separate step.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Text("Study selection: $mode • ${activeQuestions.size} questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (queueLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Loading study queue…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        if (queueError != null) {
            Text(queueError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { activeQueueFingerprint = ""; queueRefreshToken++ }) { Text("Retry") }
            return@Column
        }
        if (activeQuestions.isEmpty()) {
            StudyEmptyState(
                title = if (mode == "Due") "You're caught up" else "Nothing to study yet",
                message = if (mode == "Due") "No cards are due within today's configured limits. Try Practice or return tomorrow." else "Add flashcards or MCQs in your library, then return here."
            )
            return@Column
        }

        val safeIndex = index.coerceIn(0, activeQuestions.lastIndex)
        val q = activeQuestions[safeIndex]
        var activeQuestionBlocks by remember(q.id) { mutableStateOf<List<QuestionContentBlockEntity>>(emptyList()) }
        var contentBlocksLoaded by remember(q.id) { mutableStateOf(false) }
        LaunchedEffect(q.id) {
            contentBlocksLoaded = false
            activeQuestionBlocks = runCatching { loadContentBlocks(q.id).sortedBy { it.position } }.getOrDefault(emptyList())
            contentBlocksLoaded = true
        }
        val numericalTemplate = remember(activeQuestionBlocks, q.id) {
            if (q.type == "Numerical") NumericalTemplateCodec.fromContentBlocks(q.id, activeQuestionBlocks).getOrNull()?.takeIf { it.authoringStatus == "VERIFIED" } else null
        }
        val studyAttemptKey = queueFingerprint
        val numericalVariant = remember(numericalTemplate, studyAttemptKey, q.id) {
            numericalTemplate?.let { NumericalTemplateAttemptRules.generate(it, studyAttemptKey, q.id).getOrNull() }
        }
        val numericalMcq = remember(numericalTemplate, studyAttemptKey, q.id) {
            numericalTemplate?.let { NumericalTemplateEngine.generateMultipleChoice(it, NumericalTemplateAttemptRules.seedFor(studyAttemptKey, q.id)).getOrNull() }
        }
        val displayPrompt = numericalVariant?.prompt ?: q.prompt
        val displayExplanation = numericalVariant?.explanation?.ifBlank { q.explanation } ?: q.explanation
        val displayNumericalAnswer = numericalVariant?.let {
            NumericalTemplateEngine.format(it.answer, numericalTemplate?.decimalPlaces ?: 2, numericalTemplate?.significantFigures ?: 0) + it.answerUnit.let { unit -> if (unit.isBlank()) "" else " $unit" }
        } ?: q.answer
        fun numericalCorrect(response: String): Boolean = if (numericalTemplate != null && numericalVariant != null) {
            NumericalTemplateEngine.isCorrect(numericalTemplate, numericalVariant, response)
        } else response.trim().replace(",", "") == q.answer.trim().replace(",", "")
        val progressTotal = (dueCompletedCount + activeQuestions.size).coerceAtLeast(1)
        val progressPosition = if (mode == "Due") dueCompletedCount + safeIndex + 1 else safeIndex + 1
        var revealedHintIds by remember(q.id) { mutableStateOf(emptySet<String>()) }
        val options = remember(q.id, q.optionsJson) {
            runCatching { JSONArray(q.optionsJson).let { array -> (0 until array.length()).map { array.optString(it) } } }.getOrDefault(emptyList())
        }
        val clozeAnswers = remember(q.id, q.prompt) { ClozeRules.answers(q.prompt) }
        val maskedClozePrompt = remember(q.id, q.prompt) { ClozeRules.mask(q.prompt) }
        Text("Card $progressPosition of $progressTotal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = progressPosition.toFloat() / progressTotal.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(if (revealed || checked) 14.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(if (revealed || checked) 9.dp else 11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(q.type.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                }
                RichContentText(
                    if (q.type == "Cloze" && !revealed) maskedClozePrompt else displayPrompt,
                    style = if (revealed || checked) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                )
                if (!contentBlocksLoaded) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                activeQuestionBlocks.forEach { block ->
                    when (block.kind.uppercase()) {
                        "LATEX" -> if (block.text.isNotBlank()) LatexFormulaView(block.text)
                        "HEADING" -> Text(block.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        "HINT" -> if (block.text.isNotBlank()) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Hint", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    if (block.id in revealedHintIds) RichContentText(block.text)
                                    else OutlinedButton(onClick = { revealedHintIds = revealedHintIds + block.id }) { Text("Reveal hint") }
                                }
                            }
                        }
                        "LIST" -> block.text.lineSequence().filter { it.isNotBlank() }.forEach { line -> RichContentText(if (line.trimStart().startsWith("-")) line else "- $line") }
                        "IMAGE" -> if (block.mediaBase64.isNotBlank()) OfflineImageBlock(block.mediaBase64, block.altText)
                        "TABLE" -> if (block.text.isNotBlank()) ContentTable(block.text)
                        "GRAPH" -> if (block.text.isNotBlank()) ContentGraph(block.text)
                        "DERIVATION" -> DerivationBlock(block.text)
                        else -> RichContentText(block.text)
                    }
                }
                val metadata = listOf(q.subject, q.chapter, q.topic).filter { it.isNotBlank() }.joinToString(" • ")
                val sourceMetadata = listOf(q.sourceTitle, q.sourceDetail).filter { it.isNotBlank() }.joinToString(" · ")
                if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sourceMetadata.isNotBlank()) Text("Source: $sourceMetadata", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (q.learningState == "Learning" || q.learningState == "Relearning") {
                    val configuredSteps = ReviewScheduler.parseSteps(
                        if (q.learningState == "Relearning") settings.relearningStepsMinutesCsv else settings.learningStepsMinutesCsv
                    ).ifEmpty { listOf(10) }
                    val currentStep = q.learningStepIndex.coerceIn(0, configuredSteps.lastIndex)
                    Text(
                        "${q.learningState} • Step ${currentStep + 1} of ${configuredSteps.size} (${configuredSteps[currentStep]} min)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (q.latex.isNotBlank()) { HorizontalDivider(); LatexFormulaView(q.latex, Modifier.fillMaxWidth()) }

                if ((q.type == "MCQ" || q.type == "True/False") && options.isNotEmpty()) {
                    options.forEachIndexed { optionIndex, option ->
                        val prefix = ('A'.code + optionIndex).toChar()
                        val isCorrectOption = checked && option == q.answer
                        val background = when {
                            isCorrectOption -> MaterialTheme.colorScheme.tertiaryContainer
                            selectedAnswer == option -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(modifier = Modifier.fillMaxWidth().clickable(enabled = !checked) { selectedAnswer = option }, colors = CardDefaults.cardColors(containerColor = background)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$prefix.", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(10.dp))
                                Text(option, modifier = Modifier.weight(1f))
                                if (isCorrectOption) Text("Correct", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (!checked) {
                        Button(onClick = { checked = true }, enabled = selectedAnswer != null, modifier = Modifier.fillMaxWidth()) { Text("Check answer") }
                    } else {
                        val right = selectedAnswer == q.answer
                        Text(if (right) "Correct answer" else "Not quite — review the correct answer.", fontWeight = FontWeight.Bold, color = if (right) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        RichContentText("Answer: ${q.answer}", style = MaterialTheme.typography.titleMedium)
                        if (!right && mode != "Practice") {
                            Text("What caused the mistake?", fontWeight = FontWeight.SemiBold)
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                mistakeCategories.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        pair.forEach { category -> FilterChip(selected = mistakeCategory == category, onClick = { mistakeCategory = if (mistakeCategory == category) "" else category }, label = { Text(category) }) }
                                    }
                                }
                            }
                        }
                    }
                } else if (q.type == "Numerical") {
                    if (numericalTemplate != null && numericalVariant == null) {
                        Text("This numerical template could not generate a valid question. Check its variables and constraints in the editor.", color = MaterialTheme.colorScheme.error)
                    } else if (!checked) {
                        if (numericalMcq != null) {
                            numericalMcq.options.forEachIndexed { optionIndex, option ->
                                Card(modifier = Modifier.fillMaxWidth().clickable { selectedAnswer = option.label }, colors = CardDefaults.cardColors(containerColor = if (selectedAnswer == option.label) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${('A'.code + optionIndex).toChar()}.  ", fontWeight = FontWeight.Bold); Text(option.label)
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(value = selectedAnswer.orEmpty(), onValueChange = { selectedAnswer = it }, label = { Text("Your numerical answer") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        Button(onClick = { checked = true }, enabled = !selectedAnswer.isNullOrBlank(), modifier = Modifier.fillMaxWidth()) { Text("Check answer") }
                    } else {
                        val correct = numericalCorrect(selectedAnswer.orEmpty())
                        Text(if (correct) "Correct answer" else "Check the expected value below", fontWeight = FontWeight.Bold, color = if (correct) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        RichContentText("Answer: $displayNumericalAnswer", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    if (!revealed) Button(onClick = { revealed = true; stepByStepStage = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Show answer") }
                    else {
                        HorizontalDivider()
                        Text("Answer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        val revealedAnswer = if (q.type == "Cloze") clozeAnswers.joinToString("; ").ifBlank { q.answer } else q.answer
                        RichContentText(revealedAnswer, style = MaterialTheme.typography.titleMedium)
                        if (mode == "Step-by-step" && stepByStepStage < 2) {
                            Button(onClick = { stepByStepStage = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Next: reveal explanation") }
                        }
                    }
                }
                if ((revealed || checked) && displayExplanation.isNotBlank() && (mode != "Step-by-step" || stepByStepStage >= 2 || q.type == "MCQ")) {
                    HorizontalDivider()
                    Text("Explanation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    RichContentText(displayExplanation)
                }
            }
        }

        if ((revealed || checked) && (mode != "Step-by-step" || stepByStepStage >= 2 || q.type == "MCQ")) {
            if (mode == "Practice") {
                Button(onClick = { index = (safeIndex + 1) % activeQuestions.size; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Next question — no scheduling") }
            } else {
                Text("How well did you know this?", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(ReviewScheduler.AGAIN, ReviewScheduler.HARD, ReviewScheduler.GOOD, ReviewScheduler.EASY).forEach { rating ->
                        OutlinedButton(onClick = {
                            val wasCorrect = when (q.type) {
                                "MCQ", "True/False" -> selectedAnswer == q.answer
                                "Numerical" -> numericalCorrect(selectedAnswer.orEmpty())
                                else -> null
                            }
                            val reviewMode = when (q.learningState) {
                                "New" -> "New"
                                "Learning" -> "Learning"
                                "Relearning" -> "Relearning"
                                else -> if (mode == "Exam") "Exam" else "Review"
                            }
                            onReview(q, rating, wasCorrect, if (wasCorrect == false) mistakeCategory else null, reviewMode)
                            if (mode == "Due") {
                                activeQuestions = activeQuestions.filterNot { it.id == q.id }
                                activeQueueIds = activeQueueIds.filterNot { it == q.id }
                                dueCompletedCount++
                                index = 0
                            } else index = (safeIndex + 1) % activeQuestions.size
                            revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0
                        }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
                            Text(rating, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
        if (mode != "Due") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { index = if (safeIndex == 0) activeQuestions.lastIndex else safeIndex - 1; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0 }, modifier = Modifier.weight(1f)) { Text("Previous") }
                OutlinedButton(onClick = { index = (safeIndex + 1) % activeQuestions.size; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0 }, modifier = Modifier.weight(1f)) { Text("Next") }
            }
        }
        Text("Ratings: Again, Hard, Good, and Easy update FSRS memory state. Practice mode does not record reviews.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExamModeScreen(
    questions: List<QuestionEntity>,
    loadContentBlocks: suspend (String) -> List<QuestionContentBlockEntity>,
    loadQuestionParts: suspend (List<String>) -> List<QuestionPartEntity>,
    settings: StudySettingsEntity,
    onSaveExamAttempt: (ExamAttemptEntity) -> Unit,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit,
    onExit: () -> Unit
) {
    var questionParts by remember { mutableStateOf<List<QuestionPartEntity>>(emptyList()) }
    var examContentBlocks by remember { mutableStateOf<Map<String, List<QuestionContentBlockEntity>>>(emptyMap()) }
    var partsLoading by remember { mutableStateOf(true) }
    var partsLoadError by remember { mutableStateOf<String?>(null) }
    val examQuestionIdsKey = remember(questions) { questions.joinToString("|") { it.id } }
    LaunchedEffect(examQuestionIdsKey) {
        partsLoading = true
        partsLoadError = null
        try {
            questionParts = loadQuestionParts(questions.map { it.id })
            examContentBlocks = questions.filter { it.type == "Numerical" }.associate { q -> q.id to loadContentBlocks(q.id) }
        } catch (error: Exception) {
            questionParts = emptyList()
            partsLoadError = error.message ?: "Unable to load multipart exam data"
        } finally {
            partsLoading = false
        }
    }
    val partsByQuestion = remember(questionParts) { questionParts.groupBy { it.questionId }.mapValues { it.value.sortedBy { part -> part.position } } }
    val eligible = remember(questions, partsByQuestion) {
        questions.filter { q ->
            val hasParts = partsByQuestion[q.id].orEmpty().isNotEmpty()
            val hasMcq = q.type in setOf("MCQ", "True/False") && runCatching { JSONArray(q.optionsJson).length() >= 2 }.getOrDefault(false)
            val hasNumerical = q.type == "Numerical"
            hasParts || hasMcq || hasNumerical
        }
    }
    var examQuestions by remember { mutableStateOf(emptyList<QuestionEntity>()) }
    val answers = remember { mutableStateMapOf<String, String>() }
    val marked = remember { mutableStateMapOf<String, Boolean>() }
    val manualPartScores = remember { mutableStateMapOf<String, String>() }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(settings.examDurationMinutes * 60) }
    var started by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var confirmSubmit by remember { mutableStateOf(false) }
    var startedAt by rememberSaveable { mutableStateOf(0L) }
    var attemptSaved by remember { mutableStateOf(false) }

    fun partsFor(q: QuestionEntity) = partsByQuestion[q.id].orEmpty()
    fun numericalTemplateFor(q: QuestionEntity) = NumericalTemplateCodec.fromContentBlocks(q.id, examContentBlocks[q.id].orEmpty()).getOrNull()?.takeIf { it.authoringStatus == "VERIFIED" }
    fun numericalVariantFor(q: QuestionEntity): NumericalTemplateEngine.Variant? {
        val template = numericalTemplateFor(q) ?: return null
        return NumericalTemplateAttemptRules.generate(template, "exam-$startedAt", q.id).getOrNull()
    }
    fun numericalMcqFor(q: QuestionEntity): NumericalTemplateEngine.MultipleChoiceVariant? {
        val template = numericalTemplateFor(q) ?: return null
        return NumericalTemplateEngine.generateMultipleChoice(template, NumericalTemplateAttemptRules.seedFor("exam-$startedAt", q.id)).getOrNull()
    }
    fun numericalIsCorrect(q: QuestionEntity, response: String): Boolean {
        val template = numericalTemplateFor(q) ?: return response.trim().replace(",", "") == q.answer.trim().replace(",", "")
        val mcq = numericalMcqFor(q)
        if (mcq != null) return mcq.options.any { it.isCorrect && it.label == response }
        val variant = numericalVariantFor(q) ?: return false
        return NumericalTemplateEngine.isCorrect(template, variant, response)
    }
    fun hasAnswer(q: QuestionEntity): Boolean = if (partsFor(q).isNotEmpty()) partsFor(q).any { answers[it.id]?.isNotBlank() == true } else answers[q.id]?.isNotBlank() == true
    fun earned(q: QuestionEntity): Double = if (partsFor(q).isNotEmpty()) {
        val qParts = partsFor(q)
        val answerMap = answers.filterKeys { key -> qParts.any { it.id == key } }
        val manual = qParts.mapNotNull { part -> manualPartScores[part.id]?.toDoubleOrNull()?.let { part.id to it } }.toMap()
        MultipartExamRules.score(qParts, answerMap, manual).earned
    } else if (if (q.type == "Numerical") numericalIsCorrect(q, answers[q.id].orEmpty()) else answers[q.id] == q.answer) 1.0 else 0.0
    fun possible(q: QuestionEntity): Double = if (partsFor(q).isNotEmpty()) MultipartExamRules.score(partsFor(q), emptyMap()).possible else 1.0
    fun finishExam() {
        if (submitted) return
        submitted = true
    }

    LaunchedEffect(started, submitted) {
        if (started && !submitted) {
            while (remainingSeconds > 0) { delay(1000); remainingSeconds -= 1 }
            if (!submitted && remainingSeconds == 0) finishExam()
        }
    }

    if (!started) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Exam mode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Timed exams support MCQs, true/false, tolerance-graded numerical templates and multipart questions. Multipart scores use each part's assigned points.")
            if (partsLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Loading multipart eligibility…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (partsLoadError != null) {
                Text(partsLoadError.orEmpty(), color = MaterialTheme.colorScheme.error)
            } else {
                Text("Eligible questions loaded: ${eligible.size} (exam pool capped at 500)")
            }
            Text("Configured exam: ${settings.examQuestionCount} questions • ${settings.examDurationMinutes} minutes • ${if (settings.examRandomize) "random order" else "library order"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                val ordered = if (settings.examRandomize) eligible.shuffled() else eligible.sortedBy { it.createdAt }
                examQuestions = ordered.take(settings.examQuestionCount)
                answers.clear(); marked.clear(); manualPartScores.clear(); currentIndex = 0; remainingSeconds = settings.examDurationMinutes * 60; submitted = false; attemptSaved = false; startedAt = System.currentTimeMillis(); started = true
            }, enabled = !partsLoading && partsLoadError == null && eligible.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Start exam") }
            OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Back to study") }
            if (!partsLoading && partsLoadError == null && eligible.isEmpty()) Text("Add an MCQ, numerical question or a question with authored parts to start an exam.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    if (submitted) {
        val totalEarned = examQuestions.sumOf { earned(it) }
        val totalPossible = examQuestions.sumOf { possible(it) }
        val percent = if (totalPossible <= 0.0) 0 else (100.0 * totalEarned / totalPossible).toInt()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Exam results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${"%.2f".format(totalEarned)} / ${"%.2f".format(totalPossible)} points", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("$percent% weighted score")
                    Text("Marked: ${marked.values.count { it }} • Unanswered: ${examQuestions.count { !hasAnswer(it) }}", style = MaterialTheme.typography.bodySmall)
                }
            }
            examQuestions.forEachIndexed { i, q ->
                val qParts = partsFor(q)
                val score = earned(q)
                val max = possible(q)
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Question ${i + 1} • ${"%.2f".format(score)} / ${"%.2f".format(max)}${if (marked[q.id] == true) " • Marked" else ""}", fontWeight = FontWeight.Bold)
                        RichContentText(numericalVariantFor(q)?.prompt ?: q.prompt)
                        if (qParts.isNotEmpty()) qParts.forEach { part ->
                            val response = answers[part.id].orEmpty()
                            val correct = MultipartExamRules.isCorrect(part, response)
                            val automaticPoints = if (correct) part.points else 0.0
                            val awardedText = manualPartScores[part.id] ?: "${"%.2f".format(automaticPoints)}"
                            Text(part.prompt, fontWeight = FontWeight.Medium)
                            Text("Automatic marking: ${"%.2f".format(automaticPoints)} / ${part.points} pts")
                            OutlinedTextField(
                                value = awardedText,
                                onValueChange = { value ->
                                    if (value.length <= 16 && value.matches(Regex("[0-9]*([.]?[0-9]*)?"))) manualPartScores[part.id] = value
                                },
                                label = { Text("Examiner-awarded points (0–${part.points})") },
                                supportingText = { Text("Adjust for method marks or other partial credit. Values are capped at the part maximum.") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Your answer: ${response.ifBlank { "Not answered" }}")
                            Text("Expected answer: ${part.answer}", fontWeight = FontWeight.Medium)
                            if (part.explanation.isNotBlank()) RichContentText(part.explanation)
                        } else {
                            Text("Your answer: ${answers[q.id] ?: "Not answered"}")
                            val generatedMcq = if (q.type == "Numerical") numericalMcqFor(q) else null
                            val exactAnswer = generatedMcq?.options?.firstOrNull { it.isCorrect }?.label
                                ?: if (q.type == "Numerical") numericalVariantFor(q)?.let { variant ->
                                    val template = numericalTemplateFor(q)
                                    NumericalTemplateEngine.format(variant.answer, template?.decimalPlaces ?: 2, template?.significantFigures ?: 0) + variant.answerUnit.let { if (it.isBlank()) "" else " $it" }
                                } ?: q.answer else q.answer
                            Text("Correct answer: $exactAnswer", fontWeight = FontWeight.SemiBold)
                            if (q.explanation.isNotBlank()) RichContentText(q.explanation)
                        }
                    }
                }
            }
            Button(onClick = {
                if (!attemptSaved) {
                    val itemSnapshots = JSONArray().apply {
                        examQuestions.forEachIndexed { index, question ->
                            val qParts = partsFor(question)
                            put(JSONObject()
                                .put("position", index + 1).put("questionId", question.id)
                                .put("prompt", numericalVariantFor(question)?.prompt ?: question.prompt).put("subject", question.subject)
                                .put("chapter", question.chapter).put("topic", question.topic)
                                .put("questionType", question.type).put("optionsJson", question.optionsJson)
                                .put("generatedNumericalOptions", numericalMcqFor(question)?.let { generated -> JSONArray().apply { generated.options.forEach { put(JSONObject().put("label", it.label).put("correct", it.isCorrect)) } } } ?: JSONObject.NULL)
                                .put("response", if (qParts.isEmpty()) answers[question.id].orEmpty() else "")
                                .put("correctAnswer", if (qParts.isEmpty()) {
                                    val template = numericalTemplateFor(question)
                                    val variant = numericalVariantFor(question)
                                    if (template != null && variant != null) NumericalTemplateEngine.format(variant.answer, template.decimalPlaces, template.significantFigures) + template.answerUnit.let { if (it.isBlank()) "" else " $it" } else question.answer
                                } else "")
                                .put("numericalAttemptSeed", if (numericalTemplateFor(question) != null) NumericalTemplateAttemptRules.seedFor("exam-$startedAt", question.id) else JSONObject.NULL)
                                .put("numericalTemplateVersion", numericalTemplateFor(question)?.version ?: JSONObject.NULL)
                                .put("earnedPoints", earned(question)).put("possiblePoints", possible(question))
                                .put("answered", hasAnswer(question)).put("marked", marked[question.id] == true)
                                .put("parts", JSONArray().apply {
                                    qParts.forEach { part ->
                                        val response = answers[part.id].orEmpty()
                                        val autoCorrect = MultipartExamRules.isCorrect(part, response)
                                        val autoPoints = if (autoCorrect) part.points else 0.0
                                        val awarded = manualPartScores[part.id]?.toDoubleOrNull()?.coerceIn(0.0, part.points) ?: autoPoints
                                        put(JSONObject().put("partId", part.id).put("prompt", part.prompt)
                                            .put("response", response).put("correctAnswer", part.answer)
                                            .put("automaticPoints", autoPoints).put("awardedPoints", awarded)
                                            .put("possiblePoints", part.points))
                                    }
                                }))
                        }
                    }
                    val earnedTotal = examQuestions.sumOf { earned(it) }
                    val possibleTotal = examQuestions.sumOf { possible(it) }
                    val now = System.currentTimeMillis()
                    onSaveExamAttempt(ExamAttemptEntity(
                        id = java.util.UUID.randomUUID().toString(), startedAt = startedAt,
                        completedAt = now, durationSeconds = ((settings.examDurationMinutes * 60) - remainingSeconds).coerceAtLeast(0),
                        questionCount = examQuestions.size, answeredCount = examQuestions.count { hasAnswer(it) },
                        markedCount = marked.values.count { it }, earnedPoints = earnedTotal,
                        possiblePoints = possibleTotal,
                        scorePercent = ExamAttemptRules.percentage(earnedTotal, possibleTotal),
                        itemsJson = itemSnapshots.toString()
                    ))
                    attemptSaved = true
                    examQuestions.forEach { question ->
                        if (hasAnswer(question)) {
                            val full = earned(question) >= possible(question) && possible(question) > 0.0
                            onReview(question, if (full) ReviewScheduler.GOOD else ReviewScheduler.AGAIN, full,
                                if (full) null else "Exam answer incorrect or incomplete", if (question.learningState == "New") "New" else "Exam")
                        }
                    }
                }
                started = false; submitted = false; examQuestions = emptyList(); onExit()
            }, enabled = !attemptSaved, modifier = Modifier.fillMaxWidth()) { Text(if (attemptSaved) "Saved" else "Save grading and finish") }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, examQuestions.lastIndex)
    val q = examQuestions[safeIndex]
    val qParts = partsFor(q)
    val options = remember(q.id, q.optionsJson) { runCatching { JSONArray(q.optionsJson).let { a -> (0 until a.length()).map { a.optString(it) } } }.getOrDefault(emptyList()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Exam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}", color = if (remainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Text("Question ${safeIndex + 1} of ${examQuestions.size}${if (marked[q.id] == true) " • Marked for review" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            examQuestions.forEachIndexed { ix, candidate ->
                val status = if (hasAnswer(candidate)) "✓" else "·"
                FilterChip(selected = ix == safeIndex, onClick = { currentIndex = ix }, label = { Text("${ix + 1} $status${if (marked[candidate.id] == true) "!" else ""}") })
            }
        }
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RichContentText(numericalVariantFor(q)?.prompt ?: q.prompt, style = MaterialTheme.typography.titleLarge)
                if (q.latex.isNotBlank()) LatexFormulaView(q.latex, Modifier.fillMaxWidth())
                if (qParts.isNotEmpty()) {
                    qParts.forEachIndexed { partIndex, part ->
                        HorizontalDivider()
                        Text("Part ${partIndex + 1} • ${part.points} points", fontWeight = FontWeight.Bold)
                        RichContentText(part.prompt)
                        val partOptions = runCatching { JSONArray(part.optionsJson).let { a -> (0 until a.length()).map { a.optString(it) } } }.getOrDefault(emptyList())
                        if (partOptions.size >= 2) {
                            partOptions.forEach { option ->
                                Card(modifier = Modifier.fillMaxWidth().clickable { answers[part.id] = option }, colors = CardDefaults.cardColors(containerColor = if (answers[part.id] == option) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                                    Text(option, Modifier.padding(12.dp))
                                }
                            }
                        } else {
                            OutlinedTextField(value = answers[part.id].orEmpty(), onValueChange = { answers[part.id] = it.take(2000) }, label = { Text("Your answer") }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else if (q.type == "Numerical") {
                    val generatedMcq = numericalMcqFor(q)
                    if (numericalTemplateFor(q) != null && generatedMcq == null) {
                        Text("This numerical template could not generate a valid question and choice set.", color = MaterialTheme.colorScheme.error)
                    } else if (generatedMcq != null) {
                        generatedMcq.options.forEachIndexed { optionIndex, option ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { answers[q.id] = option.label }, colors = CardDefaults.cardColors(containerColor = if (answers[q.id] == option.label) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${('A'.code + optionIndex).toChar()}.  ", fontWeight = FontWeight.Bold); Text(option.label)
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(value = answers[q.id].orEmpty(), onValueChange = { answers[q.id] = it.take(100) }, label = { Text("Your numerical answer") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                } else {
                    options.forEachIndexed { optionIndex, option ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { answers[q.id] = option }, colors = CardDefaults.cardColors(containerColor = if (answers[q.id] == option) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${('A'.code + optionIndex).toChar()}.  ", fontWeight = FontWeight.Bold); Text(option)
                            }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = { marked[q.id] = marked[q.id] != true }, modifier = Modifier.fillMaxWidth()) { Text(if (marked[q.id] == true) "Remove review mark" else "Mark for review") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { currentIndex = (safeIndex - 1).coerceAtLeast(0) }, enabled = safeIndex > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { currentIndex = (safeIndex + 1).coerceAtMost(examQuestions.lastIndex) }, enabled = safeIndex < examQuestions.lastIndex, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        Text("Answered: ${examQuestions.count { hasAnswer(it) }} / ${examQuestions.size} • Marked: ${marked.values.count { it }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { confirmSubmit = true }, modifier = Modifier.fillMaxWidth()) { Text("Submit exam") }
    }
    if (confirmSubmit) AlertDialog(
        onDismissRequest = { confirmSubmit = false },
        title = { Text("Submit exam?") },
        text = { Text("You answered ${examQuestions.count { hasAnswer(it) }} of ${examQuestions.size} questions. Unanswered questions will not be added to review history.") },
        confirmButton = { TextButton(onClick = { confirmSubmit = false; finishExam() }) { Text("Submit") } },
        dismissButton = { TextButton(onClick = { confirmSubmit = false }) { Text("Continue exam") } }
    )
}

@Composable
fun ProgressScreen(
    coreStats: ProgressCoreStats,
    typeAccuracy: List<ProgressAccuracyRow>,
    weakTopics: List<ProgressCountRow>,
    weakSubjects: List<ProgressCountRow>,
    weakChapters: List<ProgressCountRow>,
    mistakes: List<ProgressCountRow>,
    recentReviews: List<ProgressRecentReviewRow>,
    dailyReviews: List<ProgressDailyReviewRow>,
    examStats: ProgressExamStats,
    examResponseStats: ProgressExamResponseStats,
    recentExamAttempts: List<ExamAttemptEntity>
) {
    var expandedAttemptId by remember { mutableStateOf<String?>(null) }
    val due = coreStats.dueCount
    val newCount = coreStats.newCount
    val learning = coreStats.learningCount
    val mastered = coreStats.masteredCount
    val accuracy = if (coreStats.scoredReviews == 0) 0 else coreStats.correctReviews * 100 / coreStats.scoredReviews
    val recentAccuracy = if (coreStats.recentScoredReviews == 0) 0 else coreStats.recentCorrectReviews * 100 / coreStats.recentScoredReviews
    val lastSevenDays = coreStats.lastSevenDays
    val averageExamScore = examStats.averageScore.toInt()
    val bestExamScore = examStats.bestScore.toInt()
    val medianExamScore = examStats.medianScore.toInt()
    val examTrendLabel = examStats.recentTrend?.let { delta ->
        val sign = if (delta > 0.0) "+" else ""
        "$sign${"%.1f".format(delta)} percentage points across the latest five vs previous five"
    } ?: "Complete at least 10 exams to see a five-attempt trend."
    val trendDateFormat = remember { DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()) }
    val today = java.time.LocalDate.now(ZoneId.systemDefault())
    val dailyByDate = dailyReviews.associateBy { it.dayKey }
    val trendDays = (13 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        date to dailyByDate[date.toString()]
    }
    val activeDays = trendDays.count { ( _, row) -> (row?.totalReviews ?: 0) > 0 }
    var streak = 0
    var streakCursor = if ((dailyByDate[today.toString()]?.totalReviews ?: 0) > 0) today else today.minusDays(1)
    while (streakCursor >= today.minusDays(13) && (dailyByDate[streakCursor.toString()]?.totalReviews ?: 0) > 0) {
        streak++; streakCursor = streakCursor.minusDays(1)
    }
    val examBandRows = listOf(
        "0–39%" to "${examStats.band0To39} attempt(s)",
        "40–59%" to "${examStats.band40To59} attempt(s)",
        "60–79%" to "${examStats.band60To79} attempt(s)",
        "80–100%" to "${examStats.band80To100} attempt(s)"
    )

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Analytics are calculated from recorded reviews, not sample data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Due now", due.toString(), Modifier.weight(1f)); ProgressStat("Total reviews", coreStats.totalReviews.toString(), Modifier.weight(1f)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Accuracy", "$accuracy%", Modifier.weight(1f)); ProgressStat("30-day accuracy", "$recentAccuracy%", Modifier.weight(1f)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Reviews · 7 days", lastSevenDays.toString(), Modifier.weight(1f)); ProgressStat("Active review days", coreStats.activeReviewDays.toString(), Modifier.weight(1f)) }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Daily review activity · 14 days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$activeDays/14 active days • $streak-day current streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                trendDays.forEachIndexed { dayIndex, (date, row) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(date.format(trendDateFormat), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        val total = row?.totalReviews ?: 0
                        val scored = row?.scoredReviews ?: 0
                        val correct = row?.correctReviews ?: 0
                        val accuracyLabel = if (scored == 0) "" else " • ${correct * 100 / scored}% accuracy"
                        Text("$total review(s)$accuracyLabel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (dayIndex != trendDays.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Saved exams", examStats.attemptCount.toString(), Modifier.weight(1f)); ProgressStat("Average exam score", "$averageExamScore%", Modifier.weight(1f)) }
        val examAccuracy = if (examResponseStats.scored == 0) 0 else examResponseStats.correct * 100 / examResponseStats.scored
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Exam response accuracy", "$examAccuracy%", Modifier.weight(1f)); ProgressStat("Scored exam responses", examResponseStats.scored.toString(), Modifier.weight(1f)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Best exam", "$bestExamScore%", Modifier.weight(1f)); ProgressStat("Median exam", "$medianExamScore%", Modifier.weight(1f)) }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Exam performance trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(examTrendLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProgressListCard("Exam score distribution", examBandRows, "Complete an exam to build your score distribution.")
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Learning status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("New: $newCount"); Text("Learning / relearning: $learning"); Text("FSRS stability 21+ days: $mastered")
                Text("Questions in library: ${coreStats.questionCount}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProgressListCard("Accuracy by question type", typeAccuracy.map { it.label to "${if (it.scored == 0) 0 else it.correct * 100 / it.scored}% (${it.correct}/${it.scored} correct)" }, "No scored reviews available by question type yet.")
        ProgressListCard("Topics with most incorrect answers", weakTopics.map { it.label to "${it.count} incorrect" }, "No incorrect answers recorded yet.")
        ProgressListCard("Subjects with most recorded mistakes", weakSubjects.map { it.label to "${it.count} incorrect" }, "No subject-level mistakes recorded yet.")
        ProgressListCard("Chapters with most recorded mistakes", weakChapters.map { it.label to "${it.count} incorrect" }, "No chapter-level mistakes recorded yet.")
        ProgressListCard("Mistake categories", mistakes.map { it.label to "${it.count} times" }, "Mistake categories appear here when you classify an incorrect answer.")
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exam history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (recentExamAttempts.isEmpty()) Text("Completed exams will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else {
                    Text("Best score: $bestExamScore% • ${examStats.attemptCount} saved attempt(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    recentExamAttempts.forEachIndexed { index, attempt ->
                        Column(modifier = Modifier.fillMaxWidth().clickable {
                            expandedAttemptId = if (expandedAttemptId == attempt.id) null else attempt.id
                        }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${attempt.scorePercent.toInt()}% • ${"%.2f".format(attempt.earnedPoints)} / ${"%.2f".format(attempt.possiblePoints)} points", fontWeight = FontWeight.SemiBold)
                            Text("${attempt.answeredCount}/${attempt.questionCount} answered • ${attempt.durationSeconds / 60}m ${attempt.durationSeconds % 60}s • ${formatReviewDate(attempt.completedAt)} • ${if (expandedAttemptId == attempt.id) "Hide details" else "View details"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (expandedAttemptId == attempt.id) {
                                val savedItems = runCatching { JSONArray(attempt.itemsJson) }.getOrDefault(JSONArray())
                                for (itemIndex in 0 until savedItems.length()) {
                                    val item = savedItems.optJSONObject(itemIndex) ?: continue
                                    Column(Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("${item.optInt("position", itemIndex + 1)}. ${item.optString("prompt", "Question")}", fontWeight = FontWeight.Medium)
                                        Text("${"%.2f".format(item.optDouble("earnedPoints", 0.0))} / ${"%.2f".format(item.optDouble("possiblePoints", 0.0))} pts${if (item.optBoolean("marked")) " • Marked" else ""}", style = MaterialTheme.typography.bodySmall)
                                        val response = item.optString("response")
                                        if (response.isNotBlank()) Text("Your answer: $response", style = MaterialTheme.typography.bodySmall)
                                        val expected = item.optString("correctAnswer")
                                        if (expected.isNotBlank()) Text("Expected: $expected", style = MaterialTheme.typography.bodySmall)
                                        val generatedOptions = item.optJSONArray("generatedNumericalOptions")
                                        if (generatedOptions != null && generatedOptions.length() > 0) {
                                            Text("Generated numerical choices", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            for (optionIndex in 0 until generatedOptions.length()) {
                                                val option = generatedOptions.optJSONObject(optionIndex) ?: continue
                                                val label = option.optString("label")
                                                val isCorrect = option.optBoolean("correct")
                                                val isSelected = response == label
                                                val marker = when {
                                                    isCorrect && isSelected -> "✓ Correct • Your choice"
                                                    isCorrect -> "✓ Correct answer"
                                                    isSelected -> "✗ Your choice"
                                                    else -> "•"
                                                }
                                                Text("$marker  $label", style = MaterialTheme.typography.bodySmall,
                                                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            val savedOptions = runCatching { JSONArray(item.optString("optionsJson", "[]")) }.getOrDefault(JSONArray())
                                            if (savedOptions.length() > 0) {
                                                Text("Choices", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                for (optionIndex in 0 until savedOptions.length()) {
                                                    val label = savedOptions.optString(optionIndex)
                                                    val isCorrect = label == expected
                                                    val isSelected = response == label
                                                    val marker = when {
                                                        isCorrect && isSelected -> "✓ Correct • Your choice"
                                                        isCorrect -> "✓ Correct answer"
                                                        isSelected -> "✗ Your choice"
                                                        else -> "•"
                                                    }
                                                    Text("$marker  $label", style = MaterialTheme.typography.bodySmall,
                                                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                        val savedParts = item.optJSONArray("parts") ?: JSONArray()
                                        for (partIndex in 0 until savedParts.length()) {
                                            val part = savedParts.optJSONObject(partIndex) ?: continue
                                            Text("• ${part.optString("prompt")}: ${part.optString("response", "(blank)")} — ${"%.2f".format(part.optDouble("awardedPoints", 0.0))}/${"%.2f".format(part.optDouble("possiblePoints", 0.0))} pts", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                        if (index != recentExamAttempts.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (recentReviews.isEmpty()) Text("No review history yet. Complete a study session to start tracking progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else recentReviews.forEachIndexed { index, log ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(log.prompt ?: "Question no longer in library", maxLines = 2, fontWeight = FontWeight.Medium)
                        Text("${log.rating} • ${log.reviewMode} • ${formatReviewDate(log.reviewedAt)}${if (log.mistakeCategory.isNotBlank()) " • ${log.mistakeCategory}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (index != recentReviews.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ProgressStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProgressListCard(title: String, rows: List<Pair<String, String>>, emptyText: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (rows.isEmpty()) Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else rows.forEach { (label, value) -> Row(verticalAlignment = Alignment.CenterVertically) { Text(label, modifier = Modifier.weight(1f)); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) } }
        }
    }
}

@Composable
private fun StudyEmptyState(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatReviewDate(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
