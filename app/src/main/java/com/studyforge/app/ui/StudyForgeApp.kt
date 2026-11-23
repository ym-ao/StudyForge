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
import androidx.compose.ui.unit.dp
import androidx.room.withTransaction
import com.studyforge.app.data.CollectionEntity
import com.studyforge.app.data.QuestionCollectionCrossRef
import com.studyforge.app.data.QuestionEntity
import com.studyforge.app.data.LibraryFilterCriteria
import com.studyforge.app.data.QuestionLibraryFilter
import com.studyforge.app.data.SavedFilterPresetEntity
import com.studyforge.app.data.TagEntity
import com.studyforge.app.data.QuestionContentBlockEntity
import com.studyforge.app.data.QuestionPartEntity
import com.studyforge.app.data.QuestionAuthoringRules
import com.studyforge.app.data.StudySessionPresetEntity
import com.studyforge.app.data.DerivationRules
import com.studyforge.app.data.TagText
import com.studyforge.app.data.ReviewLogEntity
import com.studyforge.app.data.StudySettingsEntity
import com.studyforge.app.data.recordGradedReview
import com.studyforge.app.data.StudyDatabase
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyForgeApp(database: StudyDatabase) {
    val dao = database.dao()
    val appContext = LocalContext.current
    val questions by dao.observeQuestions().collectAsState(initial = emptyList())
    val collections by dao.observeCollections().collectAsState(initial = emptyList())
    val reviewLogs by dao.observeReviewLogs().collectAsState(initial = emptyList())
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
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
            collections = collections,
            onBack = { editorOpen = false; editorId = null },
            onSave = { question, selectedCollections, contentBlocks, questionParts ->
                scope.launch {
                    database.withTransaction {
                        dao.upsertQuestion(question)
                        dao.deleteContentBlocks(question.id)
                        dao.deleteQuestionParts(question.id)
                        val normalizedBlocks = QuestionAuthoringRules.normalizeBlocks(question.id, contentBlocks)
                        val normalizedParts = QuestionAuthoringRules.normalizeParts(question.id, questionParts)
                        if (normalizedBlocks.isNotEmpty()) dao.upsertContentBlocks(normalizedBlocks)
                        if (normalizedParts.isNotEmpty()) dao.upsertQuestionParts(normalizedParts)
                        dao.clearQuestionCollections(question.id)
                        if (selectedCollections.isNotEmpty()) {
                            dao.insertLinks(selectedCollections.map { QuestionCollectionCrossRef(question.id, it) })
                        }
                    }
                    editorOpen = false
                    editorId = null
                    snackbar.showSnackbar("Question saved to your library")
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
                    onDelete = { collection -> scope.launch { dao.deleteCollection(collection.id) } }
                )
                3 -> StudyScreen(
                    questions = questions,
                    reviewLogs = reviewLogs,
                    contentBlocks = allContentBlocks,
                    questionParts = allQuestionParts,
                    sessionPresets = studySessionPresets,
                    settings = studySettings,
                    onSaveSessionPreset = { preset -> scope.launch { dao.upsertStudySessionPreset(preset); snackbar.showSnackbar("Study session saved") } },
                    onDeleteSessionPreset = { preset -> scope.launch { dao.deleteStudySessionPreset(preset.id); snackbar.showSnackbar("Study session deleted") } },
                    onReview = { question, rating, wasCorrect, mistakeCategory, reviewMode ->
                        scope.launch { database.recordGradedReview(question.id, rating, wasCorrect, mistakeCategory.orEmpty(), reviewMode, settings = studySettings) }
                    }
                )
                4 -> BackupScreen(
                    onExport = { uri -> scope.launch { exportBackup(appContext, database, uri); snackbar.showSnackbar("Backup exported") } },
                    onImport = { uri -> scope.launch {
                        try {
                            importBackup(appContext, database, uri)
                            snackbar.showSnackbar("Backup imported")
                        } catch (e: Exception) {
                            snackbar.showSnackbar("Import failed: ${e.message ?: "invalid backup"}")
                        }
                    } }
                )
                5 -> ProgressScreen(questions = questions, reviewLogs = reviewLogs)
                6 -> SettingsScreen(settings = studySettings, onSave = { updated ->
                    scope.launch { dao.upsertSettings(updated); snackbar.showSnackbar("Study settings saved") }
                })
            }
        }
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
                Text("Version 2.1.8 • StudyForge learning system", fontWeight = FontWeight.SemiBold)
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
    onEdit: (QuestionEntity) -> Unit,
    onDelete: (QuestionEntity) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf("All") }
    var statusFilter by rememberSaveable { mutableStateOf("All") }
    var subjectFilter by rememberSaveable { mutableStateOf("") }
    var chapterFilter by rememberSaveable { mutableStateOf("") }
    var topicFilter by rememberSaveable { mutableStateOf("") }
    var tagFilter by rememberSaveable { mutableStateOf("") }
    var difficultyFilter by rememberSaveable { mutableStateOf("All") }
    var examFilter by rememberSaveable { mutableStateOf("") }
    var performanceFilter by rememberSaveable { mutableStateOf("All") }
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

    val collectionId = selectedCollectionId.takeIf { it.isNotBlank() }
    val selectedCollectionLabel = collections.firstOrNull { it.id == collectionId }?.name ?: "All collections"
    val collectionQuestionIds = remember(links, collectionId) {
        if (collectionId == null) emptySet() else links.filter { it.collectionId == collectionId }.map { it.questionId }.toSet()
    }
    val criteria = LibraryFilterCriteria(
        query = search, type = typeFilter, status = statusFilter, subject = subjectFilter,
        chapter = chapterFilter, topic = topicFilter, tag = tagFilter, collectionId = collectionId,
        difficulty = difficultyFilter, exam = examFilter, performance = performanceFilter
    )
    val filtered = questions.filter { q -> QuestionLibraryFilter.matches(q, criteria, collectionQuestionIds) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search questions") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showTagManager = true }) { Text("Manage tags (${tags.size})") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Text("Saved filters (${savedFilterPresets.size})", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { presetName = ""; showSavePreset = true }) { Text("Save current") }
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
                        showFilters = true
                    }, label = { Text(preset.name) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            listOf("All", "Flashcard", "MCQ").forEach { type -> FilterChip(selected = typeFilter == type, onClick = { typeFilter = type }, label = { Text(type) }) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Draft", "Verified").forEach { status -> FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status) }) }
                }
                OutlinedTextField(subjectFilter, { subjectFilter = it }, label = { Text("Subject filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(chapterFilter, { chapterFilter = it }, label = { Text("Chapter filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(topicFilter, { topicFilter = it }, label = { Text("Topic filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(tagFilter, { tagFilter = it }, label = { Text("Tag filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(examFilter, { examFilter = it }, label = { Text("Exam / syllabus filter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Difficulty")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("All", "Easy", "Medium", "Hard", "Unrated").forEach { value -> FilterChip(selected = difficultyFilter == value, onClick = { difficultyFilter = value }, label = { Text(value) }) }
                }
                Text("Performance")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("All", "Unattempted", "Weak", "Strong").forEach { value -> FilterChip(selected = performanceFilter == value, onClick = { performanceFilter = value }, label = { Text(value) }) }
                }
                TextButton(onClick = { subjectFilter = ""; chapterFilter = ""; topicFilter = ""; tagFilter = ""; examFilter = ""; difficultyFilter = "All"; performanceFilter = "All"; statusFilter = "All"; selectedCollectionId = ""; typeFilter = "All"; search = "" }) { Text("Clear all filters") }
            }
        }
        Text("${filtered.size} of ${questions.size} questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (filtered.isEmpty()) {
            EmptyState("No questions found", if (questions.isEmpty()) "Tap + to create your first question." else "Try a different search or filter.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)) {
                items(filtered, key = { it.id }) { q ->
                    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable { onEdit(q) }) {
                        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(q.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text(q.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { onEdit(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = { onDelete(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, "Delete") }
                            }
                            RichContentText(q.prompt, style = MaterialTheme.typography.titleMedium)
                            val meta = listOf(q.subject, q.chapter, q.topic, q.exam).filter { it.isNotBlank() }.joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Difficulty: ${q.difficulty}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            if (q.tagsCsv.isNotBlank()) Text("Tags: ${q.tagsCsv}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            if (q.latex.isNotBlank()) LatexFormulaView(q.latex, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
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
private fun FormattingToolbar(onInsert: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        listOf("Bold" to "**bold text**", "Italic" to "*italic text*", "Heading" to "## Heading", "Bullet" to "- list item", "Code" to "`code`").forEach { (label, token) ->
            FilterChip(selected = false, onClick = { onInsert(token) }, label = { Text(label) })
        }
    }
}

private fun appendEditorToken(current: String, token: String): String =
    if (current.isBlank()) token else current.trimEnd() + "\n" + token

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
    collections: List<CollectionEntity>,
    onBack: () -> Unit,
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
    var difficulty by remember(initial?.id) { mutableStateOf(initial?.difficulty ?: "Unrated") }
    var status by remember(initial?.id) { mutableStateOf(initial?.status ?: "Draft") }
    var selectedCollections by remember(initial?.id) { mutableStateOf(initialCollectionIds.toSet()) }
    val contentBlocks = remember(initial?.id) { mutableStateListOf<QuestionContentBlockEntity>().apply { addAll(initialContentBlocks) } }
    val context = LocalContext.current
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
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "New question" else "Edit question") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { TextButton(onClick = {
                    if (prompt.isBlank() || answer.isBlank()) error = "Add both a question and an answer before saving."
                    else if (type == "MCQ" && options.lines().map { it.trim() }.filter { it.isNotBlank() }.size < 2) error = "An MCQ needs at least two options."
                    else if (type == "MCQ" && options.lines().map { it.trim() }.filter { it.isNotBlank() }.none { it == answer.trim() }) error = "The correct answer must exactly match one of the options."
                    else if (QuestionAuthoringRules.validateParts(questionParts) != null) error = QuestionAuthoringRules.validateParts(questionParts)
                    else if (QuestionAuthoringRules.validateBlocks(contentBlocks) != null) error = QuestionAuthoringRules.validateBlocks(contentBlocks)
                    else {
                        val now = System.currentTimeMillis()
                        val parsed = options.lines().map { it.trim() }.filter { it.isNotBlank() }
                        val entity = QuestionEntity(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            prompt = prompt.trim(), answer = answer.trim(), explanation = explanation.trim(),
                            subject = subject.trim(), chapter = chapter.trim(), topic = topic.trim(), type = type,
                            optionsJson = JSONArray(parsed).toString(), latex = latex.trim(), tagsCsv = tags.trim(), status = status,
                            createdAt = initial?.createdAt ?: now, updatedAt = now,
                            reviewCount = initial?.reviewCount ?: 0, lastReviewedAt = initial?.lastReviewedAt,
                            dueAt = initial?.dueAt, intervalDays = initial?.intervalDays ?: 0.0,
                            easeFactor = initial?.easeFactor ?: 2.5, repetitions = initial?.repetitions ?: 0,
                            lapseCount = initial?.lapseCount ?: 0, learningState = initial?.learningState ?: "New",
                            correctCount = initial?.correctCount ?: 0, incorrectCount = initial?.incorrectCount ?: 0,
                            fsrsStability = initial?.fsrsStability ?: 0.0, fsrsDifficulty = initial?.fsrsDifficulty ?: 0.0,
                            difficulty = difficulty, exam = exam.trim()
                        )
                        onSave(entity, selectedCollections.toList(), contentBlocks.toList(), questionParts.toList())
                    }
                }) { Text("Save", fontWeight = FontWeight.Bold) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            Text("Formatting shortcuts • apply to the field below each toolbar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FormattingToolbar { token -> prompt = appendEditorToken(prompt, token) }
            OutlinedTextField(prompt, { prompt = it; error = null }, label = { Text("Question / Front (supports basic Markdown)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            if (prompt.isNotBlank()) Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Question preview", style = MaterialTheme.typography.labelSmall); RichContentText(prompt) } }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Question type", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.AssistChip(onClick = { typeExpanded = true }, label = { Text(type) })
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("Flashcard", "MCQ").forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false }) }
                }
            }
            if (type == "MCQ") OutlinedTextField(options, { options = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Options (one per line)") }, minLines = 4, supportingText = { Text("The answer field below must match the correct option exactly.") })
            FormattingToolbar { token -> answer = appendEditorToken(answer, token) }
            OutlinedTextField(answer, { answer = it; error = null }, label = { Text(if (type == "MCQ") "Correct option (exact text)" else "Answer / Back") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            if (answer.isNotBlank() && type != "MCQ") Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Answer preview", style = MaterialTheme.typography.labelSmall); RichContentText(answer) } }
            FormattingToolbar { token -> explanation = appendEditorToken(explanation, token) }
            OutlinedTextField(explanation, { explanation = it }, label = { Text("Explanation (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            if (explanation.isNotBlank()) Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(10.dp)) { Text("Explanation preview", style = MaterialTheme.typography.labelSmall); RichContentText(explanation) } }
            HorizontalDivider()
            Text("Structured content blocks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Add ordered text, heading, list, or LaTeX blocks. They are saved with this question and shown during study.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { blockKindExpanded = true }) { Text("Add: $newBlockKind") }
                    DropdownMenu(expanded = blockKindExpanded, onDismissRequest = { blockKindExpanded = false }) {
                        listOf("TEXT", "HEADING", "LIST", "LATEX", "IMAGE", "TABLE", "GRAPH", "DERIVATION").forEach { kind ->
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
                            }, enabled = blockIndex > 0) { Text("↑") }
                            TextButton(onClick = {
                                if (blockIndex < contentBlocks.lastIndex) {
                                    val next = contentBlocks[blockIndex + 1]
                                    contentBlocks[blockIndex + 1] = block.copy(position = blockIndex + 1)
                                    contentBlocks[blockIndex] = next.copy(position = blockIndex)
                                }
                            }, enabled = blockIndex < contentBlocks.lastIndex) { Text("↓") }
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
                                    "TABLE" -> "Table: separate columns with | and rows with new lines"
                                    "GRAPH" -> "Graph data: one x,y pair per line (example: 0,2)"
                                    "DERIVATION" -> "One step per line: title | LaTeX equation | explanation"
                                    else -> "Block content"
                                }
                                OutlinedTextField(
                                    value = block.text,
                                    onValueChange = { value -> contentBlocks[blockIndex] = block.copy(text = value.take(20000)) },
                                    label = { Text(hint) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = if (block.kind == "LATEX") 2 else 3
                                )
                                when (block.kind.uppercase()) {
                                    "LATEX" -> if (block.text.isNotBlank()) LatexFormulaView(block.text)
                                    "HEADING" -> if (block.text.isNotBlank()) Text(block.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            Text("Create separately scored parts. Weighted partial-credit exam grading is not yet connected to the exam engine.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        OutlinedTextField(part.prompt, { questionParts[partIndex] = part.copy(prompt = it.take(10000)) }, label = { Text("Part prompt") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        OutlinedTextField(part.answer, { questionParts[partIndex] = part.copy(answer = it.take(10000)) }, label = { Text("Part answer") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        OutlinedTextField(part.explanation, { questionParts[partIndex] = part.copy(explanation = it.take(10000)) }, label = { Text("Part explanation") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status: ", fontWeight = FontWeight.SemiBold)
                listOf("Draft", "Verified").forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) }, modifier = Modifier.padding(end = 6.dp)) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BackupScreen(onExport: (Uri) -> Unit, onImport: (Uri) -> Unit) {
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json"), onResult = { it?.let(onExport) })
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), onResult = { it?.let(onImport) })
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Backup & restore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Move your library between devices or keep a safe copy. Version 7 backups include questions, collections, review history, FSRS memory state, learning-step progress, study settings, question difficulty, exam labels, saved filter presets, and the tag catalogue. Versions 1–6 remain importable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ImportExport, null, tint = MaterialTheme.colorScheme.primary)
                Text("Export JSON backup", fontWeight = FontWeight.SemiBold)
                Text("Creates one portable .json file.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { exportLauncher.launch("studyforge-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("Export library") }
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                Text("Import JSON backup", fontWeight = FontWeight.SemiBold)
                Text("Imported items are merged by ID. Existing items with matching IDs are updated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("Choose backup file") }
            }
        }
        Text("Keep a backup somewhere outside your phone as well. Uninstalling the app may remove its local database.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

private suspend fun exportBackup(context: Context, database: StudyDatabase, uri: Uri) {
    val dao = database.dao()
    val questions = dao.getAllQuestions()
    val collections = dao.getAllCollections()
    val links = dao.getAllLinks()
    val reviewLogs = dao.getAllReviewLogs()
    val settings = dao.getSettings() ?: StudySettingsEntity.DEFAULT
    val savedPresets = dao.getAllSavedFilterPresets()
    val tags = dao.getAllTags()
    val contentBlocks = dao.getAllContentBlocks()
    val questionParts = dao.getAllQuestionParts()
    val sessionPresets = dao.getAllStudySessionPresets()
    val root = JSONObject().put("app", "StudyForge").put("schemaVersion", 9).put("exportedAt", System.currentTimeMillis())
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
            .put("difficulty", q.difficulty).put("exam", q.exam).put("learningStepIndex", q.learningStepIndex)) }
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
            .put("questionLimit", preset.questionLimit).put("createdAt", preset.createdAt).put("updatedAt", preset.updatedAt)) }
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
    require(schemaVersion in 1..9) { "Unsupported backup version" }
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
        val rawMedia = o.optString("mediaBase64").take(8_000_000)
        val encoded = rawMedia.takeIf { it.matches(Regex("[A-Za-z0-9+/]*={0,2}")) }.orEmpty()
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
            questionLimit = o.optInt("questionLimit", 20).coerceIn(1, 500),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
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
                learningStepIndex = if (schemaVersion < 5) existing.learningStepIndex else imported.learningStepIndex
            )
        }
        dao.upsertQuestions(safeQuestions)
        dao.upsertCollections(collections)
        if (schemaVersion >= 8) {
            val validQuestionIds = (dao.getAllQuestions() + safeQuestions).map { it.id }.toSet()
            dao.upsertContentBlocks(importedContentBlocks.filter { it.questionId in validQuestionIds }.distinctBy { it.questionId to it.position })
            dao.upsertQuestionParts(importedQuestionParts.filter { it.questionId in validQuestionIds }.distinctBy { it.questionId to it.position })
            dao.upsertStudySessionPresets(importedSessionPresets)
        }
        if (schemaVersion >= 7 && importedTags.isNotEmpty()) dao.upsertTags(importedTags)
        if (schemaVersion >= 6 && savedPresets.isNotEmpty()) {
            val validCollectionIds = (dao.getAllCollections() + collections).map { it.id }.toSet()
            val safePresets = savedPresets.map { preset ->
                preset.copy(collectionId = preset.collectionId?.takeIf { it in validCollectionIds })
            }
            dao.upsertSavedFilterPresets(safePresets)
        }
        dao.insertLinks(links)
        if (reviewLogs.isNotEmpty()) dao.insertReviewLogs(reviewLogs)
        if (importedSettings != null) dao.upsertSettings(importedSettings)
    }
}
