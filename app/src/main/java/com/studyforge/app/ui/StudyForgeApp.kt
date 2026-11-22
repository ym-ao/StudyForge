package com.studyforge.app.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editorCollectionIds by remember { mutableStateOf(emptyList<String>()) }

    if (editorOpen) {
        val current = questions.firstOrNull { it.id == editorId }
        QuestionEditor(
            initial = current,
            initialCollectionIds = editorCollectionIds,
            collections = collections,
            onBack = { editorOpen = false; editorId = null },
            onSave = { question, selectedCollections ->
                scope.launch {
                    dao.upsertQuestion(question)
                    database.withTransaction {
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
        return
    }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Library", Icons.Default.Book),
        NavItem("Collections", Icons.Default.Folder),
        NavItem("Study", Icons.Default.School),
        NavItem("Backup", Icons.Default.ImportExport)
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
                FloatingActionButton(onClick = { editorCollectionIds = emptyList(); editorId = null; editorOpen = true }) {
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
                    onAdd = { editorCollectionIds = emptyList(); editorId = null; editorOpen = true },
                    onLibrary = { selectedTab = 1 },
                    onStudy = { selectedTab = 3 }
                )
                1 -> LibraryScreen(
                    questions = questions,
                    onEdit = { question -> scope.launch { editorCollectionIds = dao.getCollectionIds(question.id); editorId = question.id; editorOpen = true } },
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
                    onReview = { question -> scope.launch { dao.recordReview(question.id, System.currentTimeMillis()) } }
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
            }
        }
    }
}

@Composable
private fun HomeScreen(
    questionCount: Int,
    collectionCount: Int,
    reviewCount: Int,
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
            StatCard("Collections", collectionCount.toString(), Modifier.weight(1f))
            StatCard("Reviews", reviewCount.toString(), Modifier.weight(1f))
        }
        Text("Quick actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ActionCard("Question library", "Search, edit, and organize your questions.", Icons.Default.Book, onLibrary)
        ActionCard("Start studying", "Review flashcards and answer MCQs.", Icons.Default.School, onStudy)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Version 1.0", fontWeight = FontWeight.SemiBold)
                Text("Your library is stored locally on this device. Export a JSON backup regularly so you can move your work to another device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onEdit: (QuestionEntity) -> Unit,
    onDelete: (QuestionEntity) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val filtered = questions.filter { q ->
        val matchesText = search.isBlank() || listOf(q.prompt, q.subject, q.chapter, q.topic, q.tagsCsv).any { it.contains(search, true) }
        val matchesType = filter == "All" || q.type == filter
        matchesText && matchesType
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search questions") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 10.dp)) {
            listOf("All", "Flashcard", "MCQ").forEach { type ->
                FilterChip(selected = filter == type, onClick = { filter = type }, label = { Text(type) })
            }
        }
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
                                IconButton(onClick = { onEdit(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = { onDelete(q) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, "Delete") }
                            }
                            Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 3)
                            val meta = listOf(q.subject, q.chapter, q.topic).filter { it.isNotBlank() }.joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (q.latex.isNotBlank()) Text("Equation source: ${q.latex}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 2)
                        }
                    }
                }
            }
        }
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

