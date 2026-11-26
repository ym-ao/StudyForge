package com.studyforge.app.ui

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.room.withTransaction
import com.studyforge.app.data.CollectionEntity
import com.studyforge.app.data.CollectionMembershipRules
import com.studyforge.app.data.QuestionCollectionCrossRef
import com.studyforge.app.data.QuestionEntity
import com.studyforge.app.data.LibraryFilterCriteria
import com.studyforge.app.data.QuestionLibraryFilter
import com.studyforge.app.data.QuestionSelectionRules
import com.studyforge.app.data.SavedFilterPresetEntity
import com.studyforge.app.data.TagEntity
import com.studyforge.app.data.QuestionContentBlockEntity
import com.studyforge.app.data.QuestionPartEntity
import com.studyforge.app.data.QuestionAuthoringRules
import com.studyforge.app.data.MediaImportRules
import com.studyforge.app.data.StudySessionPresetEntity
import com.studyforge.app.data.DerivationRules
import com.studyforge.app.data.TagText
import com.studyforge.app.data.TextFormattingRules
import com.studyforge.app.data.QuestionInterchange
import com.studyforge.app.data.ClozeRules
import com.studyforge.app.data.ManualScheduleRules
import com.studyforge.app.data.QuestionVersionEntity
import com.studyforge.app.data.QuestionVersionRules
import com.studyforge.app.data.ReviewLogEntity
import com.studyforge.app.data.StudySettingsEntity
import com.studyforge.app.data.ExamAttemptEntity
import com.studyforge.app.data.recordGradedReview
import com.studyforge.app.data.StudyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class PendingTableImport(val uri: Uri, val anki: Boolean, val preview: QuestionInterchange.ImportPreview)
private data class TableImportResult(val imported: Int, val updated: Int, val skipped: Int, val issues: List<QuestionInterchange.ImportIssue>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyForgeApp(database: StudyDatabase) {
    val dao = database.dao()
    val appContext = LocalContext.current
    val questions by dao.observeQuestions().collectAsState(initial = emptyList())
    val collections by dao.observeCollections().collectAsState(initial = emptyList())
    val reviewLogs by dao.observeReviewLogs().collectAsState(initial = emptyList())
    val examAttempts by dao.observeExamAttempts().collectAsState(initial = emptyList())
    val links by dao.observeLinks().collectAsState(initial = emptyList())
    val savedFilterPresets by dao.observeSavedFilterPresets().collectAsState(initial = emptyList())
    val tags by dao.observeTags().collectAsState(initial = emptyList())
    val allContentBlocks by dao.observeContentBlocks().collectAsState(initial = emptyList())
    val allQuestionParts by dao.observeQuestionParts().collectAsState(initial = emptyList())
    val studySessionPresets by dao.observeStudySessionPresets().collectAsState(initial = emptyList())
    LaunchedEffect(questions) {
        val discovered = TagText.names(questions)
            .map { name -> TagEntity(id = name.lowercase(), name = name) }
        if (discovered.isNotEmpty()) dao.upsertTags(discovered)
    }
    val storedSettings by dao.observeSettings().collectAsState(initial = null)
    val studySettings = storedSettings ?: StudySettingsEntity.DEFAULT
    LaunchedEffect(Unit) {
        if (dao.getSettings() == null) dao.upsertSettings(StudySettingsEntity.DEFAULT)
    }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var pendingTableImport by remember { mutableStateOf<PendingTableImport?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
    val questionVersions by dao.observeQuestionVersions(editorId ?: "").collectAsState(initial = emptyList())
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editorSessionKey by rememberSaveable { mutableIntStateOf(0) }
    var editorCollectionIds by remember { mutableStateOf(emptyList<String>()) }
    var editorContentBlocks by remember { mutableStateOf(emptyList<QuestionContentBlockEntity>()) }
    var editorQuestionParts by remember { mutableStateOf(emptyList<QuestionPartEntity>()) }

    if (editorOpen) {
        val current = questions.firstOrNull { it.id == editorId }
        key(editorSessionKey) {
        QuestionEditor(
            initial = current,
            initialCollectionIds = editorCollectionIds,
            initialContentBlocks = editorContentBlocks,
            initialQuestionParts = editorQuestionParts,
            versions = questionVersions,
            collections = collections,
            onBack = { editorOpen = false; editorId = null },
            onRestoreVersion = { version -> scope.launch {
                try {
                val currentQuestion = dao.getQuestion(version.questionId)
                if (currentQuestion != null) {
                    val snapshot = QuestionVersionRules.restore(version.snapshotJson, currentQuestion)
                    require(QuestionAuthoringRules.validateBlocks(snapshot.contentBlocks) == null) { "This saved version has invalid content blocks" }
                    require(QuestionAuthoringRules.validateParts(snapshot.parts) == null) { "This saved version has invalid question parts" }
                    database.withTransaction {
                        val currentBlocks = dao.getContentBlocks(currentQuestion.id)
                        val currentParts = dao.getQuestionParts(currentQuestion.id)
                        val currentCollections = dao.getCollectionIds(currentQuestion.id)
                        dao.insertQuestionVersion(QuestionVersionEntity(
                            id = UUID.randomUUID().toString(), questionId = currentQuestion.id,
                            versionNumber = dao.nextQuestionVersion(currentQuestion.id), savedAt = System.currentTimeMillis(),
                            snapshotJson = QuestionVersionRules.createSnapshot(currentQuestion, currentBlocks, currentParts, currentCollections)
                        ))
                        dao.upsertQuestion(snapshot.question)
                        dao.deleteContentBlocks(snapshot.question.id); dao.deleteQuestionParts(snapshot.question.id)
                        if (snapshot.contentBlocks.isNotEmpty()) dao.upsertContentBlocks(snapshot.contentBlocks)
                        if (snapshot.parts.isNotEmpty()) dao.upsertQuestionParts(snapshot.parts)
                        dao.clearQuestionCollections(snapshot.question.id)
                        val validIds = dao.getAllCollections().map { it.id }.toSet()
                        val safeIds = snapshot.collectionIds.filter { it in validIds }
                        if (safeIds.isNotEmpty()) dao.insertLinks(safeIds.map { QuestionCollectionCrossRef(snapshot.question.id, it) })
                    }
                    editorCollectionIds = snapshot.collectionIds
                    editorContentBlocks = snapshot.contentBlocks
                    editorQuestionParts = snapshot.parts
                    editorSessionKey += 1
                    snackbar.showSnackbar("Version ${version.versionNumber} restored; review history and scheduling preserved")
                }
                } catch (e: Exception) {
                    snackbar.showSnackbar("Version restore failed: ${e.message ?: "invalid saved version"}")
                }
            } },
            onSave = { question, selectedCollections, contentBlocks, questionParts ->
                scope.launch {
                    try {
                        database.withTransaction {
                            val validCollectionIds = dao.getAllCollections().map { it.id }.toSet()
                            val safeSelectedCollections = selectedCollections.distinct().filter { it in validCollectionIds }
                            val old = dao.getQuestion(question.id)
                            if (old != null) {
                                val oldBlocks = dao.getContentBlocks(question.id)
                                val oldParts = dao.getQuestionParts(question.id)
                                val oldCollectionIds = dao.getCollectionIds(question.id)
                                val changed = old.copy(updatedAt = question.updatedAt) != question || oldBlocks != contentBlocks || oldParts != questionParts || oldCollectionIds.toSet() != safeSelectedCollections.toSet()
                                if (changed) {
                                    dao.insertQuestionVersion(QuestionVersionEntity(
                                        id = UUID.randomUUID().toString(), questionId = question.id,
                                        versionNumber = dao.nextQuestionVersion(question.id), savedAt = System.currentTimeMillis(),
                                        snapshotJson = QuestionVersionRules.createSnapshot(old, oldBlocks, oldParts, oldCollectionIds)
                                    ))
                                }
                            }
                            dao.upsertQuestion(question)
                            dao.deleteContentBlocks(question.id)
                            dao.deleteQuestionParts(question.id)
                            val normalizedBlocks = QuestionAuthoringRules.normalizeBlocks(question.id, contentBlocks)
                            val normalizedParts = QuestionAuthoringRules.normalizeParts(question.id, questionParts)
                            if (normalizedBlocks.isNotEmpty()) dao.upsertContentBlocks(normalizedBlocks)
                            if (normalizedParts.isNotEmpty()) dao.upsertQuestionParts(normalizedParts)
                            dao.clearQuestionCollections(question.id)
                            if (safeSelectedCollections.isNotEmpty()) {
                                dao.insertLinks(safeSelectedCollections.map { QuestionCollectionCrossRef(question.id, it) })
                            }
                        }
                        editorOpen = false
                        editorId = null
                        snackbar.showSnackbar("Question saved to your library")
                    } catch (error: Exception) {
                        snackbar.showSnackbar("Could not save question: ${error.message ?: "database error"}")
                    }
                }
            }
        )
        }
        return
    }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Library", Icons.Default.Book),
        NavItem("Collections", Icons.Default.Folder),
        NavItem("Study", Icons.Default.School),
        NavItem("Backup", Icons.Default.ImportExport),
        NavItem("Progress", Icons.Default.Insights),
        NavItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("StudyForge", fontWeight = FontWeight.Bold)
                        Text("Build your own learning system", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { editorCollectionIds = emptyList(); editorContentBlocks = emptyList(); editorQuestionParts = emptyList(); editorId = null; editorSessionKey += 1; editorOpen = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add question")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    questionCount = questions.size,
                    collectionCount = collections.size,
                    reviewCount = questions.sumOf { it.reviewCount },
                    dueCount = questions.count { it.dueAt == null || it.dueAt <= System.currentTimeMillis() },
                    onAdd = { editorCollectionIds = emptyList(); editorContentBlocks = emptyList(); editorQuestionParts = emptyList(); editorId = null; editorSessionKey += 1; editorOpen = true },
                    onLibrary = { selectedTab = 1 },
                    onStudy = { selectedTab = 3 }
                )
                1 -> LibraryScreen(
                    questions = questions,
                    collections = collections,
                    links = links,
                    savedFilterPresets = savedFilterPresets,
                    tags = tags,
                    onAddTag = { name -> scope.launch { dao.upsertTag(TagEntity(name.lowercase(), name)); snackbar.showSnackbar("Tag added") } },
                    onRenameTag = { old, new -> scope.launch {
                        database.withTransaction {
                            dao.getAllQuestions().forEach { q ->
                                val updated = TagText.rename(q.tagsCsv, old, new)
                                if (updated != q.tagsCsv) dao.upsertQuestion(q.copy(tagsCsv = updated, updatedAt = System.currentTimeMillis()))
                            }
                            dao.deleteTag(old.lowercase()); dao.upsertTag(TagEntity(new.lowercase(), new))
                        }
                        snackbar.showSnackbar("Tag renamed")
                    } },
                    onDeleteTag = { tag -> scope.launch {
                        database.withTransaction {
                            dao.getAllQuestions().forEach { q ->
                                val updated = TagText.remove(q.tagsCsv, tag.name)
                                if (updated != q.tagsCsv) dao.upsertQuestion(q.copy(tagsCsv = updated, updatedAt = System.currentTimeMillis()))
                            }
                            dao.deleteTag(tag.id)
                        }
                        snackbar.showSnackbar("Tag deleted and removed from questions")
                    } },
                    onSavePreset = { name, criteria ->
                        scope.launch {
                            val existing = savedFilterPresets.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
                            val preset = SavedFilterPresetEntity.fromCriteria(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(), criteria = criteria,
                                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            dao.upsertSavedFilterPreset(preset)
                            snackbar.showSnackbar(if (existing == null) "Filter preset saved" else "Filter preset updated")
                        }
                    },
                    onDeletePreset = { preset -> scope.launch { dao.deleteSavedFilterPreset(preset.id); snackbar.showSnackbar("Preset deleted") } },
                    onManualSchedule = { question, action -> scope.launch {
                        val updated = when (action) {
                            "now" -> ManualScheduleRules.setDue(question, System.currentTimeMillis())
                            "tomorrow" -> ManualScheduleRules.setDue(question, System.currentTimeMillis() + 86_400_000L)
                            "week" -> ManualScheduleRules.setDue(question, System.currentTimeMillis() + 7L * 86_400_000L)
                            else -> ManualScheduleRules.reset(question)
                        }
                        dao.upsertQuestion(updated)
                        snackbar.showSnackbar(if (action == "reset") "Scheduling reset; review history retained" else "Question rescheduled")
                    } },
                    onBulkUpdate = { selected, status, difficulty -> scope.launch {
                        database.withTransaction {
                            selected.forEach { selectedQuestion ->
                                val current = dao.getQuestion(selectedQuestion.id) ?: selectedQuestion
                                val updated = current.copy(status = status ?: current.status, difficulty = difficulty ?: current.difficulty, updatedAt = System.currentTimeMillis())
                                if (current.copy(updatedAt = updated.updatedAt) != updated) {
                                    dao.insertQuestionVersion(QuestionVersionEntity(
                                        id = UUID.randomUUID().toString(), questionId = current.id,
                                        versionNumber = dao.nextQuestionVersion(current.id), savedAt = System.currentTimeMillis(),
                                        snapshotJson = QuestionVersionRules.createSnapshot(current, dao.getContentBlocks(current.id), dao.getQuestionParts(current.id), dao.getCollectionIds(current.id))
                                    ))
                                }
                                dao.upsertQuestion(updated)
                            }
                        }
                        snackbar.showSnackbar("Updated ${selected.size} question(s)")
                    } },
                    onBulkTag = { selected, tagName, add -> scope.launch {
                        val normalized = tagName.trim()
                        if (normalized.isNotEmpty() && !normalized.contains(',')) {
                            var changedCount = 0
                            database.withTransaction {
                                if (add) dao.upsertTag(TagEntity(normalized.lowercase(), normalized))
                                selected.forEach { selectedQuestion ->
                                    val current = dao.getQuestion(selectedQuestion.id) ?: return@forEach
                                    val updatedTags = if (add) TagText.add(current.tagsCsv, normalized) else TagText.remove(current.tagsCsv, normalized)
                                    if (updatedTags != current.tagsCsv) {
                                        dao.insertQuestionVersion(QuestionVersionEntity(
                                            id = UUID.randomUUID().toString(), questionId = current.id,
                                            versionNumber = dao.nextQuestionVersion(current.id), savedAt = System.currentTimeMillis(),
                                            snapshotJson = QuestionVersionRules.createSnapshot(current, dao.getContentBlocks(current.id), dao.getQuestionParts(current.id), dao.getCollectionIds(current.id))
                                        ))
                                        dao.upsertQuestion(current.copy(tagsCsv = updatedTags, updatedAt = System.currentTimeMillis()))
                                        changedCount++
                                    }
                                }
                            }
                            snackbar.showSnackbar(if (add) "Tag added to $changedCount question(s)" else "Tag removed from $changedCount question(s)")
                        }
                    } },
                    onBulkCollection = { selected, collectionId, add -> scope.launch {
                        var changedCount = 0
                        try {
                            database.withTransaction {
                                val validCollectionIds = dao.getAllCollections().map { it.id }.toSet()
                                if (collectionId in validCollectionIds) {
                                    selected.forEach { selectedQuestion ->
                                        val current = dao.getQuestion(selectedQuestion.id) ?: return@forEach
                                        val oldIds = dao.getCollectionIds(current.id)
                                        // A legacy/stale link must not be re-inserted after the links are replaced.
                                        val safeOldIds = oldIds.filter { it in validCollectionIds }
                                        val newIds = CollectionMembershipRules.update(safeOldIds, collectionId, add)
                                        if (newIds.toSet() != oldIds.toSet()) {
                                            dao.insertQuestionVersion(QuestionVersionEntity(
                                                id = UUID.randomUUID().toString(), questionId = current.id,
                                                versionNumber = dao.nextQuestionVersion(current.id), savedAt = System.currentTimeMillis(),
                                                snapshotJson = QuestionVersionRules.createSnapshot(current, dao.getContentBlocks(current.id), dao.getQuestionParts(current.id), oldIds)
                                            ))
                                            dao.clearQuestionCollections(current.id)
                                            if (newIds.isNotEmpty()) dao.insertLinks(newIds.map { QuestionCollectionCrossRef(current.id, it) })
                                            dao.upsertQuestion(current.copy(updatedAt = System.currentTimeMillis()))
                                            changedCount++
                                        }
                                    }
                                }
                            }
                            snackbar.showSnackbar(if (add) "Added $changedCount question(s) to collection" else "Removed $changedCount question(s) from collection")
                        } catch (error: Exception) {
                            snackbar.showSnackbar("Collection update failed: ${error.message ?: "database error"}")
                        }
                    } },
                    onEdit = { question -> scope.launch {
                        editorCollectionIds = dao.getCollectionIds(question.id)
                        editorContentBlocks = dao.getContentBlocks(question.id)
                        editorQuestionParts = dao.getQuestionParts(question.id)
                        editorId = question.id
                        editorSessionKey += 1
                        editorOpen = true
                    } },
                    onDelete = { question -> scope.launch { dao.deleteQuestion(question.id); snackbar.showSnackbar("Question deleted") } }
                )
                2 -> CollectionsScreen(
                    collections = collections,
                    onCreate = { name, description ->
                        scope.launch {
                            dao.upsertCollection(CollectionEntity(UUID.randomUUID().toString(), name.trim(), description.trim()))
                            snackbar.showSnackbar("Collection created")
                        }
                    },
                    onDelete = { collection -> scope.launch { database.withTransaction { dao.clearStudySessionCollection(collection.id); dao.deleteCollection(collection.id) } } }
                )
                3 -> StudyScreen(
                    questions = questions,
                    reviewLogs = reviewLogs,
                    contentBlocks = allContentBlocks,
                    questionParts = allQuestionParts,
                    sessionPresets = studySessionPresets,
                    collections = collections,
                    collectionLinks = links,
                    settings = studySettings,
                    onSaveSessionPreset = { preset -> scope.launch { dao.upsertStudySessionPreset(preset); snackbar.showSnackbar("Study session saved") } },
                    onDeleteSessionPreset = { preset -> scope.launch { dao.deleteStudySessionPreset(preset.id); snackbar.showSnackbar("Study session deleted") } },
                    onSaveExamAttempt = { attempt -> scope.launch { dao.upsertExamAttempt(attempt); snackbar.showSnackbar("Exam attempt saved") } },
                    onReview = { question, rating, wasCorrect, mistakeCategory, reviewMode ->
                        scope.launch { database.recordGradedReview(question.id, rating, wasCorrect, mistakeCategory.orEmpty(), reviewMode, settings = studySettings) }
                    }
                )
                4 -> BackupScreen(
                    onExport = { uri -> scope.launch { exportBackup(appContext, database, uri); snackbar.showSnackbar("Backup exported") } },
                    onImport = { uri -> scope.launch {
                        try { importBackup(appContext, database, uri); snackbar.showSnackbar("Backup imported") }
                        catch (e: Exception) { snackbar.showSnackbar("Import failed: ${e.message ?: "invalid backup"}") }
                    } },
                    onExportCsv = { uri -> scope.launch { exportQuestionTable(appContext, dao.getAllQuestions(), uri, anki = false); snackbar.showSnackbar("CSV exported") } },
                    onExportAnki = { uri -> scope.launch { exportQuestionTable(appContext, dao.getAllQuestions(), uri, anki = true); snackbar.showSnackbar("Anki TSV exported") } },
                    onImportCsv = { uri -> scope.launch {
                        try {
                            val preview = readQuestionTablePreview(appContext, dao.getAllQuestions(), uri, anki = false)
                            pendingTableImport = PendingTableImport(uri, false, preview)
                        } catch (e: Exception) { snackbar.showSnackbar("CSV preview failed: ${e.message ?: "invalid file"}") }
                    } },
                    onImportAnki = { uri -> scope.launch {
                        try {
                            val preview = readQuestionTablePreview(appContext, dao.getAllQuestions(), uri, anki = true)
                            pendingTableImport = PendingTableImport(uri, true, preview)
                        } catch (e: Exception) { snackbar.showSnackbar("Anki preview failed: ${e.message ?: "invalid file"}") }
                    } }
                )
                5 -> ProgressScreen(questions = questions, reviewLogs = reviewLogs, examAttempts = examAttempts)
                6 -> SettingsScreen(settings = studySettings, onSave = { updated ->
                    scope.launch { dao.upsertSettings(updated); snackbar.showSnackbar("Study settings saved") }
                })
            }
        }
    }
    pendingTableImport?.let { pending ->
        val preview = pending.preview
        AlertDialog(
            onDismissRequest = { pendingTableImport = null },
            title = { Text(if (pending.anki) "Preview Anki import" else "Preview CSV import") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rows in file: ${preview.totalRows}")
                    Text("New questions: ${preview.newCount}")
                    Text("Updates by matching ID: ${preview.updateCount}")
                    Text("Duplicates skipped: ${preview.duplicateCount}")
                    Text("Invalid rows: ${preview.issues.size}")
                    Text("Only the valid, non-duplicate rows below will be imported. Existing review schedules and history are preserved for ID-matched updates.", style = MaterialTheme.typography.bodySmall)
                    preview.issues.take(12).forEach { issue ->
                        Text("${if (issue.rowNumber == 0) "File" else "Row ${issue.rowNumber}"}: ${issue.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (preview.issues.size > 12) Text("And ${preview.issues.size - 12} more row issue(s).", style = MaterialTheme.typography.bodySmall)
                    preview.rows.take(5).forEachIndexed { index, row ->
                        HorizontalDivider()
                        Text("${index + 1}. ${row.prompt.take(180)}", fontWeight = FontWeight.SemiBold)
                        Text(row.answer.take(180), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (preview.rows.size > 5) Text("And ${preview.rows.size - 5} more valid question(s).", style = MaterialTheme.typography.bodySmall)
                    if (!preview.canImport) Text("There are no valid rows to import.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(enabled = preview.canImport, onClick = {
                    pendingTableImport = null
                    scope.launch {
                        try {
                            val result = importQuestionTable(database, preview)
                            val firstIssue = result.issues.firstOrNull()?.let { " First issue: ${if (it.rowNumber == 0) "File" else "row ${it.rowNumber}"} — ${it.message}" }.orEmpty()
                            snackbar.showSnackbar("Imported ${result.imported}; updated ${result.updated}; skipped ${result.skipped}.$firstIssue")
                        } catch (e: Exception) { snackbar.showSnackbar("Import failed: ${e.message ?: "invalid data"}") }
                    }
                }) { Text("Import valid rows") }
            },
            dismissButton = { TextButton(onClick = { pendingTableImport = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HomeScreen(
    questionCount: Int,
    collectionCount: Int,
    reviewCount: Int,
    dueCount: Int,
    onAdd: () -> Unit,
    onLibrary: () -> Unit,
    onStudy: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your knowledge, your rules.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Create questions, organize them into collections, and study at your own pace.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Button(onClick = onAdd) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Create your first question") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Questions", questionCount.toString(), Modifier.weight(1f))
            StatCard("Due", dueCount.toString(), Modifier.weight(1f))
            StatCard("Reviews", reviewCount.toString(), Modifier.weight(1f))
        }
        Text("${collectionCount} collections • Offline-first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Quick actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ActionCard("Question library", "Search, edit, and organize your questions.", Icons.Default.Book, onLibrary)
        ActionCard("Start studying", "Review flashcards and answer MCQs.", Icons.Default.School, onStudy)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Version 2.1.24 • StudyForge learning system", fontWeight = FontWeight.SemiBold)
                Text("StudyForge now schedules reviews, tracks mistakes, and summarizes weak topics. Your library stays on this device. Export a JSON backup regularly so you can move your work to another device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    questions: List<QuestionEntity>,
    collections: List<CollectionEntity>,
    links: List<QuestionCollectionCrossRef>,
    savedFilterPresets: List<SavedFilterPresetEntity>,
    tags: List<TagEntity>,
    onAddTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (TagEntity) -> Unit,
    onSavePreset: (String, LibraryFilterCriteria) -> Unit,
    onDeletePreset: (SavedFilterPresetEntity) -> Unit,
    onManualSchedule: (QuestionEntity, String) -> Unit,
    onBulkUpdate: (List<QuestionEntity>, String?, String?) -> Unit,
    onBulkTag: (List<QuestionEntity>, String, Boolean) -> Unit,
    onBulkCollection: (List<QuestionEntity>, String, Boolean) -> Unit,
    onEdit: (QuestionEntity) -> Unit,
    onDelete: (QuestionEntity) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var appliedSearch by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(search) {
        delay(180)
        appliedSearch = search
    }
    var typeFilter by rememberSaveable { mutableStateOf("All") }
    var statusFilter by rememberSaveable { mutableStateOf("All") }
    var subjectFilter by rememberSaveable { mutableStateOf("") }
    var chapterFilter by rememberSaveable { mutableStateOf("") }
    var topicFilter by rememberSaveable { mutableStateOf("") }
    var tagFilter by rememberSaveable { mutableStateOf("") }
    var difficultyFilter by rememberSaveable { mutableStateOf("All") }
    var examFilter by rememberSaveable { mutableStateOf("") }
    var performanceFilter by rememberSaveable { mutableStateOf("All") }
    var sourceTitleFilter by rememberSaveable { mutableStateOf("") }
    var sourceDetailFilter by rememberSaveable { mutableStateOf("") }
    var createdAfterFilter by rememberSaveable { mutableStateOf("") }
    var createdBeforeFilter by rememberSaveable { mutableStateOf("") }
    var updatedAfterFilter by rememberSaveable { mutableStateOf("") }
    var updatedBeforeFilter by rememberSaveable { mutableStateOf("") }
    var selectedCollectionId by rememberSaveable { mutableStateOf("") }
    var collectionMenu by remember { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showSavePreset by remember { mutableStateOf(false) }
    var showManagePresets by remember { mutableStateOf(false) }
    var presetPendingDelete by remember { mutableStateOf<SavedFilterPresetEntity?>(null) }
    var presetName by remember { mutableStateOf("") }
    var showTagManager by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var editedTagName by remember { mutableStateOf("") }
    var deletingTag by remember { mutableStateOf<TagEntity?>(null) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedQuestionIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkTagDialog by remember { mutableStateOf(false) }
    var bulkTagName by remember { mutableStateOf("") }
    var bulkTagAdd by remember { mutableStateOf(true) }
    var showBulkCollectionDialog by remember { mutableStateOf(false) }
    var bulkCollectionAdd by remember { mutableStateOf(true) }
    var bulkCollectionId by remember { mutableStateOf("") }
    var bulkCollectionMenu by remember { mutableStateOf(false) }

    val collectionId = selectedCollectionId.takeIf { it.isNotBlank() }
    val selectedCollectionLabel = collections.firstOrNull { it.id == collectionId }?.name ?: "All collections"
    val collectionQuestionIds = remember(links, collectionId) {
        if (collectionId == null) emptySet() else links.filter { it.collectionId == collectionId }.map { it.questionId }.toSet()
    }
    val criteria = LibraryFilterCriteria(
        query = appliedSearch, type = typeFilter, status = statusFilter, subject = subjectFilter,
        chapter = chapterFilter, topic = topicFilter, tag = tagFilter, collectionId = collectionId,
        difficulty = difficultyFilter, exam = examFilter, performance = performanceFilter,
        sourceTitle = sourceTitleFilter, sourceDetail = sourceDetailFilter,
        createdAfter = createdAfterFilter, createdBefore = createdBeforeFilter,
        updatedAfter = updatedAfterFilter, updatedBefore = updatedBeforeFilter
    )
    var filtered by remember { mutableStateOf(emptyList<QuestionEntity>()) }
    var filtering by remember { mutableStateOf(true) }
    LaunchedEffect(questions, criteria, collectionQuestionIds) {
        filtering = true
        val result = withContext(Dispatchers.Default) {
            questions.filter { q -> QuestionLibraryFilter.matches(q, criteria, collectionQuestionIds) }
        }
        filtered = result
        filtering = false
    }
    var liveFilterMatchCounts by remember { mutableStateOf(emptyMap<String, Int>()) }
    LaunchedEffect(questions, links, savedFilterPresets) {
        liveFilterMatchCounts = withContext(Dispatchers.Default) {
            savedFilterPresets.associate { preset ->
                val savedCriteria = preset.toCriteria()
                val presetCollectionIds = savedCriteria.collectionId?.let { id ->
                    links.asSequence().filter { it.collectionId == id }.map { it.questionId }.toSet()
                } ?: emptySet()
                preset.id to QuestionLibraryFilter.countMatches(questions, savedCriteria, presetCollectionIds)
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search questions") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showTagManager = true }) { Text("Manage tags (${tags.size})") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text("Live smart filters (${savedFilterPresets.size})", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { presetName = ""; showSavePreset = true }) { Text("Save smart filter") }
            TextButton(onClick = { showManagePresets = true }, enabled = savedFilterPresets.isNotEmpty()) { Text("Manage") }
        }
        if (savedFilterPresets.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                savedFilterPresets.forEach { preset ->
                    FilterChip(selected = false, onClick = {
                        val saved = preset.toCriteria()
                        search = saved.query; typeFilter = saved.type; statusFilter = saved.status
                        subjectFilter = saved.subject; chapterFilter = saved.chapter; topicFilter = saved.topic
                        tagFilter = saved.tag; selectedCollectionId = saved.collectionId.orEmpty()
                        difficultyFilter = saved.difficulty; examFilter = saved.exam
                        performanceFilter = saved.performance
                        sourceTitleFilter = saved.sourceTitle; sourceDetailFilter = saved.sourceDetail
                        createdAfterFilter = saved.createdAfter; createdBeforeFilter = saved.createdBefore
                        updatedAfterFilter = saved.updatedAfter; updatedBeforeFilter = saved.updatedBefore
                        showFilters = true
                    }, label = { Text("${preset.name} · ${liveFilterMatchCounts[preset.id] ?: 0}") })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            listOf("All", "Flashcard", "MCQ", "True/False", "Numerical", "Cloze").forEach { type -> FilterChip(selected = typeFilter == type, onClick = { typeFilter = type }, label = { Text(type) }) }
            FilterChip(selected = showFilters, onClick = { showFilters = !showFilters }, label = { Text(if (showFilters) "Hide filters" else "More filters") })
        }
        if (showFilters) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Collection", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { collectionMenu = true }) { Text(selectedCollectionLabel) }
                        DropdownMenu(expanded = collectionMenu, onDismissRequest = { collectionMenu = false }) {
                            DropdownMenuItem(text = { Text("All collections") }, onClick = { selectedCollectionId = ""; collectionMenu = false })
                            collections.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedCollectionId = c.id; collectionMenu = false }) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("All", "Draft", "Unverified", "Verified", "Incorrect", "Archived").forEach { status -> FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status) }) }
                }
                OutlinedTextField(subjectFilter, { subjectFilter = it }, label = { Text("Subject filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(chapterFilter, { chapterFilter = it }, label = { Text("Chapter filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(topicFilter, { topicFilter = it }, label = { Text("Topic filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(tagFilter, { tagFilter = it }, label = { Text("Tag filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(examFilter, { examFilter = it }, label = { Text("Exam / syllabus filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(sourceTitleFilter, { sourceTitleFilter = it }, label = { Text("Source title filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(sourceDetailFilter, { sourceDetailFilter = it }, label = { Text("Source reference filter (chapter / page / URL)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Created date range (YYYY-MM-DD)", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(createdAfterFilter, { createdAfterFilter = it.take(10) }, label = { Text("Created from") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(createdBeforeFilter, { createdBeforeFilter = it.take(10) }, label = { Text("Created to") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("Modified date range (YYYY-MM-DD)", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(updatedAfterFilter, { updatedAfterFilter = it.take(10) }, label = { Text("Modified from") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(updatedBeforeFilter, { updatedBeforeFilter = it.take(10) }, label = { Text("Modified to") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("Difficulty")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("All", "Easy", "Medium", "Hard", "Unrated").forEach { value -> FilterChip(selected = difficultyFilter == value, onClick = { difficultyFilter = value }, label = { Text(value) }) }
                }
                Text("Performance")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("All", "Unattempted", "Weak", "Strong").forEach { value -> FilterChip(selected = performanceFilter == value, onClick = { performanceFilter = value }, label = { Text(value) }) }
                }
                TextButton(onClick = { subjectFilter = ""; chapterFilter = ""; topicFilter = ""; tagFilter = ""; examFilter = ""; sourceTitleFilter = ""; sourceDetailFilter = ""; createdAfterFilter = ""; createdBeforeFilter = ""; updatedAfterFilter = ""; updatedBeforeFilter = ""; difficultyFilter = "All"; performanceFilter = "All"; statusFilter = "All"; selectedCollectionId = ""; typeFilter = "All"; search = "" }) { Text("Clear all filters") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${filtered.size} of ${questions.size} questions${if (filtering) " · filtering" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            TextButton(onClick = { selectionMode = !selectionMode; selectedQuestionIds = emptySet() }) { Text(if (selectionMode) "Done selecting" else "Select multiple") }
        }
        if (selectionMode) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${selectedQuestionIds.size} selected", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { selectedQuestionIds = if (selectedQuestionIds.size == filtered.size) emptySet() else filtered.map { it.id }.toSet() }) { Text(if (selectedQuestionIds.size == filtered.size) "Clear" else "Select visible") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Draft", "Verified", "Incorrect", "Archived").forEach { value ->
                    FilterChip(selected = false, onClick = { val selected = QuestionSelectionRules.resolve(questions, selectedQuestionIds); if (selected.isNotEmpty()) { onBulkUpdate(selected, value, null); selectedQuestionIds = emptySet() } }, label = { Text("Set $value") }, enabled = selectedQuestionIds.isNotEmpty())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Easy", "Medium", "Hard", "Unrated").forEach { value ->
                    FilterChip(selected = false, onClick = { val selected = QuestionSelectionRules.resolve(questions, selectedQuestionIds); if (selected.isNotEmpty()) { onBulkUpdate(selected, null, value); selectedQuestionIds = emptySet() } }, label = { Text(value) }, enabled = selectedQuestionIds.isNotEmpty())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { bulkTagAdd = true; bulkTagName = ""; showBulkTagDialog = true }, enabled = selectedQuestionIds.isNotEmpty()) { Text("Add tag") }
                OutlinedButton(onClick = { bulkTagAdd = false; bulkTagName = ""; showBulkTagDialog = true }, enabled = selectedQuestionIds.isNotEmpty()) { Text("Remove tag") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { bulkCollectionAdd = true; bulkCollectionId = ""; showBulkCollectionDialog = true }, enabled = selectedQuestionIds.isNotEmpty() && collections.isNotEmpty()) { Text("Add to collection") }
                OutlinedButton(onClick = { bulkCollectionAdd = false; bulkCollectionId = ""; showBulkCollectionDialog = true }, enabled = selectedQuestionIds.isNotEmpty() && collections.isNotEmpty()) { Text("Remove from collection") }
            }
        }
        if (filtered.isEmpty()) {
            EmptyState("No questions found", if (questions.isEmpty()) "Tap + to create your first question." else "Try a different search or filter.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)) {
                items(filtered, key = { it.id }) { q ->
                    var scheduleMenu by remember(q.id) { mutableStateOf(false) }
                    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable { if (selectionMode) selectedQuestionIds = if (q.id in selectedQuestionIds) selectedQuestionIds - q.id else selectedQuestionIds + q.id else onEdit(q) }) {
                        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectionMode) Checkbox(checked = q.id in selectedQuestionIds, onCheckedChange = { checked -> selectedQuestionIds = if (checked) selectedQuestionIds + q.id else selectedQuestionIds - q.id })
                                Text(q.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text(q.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!selectionMode) {
                                    IconButton(onClick = { onEdit(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, "Edit") }
                                    IconButton(onClick = { onDelete(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, "Delete") }
                                }
                            }
                            RichContentText(q.prompt.take(360).let { preview -> if (q.prompt.length > 360) "$preview…" else preview }, style = MaterialTheme.typography.titleMedium)
                            val meta = listOf(q.subject, q.chapter, q.topic, q.exam).filter { it.isNotBlank() }.joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Difficulty: ${q.difficulty}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                                if (!selectionMode) Box {
                                    TextButton(onClick = { scheduleMenu = true }) { Text("Schedule") }
                                    DropdownMenu(expanded = scheduleMenu, onDismissRequest = { scheduleMenu = false }) {
                                        listOf("Due now" to "now", "Tomorrow" to "tomorrow", "In 7 days" to "week", "Reset schedule" to "reset").forEach { (label, action) ->
                                            DropdownMenuItem(text = { Text(label) }, onClick = { scheduleMenu = false; onManualSchedule(q, action) })
                                        }
                                    }
                                }
                            }
                            if (q.tagsCsv.isNotBlank()) Text("Tags: ${q.tagsCsv}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            if (q.latex.isNotBlank()) Text("Math formula available · open question to view", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
    if (showBulkTagDialog) {
        AlertDialog(
            onDismissRequest = { showBulkTagDialog = false },
            title = { Text(if (bulkTagAdd) "Add tag to selected" else "Remove tag from selected") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This updates ${selectedQuestionIds.size} selected question(s). A content version is saved when a tag list changes.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = bulkTagName,
                        onValueChange = { bulkTagName = it.take(60) },
                        label = { Text("Tag name") },
                        supportingText = { if (bulkTagName.contains(',')) Text("Use one tag at a time; commas are not allowed.") },
                        isError = bulkTagName.contains(','),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val selected = QuestionSelectionRules.resolve(questions, selectedQuestionIds)
                    val tag = bulkTagName.trim()
                    if (selected.isNotEmpty() && tag.isNotEmpty()) {
                        onBulkTag(selected, tag, bulkTagAdd)
                        selectedQuestionIds = emptySet()
                        showBulkTagDialog = false
                    }
                }, enabled = bulkTagName.isNotBlank() && !bulkTagName.contains(',') && selectedQuestionIds.isNotEmpty()) { Text(if (bulkTagAdd) "Add" else "Remove") }
            },
            dismissButton = { TextButton(onClick = { showBulkTagDialog = false }) { Text("Cancel") } }
        )
    }
    if (showBulkCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showBulkCollectionDialog = false },
            title = { Text(if (bulkCollectionAdd) "Add selected to collection" else "Remove selected from collection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose one collection for ${selectedQuestionIds.size} selected question(s). Changed questions get a version-history snapshot.", style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedButton(onClick = { bulkCollectionMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(collections.firstOrNull { it.id == bulkCollectionId }?.name ?: "Choose collection")
                        }
                        DropdownMenu(expanded = bulkCollectionMenu, onDismissRequest = { bulkCollectionMenu = false }) {
                            collections.forEach { collection ->
                                DropdownMenuItem(text = { Text(collection.name) }, onClick = { bulkCollectionId = collection.id; bulkCollectionMenu = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val selected = QuestionSelectionRules.resolve(questions, selectedQuestionIds)
                    if (selected.isNotEmpty() && bulkCollectionId.isNotBlank()) {
                        onBulkCollection(selected, bulkCollectionId, bulkCollectionAdd)
                        selectedQuestionIds = emptySet()
                        showBulkCollectionDialog = false
                    }
                }, enabled = bulkCollectionId.isNotBlank() && selectedQuestionIds.isNotEmpty()) { Text(if (bulkCollectionAdd) "Add" else "Remove") }
            },
            dismissButton = { TextButton(onClick = { showBulkCollectionDialog = false }) { Text("Cancel") } }
        )
    }
    if (showTagManager) {
        AlertDialog(
            onDismissRequest = { showTagManager = false },
            title = { Text("Tag manager") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text("Create, rename or delete tags. Renaming and deleting also updates every question that uses the tag.", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = newTagName, onValueChange = { newTagName = it.filterNot { ch -> ch == ',' }.take(40) }, label = { Text("New tag") }, singleLine = true, modifier = Modifier.weight(1f))
                        TextButton(onClick = { val clean = newTagName.trim(); if (clean.isNotEmpty() && tags.none { it.name.equals(clean, true) }) { onAddTag(clean); newTagName = "" } }, enabled = newTagName.isNotBlank()) { Text("Add") }
                    }
                    if (tags.isEmpty()) Text("No tags yet. Add one here or enter tags in the question editor.")
                    tags.forEach { tag ->
                        val usage = questions.count { q -> TagText.parse(q.tagsCsv).any { it.equals(tag.name, true) } }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(tag.name); Text("Used by $usage questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            TextButton(onClick = { editingTag = tag; editedTagName = tag.name }) { Text("Rename") }
                            TextButton(onClick = { deletingTag = tag }) { Text("Delete") }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTagManager = false }) { Text("Done") } }
        )
    }
    editingTag?.let { tag ->
        AlertDialog(onDismissRequest = { editingTag = null }, title = { Text("Rename tag") },
            text = { OutlinedTextField(value = editedTagName, onValueChange = { editedTagName = it.filterNot { ch -> ch == ',' }.take(40) }, label = { Text("Tag name") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { val clean = editedTagName.trim(); if (clean.isNotEmpty() && tags.none { it.name.equals(clean, true) && it.id != tag.id }) { onRenameTag(tag.name, clean); editingTag = null } }, enabled = editedTagName.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editingTag = null }) { Text("Cancel") } })
    }
    deletingTag?.let { tag ->
        AlertDialog(onDismissRequest = { deletingTag = null }, title = { Text("Delete tag?") },
            text = { Text("${tag.name} will be removed from all questions. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { onDeleteTag(tag); deletingTag = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deletingTag = null }) { Text("Cancel") } })
    }
    if (showSavePreset) {
        AlertDialog(
            onDismissRequest = { showSavePreset = false },
            title = { Text("Save filter preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Save the current search and filter combination for reuse. Saving with an existing name updates that preset.")
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it.take(50) },
                        label = { Text("Preset name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleanName = presetName.trim()
                    if (cleanName.isNotEmpty()) {
                        onSavePreset(cleanName, criteria)
                        showSavePreset = false
                    }
                }, enabled = presetName.isNotBlank()) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSavePreset = false }) { Text("Cancel") } }
        )
    }
    if (showManagePresets) {
        AlertDialog(
            onDismissRequest = { showManagePresets = false },
            title = { Text("Manage saved filters") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())
                ) {
                    savedFilterPresets.forEach { preset ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(preset.name, modifier = Modifier.weight(1f))
                            TextButton(onClick = { presetPendingDelete = preset }) { Text("Delete") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showManagePresets = false }) { Text("Done") } }
        )
    }
    presetPendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetPendingDelete = null },
            title = { Text("Delete saved filter?") },
            text = { Text("\"${preset.name}\" will be removed. Your questions and collections will not be affected.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePreset(preset)
                    presetPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { presetPendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SelectionFormattingField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 2,
    supportingText: String? = null
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) fieldValue = TextFieldValue(value)
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Bold" to "BOLD", "Italic" to "ITALIC", "Underline" to "UNDERLINE", "Highlight" to "HIGHLIGHT", "Heading" to "HEADING", "Bullet" to "BULLET", "Code" to "CODE").forEach { (title, operation) ->
                FilterChip(selected = false, onClick = {
                    val result = TextFormattingRules.apply(fieldValue.text, fieldValue.selection.start, fieldValue.selection.end, operation)
                    fieldValue = TextFieldValue(result.text, selection = TextRange(result.selectionStart, result.selectionEnd))
                    onValueChange(result.text)
                }, label = { Text(title) })
            }
        }
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { fieldValue = it; onValueChange(it.text) },
            label = { Text(label) }, modifier = Modifier.fillMaxWidth(), minLines = minLines,
            supportingText = supportingText?.let { message -> { Text(message) } }
        )
    }
}

@Composable
private fun CollectionsScreen(
    collections: List<CollectionEntity>,
    onCreate: (String, String) -> Unit,
    onDelete: (CollectionEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Collections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Group questions by exam, subject, or any system you choose.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showDialog = true }) { Icon(Icons.Default.Add, null); Text("New") }
        }
        if (collections.isEmpty()) EmptyState("No collections yet", "Create a collection such as MDCAT Physics or Biology Chapter 1.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(collections, key = { it.id }) { collection ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(collection.name, fontWeight = FontWeight.SemiBold)
                            if (collection.description.isNotBlank()) Text(collection.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(collection) }) { Icon(Icons.Default.Delete, "Delete collection") }
                    }
                }
            }
        }
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Create collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") })
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onCreate(name, description); name = ""; description = ""; showDialog = false } }) { Text("Create") } },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditor(
    initial: QuestionEntity?,
    initialCollectionIds: List<String>,
    initialContentBlocks: List<QuestionContentBlockEntity>,
    initialQuestionParts: List<QuestionPartEntity>,
    versions: List<QuestionVersionEntity>,
    collections: List<CollectionEntity>,
    onBack: () -> Unit,
    onRestoreVersion: (QuestionVersionEntity) -> Unit,
    onSave: (QuestionEntity, List<String>, List<QuestionContentBlockEntity>, List<QuestionPartEntity>) -> Unit
) {
    var prompt by remember(initial?.id) { mutableStateOf(initial?.prompt ?: "") }
    var answer by remember(initial?.id) { mutableStateOf(initial?.answer ?: "") }
    var explanation by remember(initial?.id) { mutableStateOf(initial?.explanation ?: "") }
    var subject by remember(initial?.id) { mutableStateOf(initial?.subject ?: "") }
    var chapter by remember(initial?.id) { mutableStateOf(initial?.chapter ?: "") }
    var topic by remember(initial?.id) { mutableStateOf(initial?.topic ?: "") }
    var type by remember(initial?.id) { mutableStateOf(initial?.type ?: "Flashcard") }
    var options by remember(initial?.id) { mutableStateOf(parseOptions(initial?.optionsJson ?: "[]").joinToString("\n")) }
    var latex by remember(initial?.id) { mutableStateOf(initial?.latex ?: "") }
    var tags by remember(initial?.id) { mutableStateOf(initial?.tagsCsv ?: "") }
    var exam by remember(initial?.id) { mutableStateOf(initial?.exam ?: "") }
    var sourceTitle by remember(initial?.id) { mutableStateOf(initial?.sourceTitle ?: "") }
    var sourceDetail by remember(initial?.id) { mutableStateOf(initial?.sourceDetail ?: "") }
    var difficulty by remember(initial?.id) { mutableStateOf(initial?.difficulty ?: "Unrated") }
    var status by remember(initial?.id) { mutableStateOf(initial?.status ?: "Draft") }
    var selectedCollections by remember(initial?.id) { mutableStateOf(initialCollectionIds.toSet()) }
    val contentBlocks = remember(initial?.id) { mutableStateListOf<QuestionContentBlockEntity>().apply { addAll(initialContentBlocks) } }
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var showVersionHistory by remember { mutableStateOf(false) }
    var imageTargetIndex by remember { mutableIntStateOf(-1) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val target = imageTargetIndex
        if (uri != null && target in contentBlocks.indices) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { input -> input.readBytes() }
                    ?: error("Unable to read selected image")
                if (bytes.isEmpty()) throw IllegalStateException("Selected image is empty")
                if (bytes.size > 1_500_000) throw IllegalStateException("Image is larger than 1.5 MB. Choose a smaller image.")
                val mime = context.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") }
                    ?: throw IllegalStateException("Please choose an image file")
                contentBlocks[target] = contentBlocks[target].copy(
                    mediaBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                    mediaMimeType = mime,
                    altText = contentBlocks[target].altText.ifBlank { "Question image" }
                )
            }.onFailure { error = it.message ?: "Could not load image" }
        }
    }
    val questionParts = remember(initial?.id) { mutableStateListOf<QuestionPartEntity>().apply { addAll(initialQuestionParts) } }
    var newBlockKind by remember(initial?.id) { mutableStateOf("TEXT") }
    var blockKindExpanded by remember(initial?.id) { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "New question" else "Edit question") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (initial != null) TextButton(onClick = { showVersionHistory = true }) { Text("History (${versions.size})") }
                    TextButton(onClick = {
                    if (prompt.isBlank() || (answer.isBlank() && type != "Cloze")) error = "Add both a question and an answer before saving."
                    else if (type == "Cloze" && !ClozeRules.hasCloze(prompt)) error = "Add at least one cloze deletion using {{c1::hidden text}}."
                    else if ((type == "MCQ" || type == "True/False") && options.lines().map { it.trim() }.filter { it.isNotBlank() }.size < 2) error = "An MCQ needs at least two options."
                    else if ((type == "MCQ" || type == "True/False") && options.lines().map { it.trim() }.filter { it.isNotBlank() }.none { it == answer.trim() }) error = "The correct answer must exactly match one of the options."
                    else if (QuestionAuthoringRules.validateParts(questionParts) != null) error = QuestionAuthoringRules.validateParts(questionParts)
                    else if (QuestionAuthoringRules.validateBlocks(contentBlocks) != null) error = QuestionAuthoringRules.validateBlocks(contentBlocks)
                    else {
                        val now = System.currentTimeMillis()
                        val parsed = options.lines().map { it.trim() }.filter { it.isNotBlank() }
                        val entity = QuestionEntity(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            prompt = prompt.trim(), answer = if (type == "Cloze") ClozeRules.answers(prompt).joinToString("; ") else answer.trim(), explanation = explanation.trim(),
                            subject = subject.trim(), chapter = chapter.trim(), topic = topic.trim(), type = type,
                            optionsJson = JSONArray(parsed).toString(), latex = latex.trim(), tagsCsv = tags.trim(), status = status,
                            createdAt = initial?.createdAt ?: now, updatedAt = now,
                            reviewCount = initial?.reviewCount ?: 0, lastReviewedAt = initial?.lastReviewedAt,
                            dueAt = initial?.dueAt, intervalDays = initial?.intervalDays ?: 0.0,
                            easeFactor = initial?.easeFactor ?: 2.5, repetitions = initial?.repetitions ?: 0,
                            lapseCount = initial?.lapseCount ?: 0, learningState = initial?.learningState ?: "New",
                            correctCount = initial?.correctCount ?: 0, incorrectCount = initial?.incorrectCount ?: 0,
                            fsrsStability = initial?.fsrsStability ?: 0.0, fsrsDifficulty = initial?.fsrsDifficulty ?: 0.0,
                            difficulty = difficulty, exam = exam.trim(),
                            sourceTitle = sourceTitle.trim().take(300), sourceDetail = sourceDetail.trim().take(1000)
                        )
                        onSave(entity, selectedCollections.toList(), contentBlocks.toList(), questionParts.toList())
                    }
                }) { Text("Save", fontWeight = FontWeight.Bold) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            Text("Select text, then choose a formatting action. With no selection, a placeholder is inserted.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SelectionFormattingField(prompt, { prompt = it; error = null }, "Question / Front", minLines = 3)
            if (prompt.isNotBlank()) Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Question preview", style = MaterialTheme.typography.labelSmall); RichContentText(if (type == "Cloze") ClozeRules.mask(prompt) else prompt) } }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Question type", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.AssistChip(onClick = { typeExpanded = true }, label = { Text(type) })
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("Flashcard", "MCQ", "True/False", "Numerical", "Cloze").forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = {
                        type = t
                        if (t == "True/False") { options = "True\nFalse"; answer = "True" }
                        typeExpanded = false
                    }) }
                }
            }
            if (type == "MCQ" || type == "True/False") OutlinedTextField(options, { options = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Options (one per line)") }, minLines = 2, supportingText = { Text("The answer field below must match one option exactly.") })
            if (type == "Cloze") Text("Use {{c1::hidden text}} in the question. Each cloze is hidden during study and shown when you reveal the answer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (type != "Cloze") SelectionFormattingField(answer, { answer = it; error = null }, if (type == "MCQ" || type == "True/False") "Correct option (exact text)" else if (type == "Numerical") "Expected numerical answer" else "Answer / Back", minLines = 2)
            if (answer.isNotBlank() && type != "MCQ" && type != "True/False" && type != "Cloze") Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Answer preview", style = MaterialTheme.typography.labelSmall); RichContentText(answer) } }
            SelectionFormattingField(explanation, { explanation = it }, "Explanation (optional)", minLines = 3)
            HorizontalDivider()
            Text("Source / provenance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(sourceTitle, { sourceTitle = it }, label = { Text("Source title (textbook, notes, document, website)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(sourceDetail, { sourceDetail = it }, label = { Text("Chapter, page, section or URL (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            if (explanation.isNotBlank()) Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Explanation preview", style = MaterialTheme.typography.labelSmall); RichContentText(explanation) } }
            HorizontalDivider()
            Text("Structured content blocks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Add ordered text, heading, list, or LaTeX blocks. They are saved with this question and shown during study.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { blockKindExpanded = true }) { Text("Add: $newBlockKind") }
                    DropdownMenu(expanded = blockKindExpanded, onDismissRequest = { blockKindExpanded = false }) {
                        listOf("TEXT", "HEADING", "LIST", "HINT", "LATEX", "IMAGE", "TABLE", "GRAPH", "DERIVATION").forEach { kind ->
                            DropdownMenuItem(text = { Text(kind) }, onClick = { newBlockKind = kind; blockKindExpanded = false })
                        }
                    }
                }
                Button(onClick = {
                    contentBlocks.add(QuestionContentBlockEntity(
                        id = UUID.randomUUID().toString(),
                        questionId = initial?.id ?: "pending",
                        position = contentBlocks.size,
                        kind = newBlockKind,
                        text = ""
                    ))
                }) { Text("Add block") }
            }
            contentBlocks.forEachIndexed { blockIndex, block ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${blockIndex + 1}. ${block.kind}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                if (blockIndex > 0) {
                                    val previous = contentBlocks[blockIndex - 1]
                                    contentBlocks[blockIndex - 1] = block.copy(position = blockIndex - 1)
                                    contentBlocks[blockIndex] = previous.copy(position = blockIndex)
                                }
                            }, enabled = blockIndex > 0) { Text("Move up") }
                            TextButton(onClick = {
                                if (blockIndex < contentBlocks.lastIndex) {
                                    val next = contentBlocks[blockIndex + 1]
                                    contentBlocks[blockIndex + 1] = block.copy(position = blockIndex + 1)
                                    contentBlocks[blockIndex] = next.copy(position = blockIndex)
                                }
                            }, enabled = blockIndex < contentBlocks.lastIndex) { Text("Move down") }
                            TextButton(onClick = { contentBlocks.removeAt(blockIndex) }) { Text("Delete") }
                        }
                        when (block.kind.uppercase()) {
                            "IMAGE" -> {
                                OutlinedButton(onClick = { imageTargetIndex = blockIndex; imagePicker.launch(arrayOf("image/*")) }) { Text(if (block.mediaBase64.isBlank()) "Choose image" else "Replace image") }
                                OutlinedTextField(block.altText, { value -> contentBlocks[blockIndex] = block.copy(altText = value.take(300)) }, label = { Text("Image description (alt text)") }, modifier = Modifier.fillMaxWidth())
                                if (block.mediaBase64.isNotBlank()) OfflineImageBlock(block.mediaBase64, block.altText)
                                Text("Image is stored inside the question and works offline. Maximum image size: 1.5 MB.", style = MaterialTheme.typography.bodySmall)
                            }
                            else -> {
                                val hint = when (block.kind.uppercase()) {
                                    "LATEX" -> "LaTeX source"
                                    "HINT" -> "Hint text (shown only when the learner reveals it)"
                                    "TABLE" -> "Table: separate columns with | and rows with new lines"
                                    "GRAPH" -> "Graph data: one x,y pair per line (example: 0,2)"
                                    "DERIVATION" -> "One step per line: title | LaTeX equation | explanation"
                                    else -> "Block content"
                                }
                                if (block.kind.uppercase() in setOf("TEXT", "HEADING", "LIST", "HINT")) {
                                    SelectionFormattingField(
                                        block.text,
                                        { value -> contentBlocks[blockIndex] = block.copy(text = value.take(20000)) },
                                        hint,
                                        minLines = 3
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = block.text,
                                        onValueChange = { value -> contentBlocks[blockIndex] = block.copy(text = value.take(20000)) },
                                        label = { Text(hint) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = if (block.kind == "LATEX") 2 else 3
                                    )
                                }
                                when (block.kind.uppercase()) {
                                    "LATEX" -> if (block.text.isNotBlank()) LatexFormulaView(block.text)
                                    "HEADING" -> if (block.text.isNotBlank()) Text(block.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    "HINT" -> if (block.text.isNotBlank()) Text("Hint preview: ${block.text}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    "LIST" -> block.text.lineSequence().filter { it.isNotBlank() }.forEach { line -> RichContentText(if (line.trimStart().startsWith("-")) line else "- $line") }
                                    "TABLE" -> if (block.text.isNotBlank()) ContentTable(block.text)
                                    "GRAPH" -> if (block.text.isNotBlank()) ContentGraph(block.text)
                                    "DERIVATION" -> DerivationBlock(block.text)
                                    else -> if (block.text.isNotBlank()) RichContentText(block.text)
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
            Text("Multipart question parts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Create separately scored parts with individual point values. Exams automatically mark supported answers, then allow awarded points to be adjusted for method marks or other partial credit.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = {
                questionParts.add(QuestionPartEntity(
                    id = UUID.randomUUID().toString(),
                    questionId = initial?.id ?: "pending",
                    position = questionParts.size,
                    prompt = "",
                    answer = "",
                    points = 1.0
                ))
            }) { Text("Add question part") }
            questionParts.forEachIndexed { partIndex, part ->
                key(part.id) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Part ${partIndex + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                if (partIndex > 0) {
                                    val previous = questionParts[partIndex - 1]
                                    questionParts[partIndex - 1] = part.copy(position = partIndex - 1)
                                    questionParts[partIndex] = previous.copy(position = partIndex)
                                }
                            }, enabled = partIndex > 0) { Text("↑") }
                            TextButton(onClick = {
                                if (partIndex < questionParts.lastIndex) {
                                    val next = questionParts[partIndex + 1]
                                    questionParts[partIndex + 1] = part.copy(position = partIndex + 1)
                                    questionParts[partIndex] = next.copy(position = partIndex)
                                }
                            }, enabled = partIndex < questionParts.lastIndex) { Text("↓") }
                            TextButton(onClick = { questionParts.removeAt(partIndex) }) { Text("Delete") }
                        }
                        SelectionFormattingField(part.prompt, { questionParts[partIndex] = part.copy(prompt = it.take(10000)) }, "Part prompt", minLines = 2)
                        SelectionFormattingField(part.answer, { questionParts[partIndex] = part.copy(answer = it.take(10000)) }, "Part answer", minLines = 2)
                        SelectionFormattingField(part.explanation, { questionParts[partIndex] = part.copy(explanation = it.take(10000)) }, "Part explanation", minLines = 2)
                        OutlinedTextField(part.acceptedAnswers, { questionParts[partIndex] = part.copy(acceptedAnswers = it.take(20000)) }, label = { Text("Accepted alternatives (one per line or separated by ||)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        OutlinedTextField(part.absoluteTolerance.toString(), { value -> value.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }?.let { questionParts[partIndex] = part.copy(absoluteTolerance = it.coerceAtMost(1_000_000.0)) } }, label = { Text("Numeric absolute tolerance (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(part.relativeTolerance.toString(), { value -> value.toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..1.0 }?.let { questionParts[partIndex] = part.copy(relativeTolerance = it) } }, label = { Text("Numeric relative tolerance (0–1, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(
                            value = part.points.toString(),
                            onValueChange = { value -> value.toDoubleOrNull()?.takeIf { it in 0.01..10000.0 }?.let { questionParts[partIndex] = part.copy(points = it) } },
                            label = { Text("Points (0.01–10000)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                }
            }
            HorizontalDivider()
            Text("Organization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(subject, { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(chapter, { chapter = it }, label = { Text("Chapter") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(topic, { topic = it }, label = { Text("Topic / Subtopic") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(tags, { tags = it }, label = { Text("Tags (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(exam, { exam = it }, label = { Text("Exam / syllabus (e.g. MDCAT, Board)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Difficulty", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                Box {
                    androidx.compose.material3.AssistChip(onClick = { difficultyExpanded = true }, label = { Text(difficulty) })
                    DropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                        listOf("Unrated", "Easy", "Medium", "Hard").forEach { value ->
                            DropdownMenuItem(text = { Text(value) }, onClick = { difficulty = value; difficultyExpanded = false })
                        }
                    }
                }
            }
            HorizontalDivider()
            Text("Equation (LaTeX)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(latex, { latex = it }, label = { Text("Example: v = \\frac{\\Delta x}{\\Delta t}") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            if (latex.isNotBlank()) Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Rendered offline LaTeX preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    LatexFormulaView(latex, Modifier.fillMaxWidth())
                }
            }
            HorizontalDivider()
            Text("Add to collections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (collections.isEmpty()) Text("Create collections from the Collections tab first.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else collections.forEach { collection ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                    selectedCollections = if (collection.id in selectedCollections) selectedCollections - collection.id else selectedCollections + collection.id
                }) {
                    Checkbox(checked = collection.id in selectedCollections, onCheckedChange = { checked -> selectedCollections = if (checked) selectedCollections + collection.id else selectedCollections - collection.id })
                    Text(collection.name)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text("Status: ", fontWeight = FontWeight.SemiBold)
                listOf("Draft", "Unverified", "Verified", "Incorrect", "Archived").forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) }, modifier = Modifier.padding(end = 6.dp)) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (showVersionHistory) {
        AlertDialog(
            onDismissRequest = { showVersionHistory = false },
            title = { Text("Question version history") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Each saved edit stores the previous content. Restoring a version changes question content and collections, but preserves review history and FSRS scheduling.", style = MaterialTheme.typography.bodySmall)
                    if (versions.isEmpty()) Text("No previous versions yet. Edit and save this question to create the first snapshot.")
                    versions.forEach { version ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Version ${version.versionNumber}", fontWeight = FontWeight.SemiBold)
                                    Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(version.savedAt)), style = MaterialTheme.typography.bodySmall)
                                    val previousPrompt = runCatching { JSONObject(version.snapshotJson).optString("prompt") }.getOrDefault("")
                                    if (previousPrompt.isNotBlank()) Text(previousPrompt.take(100), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                    val currentQuestion = initial
                                    if (currentQuestion != null) {
                                        val currentSnapshot = QuestionVersionRules.createSnapshot(
                                            currentQuestion, initialContentBlocks, initialQuestionParts, initialCollectionIds
                                        )
                                        val changes = runCatching {
                                            QuestionVersionRules.compare(version.snapshotJson, currentSnapshot)
                                        }.getOrDefault(emptyList())
                                        Text(
                                            if (changes.isEmpty()) "No content differences" else "${changes.size} changed field(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        changes.take(24).forEach { change ->
                                            Column(Modifier.padding(start = 8.dp, top = 3.dp)) {
                                                Text(change.path, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                                Text("Before: ${change.previous.take(180)}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                                Text("Current: ${change.current.take(180)}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                            }
                                        }
                                        if (changes.size > 24) Text("${changes.size - 24} more changes not shown", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                TextButton(onClick = { showVersionHistory = false; onRestoreVersion(version) }) { Text("Restore") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showVersionHistory = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun BackupScreen(
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onExportAnki: (Uri) -> Unit,
    onImportCsv: (Uri) -> Unit,
    onImportAnki: (Uri) -> Unit
) {
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json"), onResult = { it?.let(onExport) })
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), onResult = { it?.let(onImport) })
    val csvExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv"), onResult = { it?.let(onExportCsv) })
    val ankiExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/tab-separated-values"), onResult = { it?.let(onExportAnki) })
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), onResult = { it?.let(onImportCsv) })
    val ankiImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), onResult = { it?.let(onImportAnki) })
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Backup & import/export", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("JSON is the complete backup format. CSV and Anki TSV are lightweight interchange formats for basic question fields; they do not carry review history, collections, images, rich blocks or multipart grading.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ImportExport, null, tint = MaterialTheme.colorScheme.primary)
                Text("Complete JSON backup", fontWeight = FontWeight.SemiBold)
                Text("Includes questions, collections, review history, scheduling, settings, rich content, saved sessions, exam attempts and question version history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { exportLauncher.launch("studyforge-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("Export full backup") }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("Import full backup") }
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CSV question interchange", fontWeight = FontWeight.SemiBold)
                Text("Use a spreadsheet or another question app. CSV imports merge rows with matching IDs and create new IDs when blank.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { csvExportLauncher.launch("studyforge-questions.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Export questions as CSV") }
                OutlinedButton(onClick = { csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) { Text("Import questions from CSV") }
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Anki TSV interchange", fontWeight = FontWeight.SemiBold)
                Text("Exports Front, Back and Tags, and imports standard three-field Anki text files. Rich media and scheduling are not transferred.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { ankiExportLauncher.launch("studyforge-anki.txt") }, modifier = Modifier.fillMaxWidth()) { Text("Export Anki TSV") }
                OutlinedButton(onClick = { ankiImportLauncher.launch(arrayOf("text/tab-separated-values", "text/plain", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) { Text("Import Anki TSV") }
            }
        }
        Text("Keep a backup somewhere outside your phone as well. JSON is the only complete restore format. Uninstalling the app may remove its local database.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Quiz, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun parseOptions(json: String): List<String> = runCatching {
    val array = JSONArray(json)
    (0 until array.length()).map { array.optString(it) }
}.getOrDefault(emptyList())

private suspend fun exportQuestionTable(context: Context, questions: List<QuestionEntity>, uri: Uri, anki: Boolean) {
    val content = if (anki) QuestionInterchange.exportAnkiTsv(questions) else QuestionInterchange.exportCsv(questions)
    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(content) }
        ?: error("Unable to open the selected export location")
}

private suspend fun readQuestionTablePreview(
    context: Context,
    existing: List<QuestionEntity>,
    uri: Uri,
    anki: Boolean
): QuestionInterchange.ImportPreview {
    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        ?: error("Unable to read the selected file")
    require(raw.length <= 20_000_000) { "The import file is too large (20 MB maximum)" }
    val preview = if (anki) QuestionInterchange.previewAnkiTsv(raw, existing) else QuestionInterchange.previewCsv(raw, existing)
    require(preview.totalRows <= 50_000) { "The file contains too many questions" }
    return preview
}

private suspend fun importQuestionTable(
    database: StudyDatabase,
    preview: QuestionInterchange.ImportPreview
): TableImportResult {
    require(preview.canImport) { "No valid, non-duplicate rows to import" }
    val dao = database.dao()
    var updatedCount = 0
    database.withTransaction {
        val existing = dao.getAllQuestions().associateBy { it.id }
        val imported = preview.rows.map { row ->
            val id = row.id.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val old = existing[id]
            val options = row.options.split('|').map { it.trim() }.filter { it.isNotBlank() }
            val type = row.type.takeIf { it in setOf("Flashcard", "MCQ", "True/False", "Numerical", "Cloze") } ?: "Flashcard"
            val cleanStatus = row.status.takeIf { it in setOf("Draft", "Verified", "Unverified", "Incorrect", "Archived") } ?: "Draft"
            QuestionEntity(
                id = id, prompt = row.prompt.take(100_000), answer = row.answer.take(100_000),
                explanation = row.explanation.take(100_000), subject = row.subject.take(120), chapter = row.chapter.take(120),
                topic = row.topic.take(120), type = type, optionsJson = JSONArray(options).toString(), latex = row.latex.take(20_000),
                tagsCsv = row.tags.take(1000), status = cleanStatus, createdAt = old?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(), reviewCount = old?.reviewCount ?: 0, lastReviewedAt = old?.lastReviewedAt,
                dueAt = old?.dueAt, intervalDays = old?.intervalDays ?: 0.0, easeFactor = old?.easeFactor ?: 2.5,
                repetitions = old?.repetitions ?: 0, lapseCount = old?.lapseCount ?: 0, learningState = old?.learningState ?: "New",
                learningStepIndex = old?.learningStepIndex ?: -1, correctCount = old?.correctCount ?: 0, incorrectCount = old?.incorrectCount ?: 0,
                fsrsStability = old?.fsrsStability ?: 0.0, fsrsDifficulty = old?.fsrsDifficulty ?: 0.0,
                difficulty = row.difficulty.takeIf { it in setOf("Unrated", "Easy", "Medium", "Hard") } ?: "Unrated", exam = row.exam.take(120),
                sourceTitle = row.sourceTitle.take(300).ifBlank { old?.sourceTitle.orEmpty() },
                sourceDetail = row.sourceDetail.take(1000).ifBlank { old?.sourceDetail.orEmpty() }
            )
        }
        imported.forEach { updated ->
            val old = existing[updated.id]
            if (old != null) {
                updatedCount++
                if (old.copy(updatedAt = updated.updatedAt) != updated) {
                    dao.insertQuestionVersion(QuestionVersionEntity(
                        id = UUID.randomUUID().toString(), questionId = old.id,
                        versionNumber = dao.nextQuestionVersion(old.id), savedAt = System.currentTimeMillis(),
                        snapshotJson = QuestionVersionRules.createSnapshot(old, dao.getContentBlocks(old.id), dao.getQuestionParts(old.id), dao.getCollectionIds(old.id))
                    ))
                }
            }
        }
        dao.upsertQuestions(imported)
    }
    return TableImportResult(imported = preview.rows.size, updated = updatedCount, skipped = preview.skippedCount, issues = preview.issues)
}

private suspend fun exportBackup(context: Context, database: StudyDatabase, uri: Uri) {
    // Read every table from one Room transaction. Without this snapshot, edits made
    // during export could produce a backup whose questions, links and dependent
    // records represent different moments in time.
    data class BackupSnapshot(
        val questions: List<QuestionEntity>,
        val collections: List<CollectionEntity>,
        val links: List<QuestionCollectionCrossRef>,
        val reviewLogs: List<ReviewLogEntity>,
        val settings: StudySettingsEntity,
        val savedPresets: List<SavedFilterPresetEntity>,
        val tags: List<TagEntity>,
        val contentBlocks: List<QuestionContentBlockEntity>,
        val questionParts: List<QuestionPartEntity>,
        val sessionPresets: List<StudySessionPresetEntity>,
        val examAttempts: List<ExamAttemptEntity>,
        val questionVersions: List<QuestionVersionEntity>
    )
    val snapshot = database.withTransaction {
        val dao = database.dao()
        BackupSnapshot(
            questions = dao.getAllQuestions(),
            collections = dao.getAllCollections(),
            links = dao.getAllLinks(),
            reviewLogs = dao.getAllReviewLogs(),
            settings = dao.getSettings() ?: StudySettingsEntity.DEFAULT,
            savedPresets = dao.getAllSavedFilterPresets(),
            tags = dao.getAllTags(),
            contentBlocks = dao.getAllContentBlocks(),
            questionParts = dao.getAllQuestionParts(),
            sessionPresets = dao.getAllStudySessionPresets(),
            examAttempts = dao.getAllExamAttempts(),
            questionVersions = dao.getAllQuestionVersions()
        )
    }
    val questions = snapshot.questions
    val collections = snapshot.collections
    val links = snapshot.links
    val reviewLogs = snapshot.reviewLogs
    val settings = snapshot.settings
    val savedPresets = snapshot.savedPresets
    val tags = snapshot.tags
    val contentBlocks = snapshot.contentBlocks
    val questionParts = snapshot.questionParts
    val sessionPresets = snapshot.sessionPresets
    val examAttempts = snapshot.examAttempts
    val questionVersions = snapshot.questionVersions
    val root = JSONObject().put("app", "StudyForge").put("schemaVersion", 15).put("exportedAt", System.currentTimeMillis())
    root.put("questions", JSONArray().apply {
        questions.forEach { q -> put(JSONObject()
            .put("id", q.id).put("prompt", q.prompt).put("answer", q.answer).put("explanation", q.explanation)
            .put("subject", q.subject).put("chapter", q.chapter).put("topic", q.topic).put("type", q.type)
            .put("optionsJson", q.optionsJson).put("latex", q.latex).put("tagsCsv", q.tagsCsv).put("status", q.status)
            .put("createdAt", q.createdAt).put("updatedAt", q.updatedAt).put("reviewCount", q.reviewCount)
            .put("lastReviewedAt", q.lastReviewedAt ?: JSONObject.NULL)
            .put("dueAt", q.dueAt ?: JSONObject.NULL).put("intervalDays", q.intervalDays).put("easeFactor", q.easeFactor)
            .put("repetitions", q.repetitions).put("lapseCount", q.lapseCount).put("learningState", q.learningState)
            .put("correctCount", q.correctCount).put("incorrectCount", q.incorrectCount)
            .put("fsrsStability", q.fsrsStability).put("fsrsDifficulty", q.fsrsDifficulty)
            .put("difficulty", q.difficulty).put("exam", q.exam).put("learningStepIndex", q.learningStepIndex)
            .put("sourceTitle", q.sourceTitle).put("sourceDetail", q.sourceDetail)) }
    })
    root.put("collections", JSONArray().apply {
        collections.forEach { c -> put(JSONObject().put("id", c.id).put("name", c.name).put("description", c.description).put("createdAt", c.createdAt)) }
    })
    root.put("links", JSONArray().apply { links.forEach { put(JSONObject().put("questionId", it.questionId).put("collectionId", it.collectionId)) } })
    root.put("reviewLogs", JSONArray().apply {
        reviewLogs.forEach { log -> put(JSONObject()
            .put("id", log.id).put("questionId", log.questionId).put("reviewedAt", log.reviewedAt)
            .put("rating", log.rating).put("wasCorrect", log.wasCorrect ?: JSONObject.NULL)
            .put("mistakeCategory", log.mistakeCategory).put("reviewMode", log.reviewMode)
            .put("scheduledDueAt", log.scheduledDueAt ?: JSONObject.NULL)) }
    })
    root.put("settings", JSONObject()
        .put("desiredRetention", settings.desiredRetention).put("maxIntervalDays", settings.maxIntervalDays)
        .put("newCardsPerDay", settings.newCardsPerDay).put("reviewCardsPerDay", settings.reviewCardsPerDay)
        .put("reviewOrder", settings.reviewOrder).put("learningStepsMinutesCsv", settings.learningStepsMinutesCsv)
        .put("relearningStepsMinutesCsv", settings.relearningStepsMinutesCsv).put("examQuestionCount", settings.examQuestionCount)
        .put("examDurationMinutes", settings.examDurationMinutes).put("examRandomize", settings.examRandomize))
    root.put("tags", JSONArray().apply { tags.forEach { put(JSONObject().put("id", it.id).put("name", it.name).put("createdAt", it.createdAt)) } })
    root.put("savedFilterPresets", JSONArray().apply {
        savedPresets.forEach { preset ->
            put(JSONObject()
                .put("id", preset.id)
                .put("name", preset.name)
                .put("query", preset.query)
                .put("type", preset.type)
                .put("status", preset.status)
                .put("subject", preset.subject)
                .put("chapter", preset.chapter)
                .put("topic", preset.topic)
                .put("tag", preset.tag)
                .put("collectionId", preset.collectionId ?: JSONObject.NULL)
                .put("difficulty", preset.difficulty)
                .put("exam", preset.exam)
                .put("performance", preset.performance)
                .put("sourceTitle", preset.sourceTitle)
                .put("sourceDetail", preset.sourceDetail)
                .put("createdAfter", preset.createdAfter).put("createdBefore", preset.createdBefore)
                .put("updatedAfter", preset.updatedAfter).put("updatedBefore", preset.updatedBefore)
                .put("createdAt", preset.createdAt)
                .put("updatedAt", preset.updatedAt))
        }
    })
    root.put("contentBlocks", JSONArray().apply {
        contentBlocks.forEach { block -> put(JSONObject()
            .put("id", block.id).put("questionId", block.questionId).put("position", block.position)
            .put("kind", block.kind).put("text", block.text).put("mediaBase64", block.mediaBase64)
            .put("mediaMimeType", block.mediaMimeType).put("altText", block.altText)) }
    })
    root.put("questionParts", JSONArray().apply {
        questionParts.forEach { part -> put(JSONObject()
            .put("id", part.id).put("questionId", part.questionId).put("position", part.position)
            .put("prompt", part.prompt).put("answer", part.answer).put("explanation", part.explanation)
            .put("points", part.points).put("optionsJson", part.optionsJson)
            .put("absoluteTolerance", part.absoluteTolerance).put("relativeTolerance", part.relativeTolerance).put("acceptedAnswers", part.acceptedAnswers)) }
    })
    root.put("studySessionPresets", JSONArray().apply {
        sessionPresets.forEach { preset -> put(JSONObject()
            .put("id", preset.id).put("name", preset.name).put("mode", preset.mode)
            .put("subject", preset.subject).put("chapter", preset.chapter).put("topic", preset.topic)
            .put("tag", preset.tag).put("difficulty", preset.difficulty).put("exam", preset.exam)
            .put("questionType", preset.questionType).put("status", preset.status).put("performance", preset.performance)
            .put("collectionId", preset.collectionId).put("questionLimit", preset.questionLimit).put("createdAt", preset.createdAt).put("updatedAt", preset.updatedAt)) }
    })
    root.put("questionVersions", JSONArray().apply {
        questionVersions.forEach { version -> put(JSONObject()
            .put("id", version.id).put("questionId", version.questionId).put("versionNumber", version.versionNumber)
            .put("savedAt", version.savedAt).put("snapshotJson", version.snapshotJson)) }
    })
    root.put("examAttempts", JSONArray().apply {
        examAttempts.forEach { attempt -> put(JSONObject()
            .put("id", attempt.id).put("startedAt", attempt.startedAt).put("completedAt", attempt.completedAt)
            .put("durationSeconds", attempt.durationSeconds).put("questionCount", attempt.questionCount)
            .put("answeredCount", attempt.answeredCount).put("markedCount", attempt.markedCount)
            .put("earnedPoints", attempt.earnedPoints).put("possiblePoints", attempt.possiblePoints)
            .put("scorePercent", attempt.scorePercent).put("itemsJson", attempt.itemsJson).put("label", attempt.label)) }
    })
    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
        ?: error("Could not open the selected file for writing")
}

private suspend fun importBackup(context: Context, database: StudyDatabase, uri: Uri) {
    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        ?: error("Could not read the selected file")
    val root = JSONObject(raw)
    require(root.optString("app") == "StudyForge") { "This file is not a StudyForge backup" }
    val schemaVersion = root.optInt("schemaVersion", -1)
    require(schemaVersion in 1..15) { "Unsupported backup version" }
    val questions = mutableListOf<QuestionEntity>()
    val qArray = root.optJSONArray("questions") ?: JSONArray()
    for (i in 0 until qArray.length()) {
        val o = qArray.getJSONObject(i)
        questions += QuestionEntity(
            id = o.getString("id"), prompt = o.optString("prompt"), answer = o.optString("answer"),
            explanation = o.optString("explanation"), subject = o.optString("subject"), chapter = o.optString("chapter"),
            topic = o.optString("topic"), type = o.optString("type", "Flashcard"), optionsJson = o.optString("optionsJson", "[]"),
            latex = o.optString("latex"), tagsCsv = o.optString("tagsCsv"), status = o.optString("status", "Draft"),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
            reviewCount = o.optInt("reviewCount", 0), lastReviewedAt = if (o.isNull("lastReviewedAt")) null else o.optLong("lastReviewedAt"),
            dueAt = if (o.isNull("dueAt")) null else o.optLong("dueAt"),
            intervalDays = o.optDouble("intervalDays", 0.0), easeFactor = o.optDouble("easeFactor", 2.5),
            repetitions = o.optInt("repetitions", 0), lapseCount = o.optInt("lapseCount", 0),
            learningState = o.optString("learningState", "New"), correctCount = o.optInt("correctCount", 0),
            incorrectCount = o.optInt("incorrectCount", 0),
            fsrsStability = o.optDouble("fsrsStability", 0.0), fsrsDifficulty = o.optDouble("fsrsDifficulty", 0.0),
            difficulty = o.optString("difficulty", "Unrated").takeIf { it in setOf("Unrated", "Easy", "Medium", "Hard") } ?: "Unrated",
            exam = o.optString("exam").take(120),
            sourceTitle = if (schemaVersion >= 13) o.optString("sourceTitle").take(300) else "",
            sourceDetail = if (schemaVersion >= 13) o.optString("sourceDetail").take(1000) else "",
            learningStepIndex = o.optInt("learningStepIndex", -1).coerceAtLeast(-1)
        )
    }
    val collections = mutableListOf<CollectionEntity>()
    val cArray = root.optJSONArray("collections") ?: JSONArray()
    for (i in 0 until cArray.length()) {
        val o = cArray.getJSONObject(i)
        collections += CollectionEntity(o.getString("id"), o.optString("name"), o.optString("description"), o.optLong("createdAt", System.currentTimeMillis()))
    }
    val links = mutableListOf<QuestionCollectionCrossRef>()
    val lArray = root.optJSONArray("links") ?: JSONArray()
    for (i in 0 until lArray.length()) {
        val o = lArray.getJSONObject(i)
        links += QuestionCollectionCrossRef(o.getString("questionId"), o.getString("collectionId"))
    }
    val reviewLogs = mutableListOf<ReviewLogEntity>()
    val reviewArray = root.optJSONArray("reviewLogs") ?: JSONArray()
    for (i in 0 until reviewArray.length()) {
        val o = reviewArray.getJSONObject(i)
        reviewLogs += ReviewLogEntity(
            id = o.getString("id"), questionId = o.getString("questionId"),
            reviewedAt = o.optLong("reviewedAt", System.currentTimeMillis()), rating = o.optString("rating", "Good"),
            wasCorrect = if (o.isNull("wasCorrect")) null else o.optBoolean("wasCorrect"),
            mistakeCategory = o.optString("mistakeCategory"), reviewMode = o.optString("reviewMode", "Review"),
            scheduledDueAt = if (o.isNull("scheduledDueAt")) null else o.optLong("scheduledDueAt")
        )
    }
    val settingsObject = root.optJSONObject("settings")
    val importedSettings = settingsObject?.let { o -> StudySettingsEntity(
        desiredRetention = o.optDouble("desiredRetention", 0.90).coerceIn(0.80, 0.97),
        maxIntervalDays = o.optInt("maxIntervalDays", 36500).coerceIn(1, 36500),
        newCardsPerDay = o.optInt("newCardsPerDay", 20).coerceIn(0, 500),
        reviewCardsPerDay = o.optInt("reviewCardsPerDay", 200).coerceIn(0, 2000),
        reviewOrder = o.optString("reviewOrder", "Due first"),
        learningStepsMinutesCsv = o.optString("learningStepsMinutesCsv", "10"),
        relearningStepsMinutesCsv = o.optString("relearningStepsMinutesCsv", "10"),
        examQuestionCount = o.optInt("examQuestionCount", 20).coerceIn(1, 200),
        examDurationMinutes = o.optInt("examDurationMinutes", 30).coerceIn(1, 240),
        examRandomize = o.optBoolean("examRandomize", true)
    ) }
    val importedTags = mutableListOf<TagEntity>()
    val tagArray = root.optJSONArray("tags") ?: JSONArray()
    for (i in 0 until tagArray.length()) {
        val o = tagArray.getJSONObject(i)
        val name = o.optString("name").trim().take(40)
        if (name.isNotBlank() && importedTags.none { it.name.equals(name, ignoreCase = true) }) importedTags += TagEntity(name.lowercase(), name, o.optLong("createdAt", System.currentTimeMillis()))
    }
    val savedPresets = mutableListOf<SavedFilterPresetEntity>()
    val presetArray = root.optJSONArray("savedFilterPresets") ?: JSONArray()
    for (i in 0 until presetArray.length()) {
        val o = presetArray.getJSONObject(i)
        val safeName = o.optString("name").trim().take(50)
        if (safeName.isNotBlank()) {
            savedPresets += SavedFilterPresetEntity(
                id = o.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                name = safeName, query = o.optString("query").take(300),
                type = o.optString("type", "All").take(30), status = o.optString("status", "All").take(30),
                subject = o.optString("subject").take(120), chapter = o.optString("chapter").take(120),
                topic = o.optString("topic").take(120), tag = o.optString("tag").take(80),
                collectionId = if (o.isNull("collectionId")) null else o.optString("collectionId"),
                difficulty = o.optString("difficulty", "All").take(20), exam = o.optString("exam").take(120),
                performance = o.optString("performance", "All").take(30),
                sourceTitle = o.optString("sourceTitle").take(300), sourceDetail = o.optString("sourceDetail").take(1000),
                createdAfter = o.optString("createdAfter").take(10), createdBefore = o.optString("createdBefore").take(10),
                updatedAfter = o.optString("updatedAfter").take(10), updatedBefore = o.optString("updatedBefore").take(10),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }
    val importedContentBlocks = mutableListOf<QuestionContentBlockEntity>()
    val contentArray = root.optJSONArray("contentBlocks") ?: JSONArray()
    for (i in 0 until contentArray.length()) {
        val o = contentArray.getJSONObject(i)
        val allowedKinds = setOf("text", "heading", "latex", "image", "table", "graph", "list", "divider", "derivation")
        val kind = o.optString("kind", "text").take(30).lowercase().let { if (it in allowedKinds) it else "text" }
        // Match the editor's 1.5 MB image limit and reject malformed Base64 before Room writes it.
        val encoded = MediaImportRules.sanitizeBase64Image(o.optString("mediaBase64")).orEmpty()
        importedContentBlocks += QuestionContentBlockEntity(
            id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
            questionId = o.getString("questionId"), position = o.optInt("position", i).coerceAtLeast(0),
            kind = kind, text = o.optString("text").take(100_000), mediaBase64 = encoded,
            mediaMimeType = o.optString("mediaMimeType").take(100).takeIf { it.startsWith("image/") }.orEmpty(), altText = o.optString("altText").take(500)
        )
    }
    val importedQuestionParts = mutableListOf<QuestionPartEntity>()
    val partsArray = root.optJSONArray("questionParts") ?: JSONArray()
    for (i in 0 until partsArray.length()) {
        val o = partsArray.getJSONObject(i)
        val points = o.optDouble("points", 1.0).takeIf { it.isFinite() && it > 0.0 } ?: 1.0
        importedQuestionParts += QuestionPartEntity(
            id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
            questionId = o.getString("questionId"), position = o.optInt("position", i).coerceAtLeast(0),
            prompt = o.optString("prompt").take(100_000), answer = o.optString("answer").take(100_000),
            explanation = o.optString("explanation").take(100_000), points = points.coerceAtMost(10000.0),
            optionsJson = o.optString("optionsJson", "[]").take(100_000),
            absoluteTolerance = o.optDouble("absoluteTolerance", 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: 0.0,
            relativeTolerance = o.optDouble("relativeTolerance", 0.0).takeIf { it.isFinite() && it in 0.0..1.0 } ?: 0.0,
            acceptedAnswers = o.optString("acceptedAnswers").take(20_000)
        )
    }
    val importedSessionPresets = mutableListOf<StudySessionPresetEntity>()
    val sessionsArray = root.optJSONArray("studySessionPresets") ?: JSONArray()
    for (i in 0 until sessionsArray.length()) {
        val o = sessionsArray.getJSONObject(i)
        val name = o.optString("name").trim().take(50)
        if (name.isNotBlank()) importedSessionPresets += StudySessionPresetEntity(
            id = o.optString("id").ifBlank { UUID.randomUUID().toString() }, name = name,
            mode = o.optString("mode", "Practice").take(30).takeIf { it in setOf("Due", "All", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam") } ?: "Practice", subject = o.optString("subject").take(120),
            chapter = o.optString("chapter").take(120), topic = o.optString("topic").take(120),
            tag = o.optString("tag").take(80), difficulty = o.optString("difficulty", "All").take(20),
            exam = o.optString("exam").take(120), questionType = o.optString("questionType", "All").take(30),
            status = o.optString("status", "All").take(30), performance = o.optString("performance", "All").take(30),
            collectionId = o.optString("collectionId").take(100), questionLimit = o.optInt("questionLimit", 20).coerceIn(1, 500),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    val importedExamAttempts = mutableListOf<ExamAttemptEntity>()
    if (schemaVersion >= 10) {
        val attemptsArray = root.optJSONArray("examAttempts") ?: JSONArray()
        for (i in 0 until attemptsArray.length()) {
            val o = attemptsArray.getJSONObject(i)
            val possible = o.optDouble("possiblePoints", 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
            val earned = o.optDouble("earnedPoints", 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
            val score = o.optDouble("scorePercent", if (possible > 0.0) earned / possible * 100.0 else 0.0)
                .takeIf { it.isFinite() }?.coerceIn(0.0, 100.0) ?: 0.0
            val items = o.optString("itemsJson", "[]")
            // Reject malformed item snapshots rather than importing opaque invalid JSON.
            JSONArray(items)
            importedExamAttempts += ExamAttemptEntity(
                id = o.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                startedAt = o.optLong("startedAt", System.currentTimeMillis()),
                completedAt = o.optLong("completedAt", System.currentTimeMillis()),
                durationSeconds = o.optInt("durationSeconds", 0).coerceIn(0, 86_400),
                questionCount = o.optInt("questionCount", 0).coerceIn(0, 500),
                answeredCount = o.optInt("answeredCount", 0).coerceIn(0, o.optInt("questionCount", 0).coerceIn(0, 500)),
                markedCount = o.optInt("markedCount", 0).coerceIn(0, o.optInt("questionCount", 0).coerceIn(0, 500)),
                earnedPoints = earned.coerceAtMost(1_000_000.0), possiblePoints = possible.coerceAtMost(1_000_000.0),
                scorePercent = score, itemsJson = items, label = o.optString("label", "Timed exam").take(100)
            )
        }
    }

    val importedQuestionVersions = mutableListOf<QuestionVersionEntity>()
    if (schemaVersion >= 12) {
        val versionsArray = root.optJSONArray("questionVersions") ?: JSONArray()
        require(versionsArray.length() <= 100_000) { "Too many question versions in backup" }
        for (i in 0 until versionsArray.length()) {
            val o = versionsArray.getJSONObject(i)
            val snapshot = o.optString("snapshotJson")
            require(snapshot.length <= 20_000_000) { "A question version snapshot is too large" }
            val snapshotObject = JSONObject(snapshot)
            val questionId = o.optString("questionId").take(100)
            require(questionId.isNotBlank() && snapshotObject.optString("id") == questionId) { "Question version identity is invalid" }
            importedQuestionVersions += QuestionVersionEntity(
                id = o.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                questionId = questionId,
                versionNumber = o.optInt("versionNumber", 1).coerceAtLeast(1),
                savedAt = o.optLong("savedAt", System.currentTimeMillis()), snapshotJson = snapshot
            )
        }
    }

    database.withTransaction {
        val dao = database.dao()
        val existingById = dao.getAllQuestions().associateBy { it.id }
        val safeQuestions = questions.map { imported ->
            val existing = existingById[imported.id]
            if (existing == null) imported else imported.copy(
                fsrsStability = if (schemaVersion < 3) existing.fsrsStability else imported.fsrsStability,
                fsrsDifficulty = if (schemaVersion < 3) existing.fsrsDifficulty else imported.fsrsDifficulty,
                difficulty = if (schemaVersion < 4) existing.difficulty else imported.difficulty,
                exam = if (schemaVersion < 4) existing.exam else imported.exam,
                learningStepIndex = if (schemaVersion < 5) existing.learningStepIndex else imported.learningStepIndex,
                sourceTitle = if (schemaVersion < 13) existing.sourceTitle else imported.sourceTitle,
                sourceDetail = if (schemaVersion < 13) existing.sourceDetail else imported.sourceDetail
            )
        }
        val pendingVersionSnapshots = safeQuestions.mapNotNull { incoming ->
            val old = existingById[incoming.id] ?: return@mapNotNull null
            val oldBlocks = dao.getContentBlocks(old.id)
            val oldParts = dao.getQuestionParts(old.id)
            val incomingBlocks = importedContentBlocks.filter { it.questionId == old.id }.sortedBy { it.position }
            val incomingParts = importedQuestionParts.filter { it.questionId == old.id }.sortedBy { it.position }
            val changed = old.copy(updatedAt = incoming.updatedAt) != incoming ||
                (schemaVersion >= 8 && incomingBlocks != oldBlocks) ||
                (schemaVersion >= 8 && incomingParts != oldParts)
            if (changed) old.id to QuestionVersionRules.createSnapshot(old, oldBlocks, oldParts, dao.getCollectionIds(old.id)) else null
        }
        dao.upsertQuestions(safeQuestions)
        dao.upsertCollections(collections)
        if (schemaVersion >= 8) {
            val importedQuestionIds = safeQuestions.map { it.id }.toSet()
            importedQuestionIds.forEach { questionId ->
                dao.deleteContentBlocks(questionId)
                dao.deleteQuestionParts(questionId)
            }
            val validQuestionIds = (dao.getAllQuestions() + safeQuestions).map { it.id }.toSet()
            dao.upsertContentBlocks(importedContentBlocks.filter { it.questionId in validQuestionIds }.distinctBy { it.questionId to it.position })
            dao.upsertQuestionParts(importedQuestionParts.filter { it.questionId in validQuestionIds }.distinctBy { it.questionId to it.position })
            val validCollectionIdsForSessions = dao.getAllCollections().map { it.id }.toSet()
            dao.upsertStudySessionPresets(importedSessionPresets.map { preset ->
                preset.copy(collectionId = preset.collectionId.takeIf { it.isBlank() || it in validCollectionIdsForSessions }.orEmpty())
            })
        }
        if (schemaVersion >= 7 && importedTags.isNotEmpty()) dao.upsertTags(importedTags)
        if (schemaVersion >= 6 && savedPresets.isNotEmpty()) {
            val validCollectionIds = (dao.getAllCollections() + collections).map { it.id }.toSet()
            val existingPresets = dao.getAllSavedFilterPresets().associateBy { it.id }
            val safePresets = savedPresets.map { preset ->
                val existing = existingPresets[preset.id]
                preset.copy(
                    collectionId = preset.collectionId?.takeIf { it in validCollectionIds },
                    sourceTitle = if (schemaVersion < 14) existing?.sourceTitle.orEmpty() else preset.sourceTitle,
                    sourceDetail = if (schemaVersion < 14) existing?.sourceDetail.orEmpty() else preset.sourceDetail,
                    createdAfter = if (schemaVersion < 15) existing?.createdAfter.orEmpty() else preset.createdAfter,
                    createdBefore = if (schemaVersion < 15) existing?.createdBefore.orEmpty() else preset.createdBefore,
                    updatedAfter = if (schemaVersion < 15) existing?.updatedAfter.orEmpty() else preset.updatedAfter,
                    updatedBefore = if (schemaVersion < 15) existing?.updatedBefore.orEmpty() else preset.updatedBefore
                )
            }
            dao.upsertSavedFilterPresets(safePresets)
        }
        dao.insertLinks(links)
        if (reviewLogs.isNotEmpty()) dao.insertReviewLogs(reviewLogs)
        if (importedExamAttempts.isNotEmpty()) dao.upsertExamAttempts(importedExamAttempts)
        importedQuestionVersions.forEach { incomingVersion ->
            val sameId = dao.getQuestionVersionById(incomingVersion.id)
            val sameNumber = dao.getQuestionVersionByNumber(incomingVersion.questionId, incomingVersion.versionNumber)
            val sameIdMatches = sameId != null && sameId.questionId == incomingVersion.questionId && sameId.versionNumber == incomingVersion.versionNumber
            val safeVersion = if (sameId == null && sameNumber == null) incomingVersion else if (sameIdMatches) incomingVersion else incomingVersion.copy(
                id = UUID.randomUUID().toString(), versionNumber = dao.nextQuestionVersion(incomingVersion.questionId)
            )
            dao.upsertQuestionVersion(safeVersion)
        }
        pendingVersionSnapshots.forEach { (questionId, snapshotJson) ->
            dao.insertQuestionVersion(QuestionVersionEntity(
                id = UUID.randomUUID().toString(), questionId = questionId,
                versionNumber = dao.nextQuestionVersion(questionId), savedAt = System.currentTimeMillis(), snapshotJson = snapshotJson
            ))
        }
        if (importedSettings != null) dao.upsertSettings(importedSettings)
    }
}