@Composable
private fun StudyScreen(questions: List<QuestionEntity>, onReview: (QuestionEntity) -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var checked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(questions.size) { if (index >= questions.size) index = 0 }
    if (questions.isEmpty()) {
        EmptyState("Nothing to study yet", "Add flashcards or MCQs in your library, then come back here.")
        return
    }
    val q = questions[index.coerceIn(0, questions.lastIndex)]
    val options = remember(q.id, q.optionsJson) { runCatching { JSONArray(q.optionsJson).let { arr -> (0 until arr.length()).map { arr.optString(it) } } }.getOrDefault(emptyList()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Study session", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${index + 1} / ${questions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(q.type.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(q.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (q.subject.isNotBlank() || q.chapter.isNotBlank()) Text(listOf(q.subject, q.chapter).filter { it.isNotBlank() }.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (q.latex.isNotBlank()) {
                    HorizontalDivider()
                    Text("Equation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    Text(q.latex, style = MaterialTheme.typography.bodyLarge)
                }
                if (q.type == "MCQ" && options.isNotEmpty()) {
                    options.forEachIndexed { optionIndex, option ->
                        val prefix = ('A'.code + optionIndex).toChar()
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !checked) { selectedAnswer = option },
                            colors = CardDefaults.cardColors(containerColor = if (selectedAnswer == option) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$prefix.", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(10.dp))
                                Text(option, modifier = Modifier.weight(1f))
                                if (checked && option == q.answer) Text("Correct", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (!checked) Button(onClick = { checked = true }, enabled = selectedAnswer != null, modifier = Modifier.fillMaxWidth()) { Text("Check answer") }
                    if (checked) {
                        val right = selectedAnswer == q.answer
                        Text(if (right) "Correct" else "Not quite — review the answer below.", fontWeight = FontWeight.Bold, color = if (right) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        Text("Answer: ${q.answer}", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    if (!revealed) Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) { Text("Reveal answer") }
                    else {
                        HorizontalDivider()
                        Text("Answer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(q.answer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                if ((revealed || checked) && q.explanation.isNotBlank()) {
                    HorizontalDivider()
                    Text("Explanation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(q.explanation)
                }
            }
        }
        if (revealed || checked) {
            Text("How well did you know this?", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Again", "Good", "Easy").forEach { grade ->
                    OutlinedButton(onClick = {
                        onReview(q)
                        if (index < questions.lastIndex) index++ else index = 0
                        revealed = false; checked = false; selectedAnswer = null
                    }, modifier = Modifier.weight(1f)) { Text(grade) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { index = if (index == 0) questions.lastIndex else index - 1; revealed = false; checked = false; selectedAnswer = null }, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { index = (index + 1) % questions.size; revealed = false; checked = false; selectedAnswer = null }, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        Text("Version 1.0 records review counts only. Adaptive spaced repetition and mistake analytics are planned for Version 2.0.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditor(
    initial: QuestionEntity?,
    initialCollectionIds: List<String>,
    collections: List<CollectionEntity>,
    onBack: () -> Unit,
    onSave: (QuestionEntity, List<String>) -> Unit
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
    var status by remember(initial?.id) { mutableStateOf(initial?.status ?: "Draft") }
    var selectedCollections by remember(initial?.id) { mutableStateOf(initialCollectionIds.toSet()) }
    var typeExpanded by remember { mutableStateOf(false) }
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
                    else {
                        val now = System.currentTimeMillis()
                        val parsed = options.lines().map { it.trim() }.filter { it.isNotBlank() }
                        val entity = QuestionEntity(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            prompt = prompt.trim(), answer = answer.trim(), explanation = explanation.trim(),
                            subject = subject.trim(), chapter = chapter.trim(), topic = topic.trim(), type = type,
                            optionsJson = JSONArray(parsed).toString(), latex = latex.trim(), tagsCsv = tags.trim(), status = status,
                            createdAt = initial?.createdAt ?: now, updatedAt = now,
                            reviewCount = initial?.reviewCount ?: 0, lastReviewedAt = initial?.lastReviewedAt
                        )
                        onSave(entity, selectedCollections.toList())
                    }
                }) { Text("Save", fontWeight = FontWeight.Bold) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            OutlinedTextField(prompt, { prompt = it; error = null }, label = { Text("Question / Front") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Question type", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.AssistChip(onClick = { typeExpanded = true }, label = { Text(type) })
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("Flashcard", "MCQ").forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false }) }
                }
            }
            if (type == "MCQ") OutlinedTextField(options, { options = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Options (one per line)") }, minLines = 4, supportingText = { Text("The answer field below must match the correct option exactly.") })
            OutlinedTextField(answer, { answer = it; error = null }, label = { Text(if (type == "MCQ") "Correct option (exact text)" else "Answer / Back") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(explanation, { explanation = it }, label = { Text("Explanation (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            HorizontalDivider()
            Text("Organization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(subject, { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(chapter, { chapter = it }, label = { Text("Chapter") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(topic, { topic = it }, label = { Text("Topic / Subtopic") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(tags, { tags = it }, label = { Text("Tags (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            HorizontalDivider()
            Text("Equation (LaTeX source)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(latex, { latex = it }, label = { Text("Example: v = \\frac{\\Delta x}{\\Delta t}") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            if (latex.isNotBlank()) Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("LaTeX source preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(latex, style = MaterialTheme.typography.bodyLarge)
                    Text("The source is saved exactly. Rendered equation preview is planned for a later 1.x update.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text("Move your library between devices or keep a safe copy. The backup includes questions, collections, links, and review counts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val root = JSONObject().put("app", "StudyForge").put("schemaVersion", 1).put("exportedAt", System.currentTimeMillis())
    root.put("questions", JSONArray().apply {
        questions.forEach { q -> put(JSONObject()
            .put("id", q.id).put("prompt", q.prompt).put("answer", q.answer).put("explanation", q.explanation)
            .put("subject", q.subject).put("chapter", q.chapter).put("topic", q.topic).put("type", q.type)
            .put("optionsJson", q.optionsJson).put("latex", q.latex).put("tagsCsv", q.tagsCsv).put("status", q.status)
            .put("createdAt", q.createdAt).put("updatedAt", q.updatedAt).put("reviewCount", q.reviewCount)
            .put("lastReviewedAt", q.lastReviewedAt ?: JSONObject.NULL)) }
    })
    root.put("collections", JSONArray().apply {
        collections.forEach { c -> put(JSONObject().put("id", c.id).put("name", c.name).put("description", c.description).put("createdAt", c.createdAt)) }
    })
    root.put("links", JSONArray().apply { links.forEach { put(JSONObject().put("questionId", it.questionId).put("collectionId", it.collectionId)) } })
    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
        ?: error("Could not open the selected file for writing")
}

private suspend fun importBackup(context: Context, database: StudyDatabase, uri: Uri) {
    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        ?: error("Could not read the selected file")
    val root = JSONObject(raw)
    require(root.optString("app") == "StudyForge") { "This file is not a StudyForge backup" }
    require(root.optInt("schemaVersion", -1) == 1) { "Unsupported backup version" }
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
            reviewCount = o.optInt("reviewCount", 0), lastReviewedAt = if (o.isNull("lastReviewedAt")) null else o.optLong("lastReviewedAt")
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
    database.withTransaction {
        val dao = database.dao()
        dao.upsertQuestions(questions)
        dao.upsertCollections(collections)
        dao.insertLinks(links)
    }
}
