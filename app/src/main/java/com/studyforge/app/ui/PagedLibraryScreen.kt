package com.studyforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.studyforge.app.data.LibraryFilterCriteria
import com.studyforge.app.data.CollectionEntity
import com.studyforge.app.data.QuestionLibraryRow
import com.studyforge.app.data.SavedFilterPresetEntity
import com.studyforge.app.data.TagEntity
import com.studyforge.app.viewmodel.LibraryViewModel
import kotlinx.coroutines.delay

/** Production Library destination. Only lightweight Room projection rows are rendered. */
@Composable
fun PagedLibraryScreen(
    viewModel: LibraryViewModel,
    savedFilterPresets: List<SavedFilterPresetEntity>,
    collections: List<CollectionEntity>,
    tags: List<TagEntity>,
    onSavePreset: (String, LibraryFilterCriteria) -> Unit,
    onDeletePreset: (SavedFilterPresetEntity) -> Unit,
    onAddTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (TagEntity) -> Unit,
    onOpenQuestion: (String) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onScheduleQuestion: (String, String) -> Unit,
    onBulkUpdate: (Set<String>, String?, String?) -> Unit,
    onBulkTag: (Set<String>, String, Boolean) -> Unit,
    onBulkCollection: (Set<String>, String, Boolean) -> Unit,
    onAddQuestion: () -> Unit
) {
    val criteria by viewModel.criteria.collectAsState()
    val rows = viewModel.rows.collectAsLazyPagingItems()
    var search by rememberSaveable { mutableStateOf(criteria.query) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var presetName by rememberSaveable { mutableStateOf("") }
    var showPresetEditor by rememberSaveable { mutableStateOf(false) }
    var showManagePresets by remember { mutableStateOf(false) }
    var presetPendingDelete by remember { mutableStateOf<SavedFilterPresetEntity?>(null) }
    var showTagManager by remember { mutableStateOf(false) }
    var newTagName by rememberSaveable { mutableStateOf("") }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var editedTagName by rememberSaveable { mutableStateOf("") }
    var deletingTag by remember { mutableStateOf<TagEntity?>(null) }
    var tagUsageCounts by remember { mutableStateOf(emptyMap<String, Int>()) }
    var totalCount by remember { mutableStateOf<Int?>(null) }
    var collectionMenu by remember { mutableStateOf(false) }
    var showBulkTagDialog by remember { mutableStateOf(false) }
    var bulkTagText by rememberSaveable { mutableStateOf("") }
    var bulkTagAdd by remember { mutableStateOf(true) }
    var showBulkCollectionDialog by remember { mutableStateOf(false) }
    var bulkCollectionAdd by remember { mutableStateOf(true) }
    var bulkCollectionId by rememberSaveable { mutableStateOf("") }
    var presetCounts by remember { mutableStateOf(emptyMap<String, Int>()) }

    LaunchedEffect(search) {
        delay(180)
        viewModel.updateQuery(search.trim())
    }
    LaunchedEffect(criteria) { totalCount = viewModel.countMatches(criteria) }
    LaunchedEffect(savedFilterPresets) {
        presetCounts = savedFilterPresets.associate { preset -> preset.id to viewModel.countMatches(preset.toCriteria()) }
    }
    LaunchedEffect(tags) {
        tagUsageCounts = tags.associate { tag -> tag.id to viewModel.countTagUsage(tag.name) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search questions") },
            singleLine = true
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${totalCount?.toString() ?: "…"} matching questions", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { showTagManager = true }) { Text("Manage tags (${tags.size})") }
            TextButton(onClick = onAddQuestion) { Text("Add question") }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("All", "Flashcard", "MCQ", "True/False", "Numerical", "Cloze").forEach { type ->
                FilterChip(selected = criteria.type == type, onClick = { viewModel.updateCriteria(criteria.copy(type = type)) }, label = { Text(type) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(selected = showFilters, onClick = { showFilters = !showFilters }, label = { Text(if (showFilters) "Hide filters" else "More filters") })
            TextButton(onClick = { selectionMode = !selectionMode; selectedIds = emptySet() }) { Text(if (selectionMode) "Done selecting" else "Select multiple") }
            TextButton(onClick = { presetName = ""; showPresetEditor = true }) { Text("Save filter") }
        }
        if (showFilters) {
            Column(modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Draft", "Unverified", "Verified", "Incorrect", "Archived").forEach { status ->
                        FilterChip(selected = criteria.status == status, onClick = { viewModel.updateCriteria(criteria.copy(status = status)) }, label = { Text(status) })
                    }
                }
                OutlinedTextField(criteria.subject, { viewModel.updateCriteria(criteria.copy(subject = it)) }, label = { Text("Subject") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.chapter, { viewModel.updateCriteria(criteria.copy(chapter = it)) }, label = { Text("Chapter") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.topic, { viewModel.updateCriteria(criteria.copy(topic = it)) }, label = { Text("Topic") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.tag, { viewModel.updateCriteria(criteria.copy(tag = it)) }, label = { Text("Tag") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Collection", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { collectionMenu = true }) { Text(collections.firstOrNull { it.id == criteria.collectionId }?.name ?: "All collections") }
                        DropdownMenu(expanded = collectionMenu, onDismissRequest = { collectionMenu = false }) {
                            DropdownMenuItem(text = { Text("All collections") }, onClick = { viewModel.updateCriteria(criteria.copy(collectionId = null)); collectionMenu = false })
                            collections.forEach { collection -> DropdownMenuItem(text = { Text(collection.name) }, onClick = { viewModel.updateCriteria(criteria.copy(collectionId = collection.id)); collectionMenu = false }) }
                        }
                    }
                }
                OutlinedTextField(criteria.exam, { viewModel.updateCriteria(criteria.copy(exam = it)) }, label = { Text("Exam") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.sourceTitle, { viewModel.updateCriteria(criteria.copy(sourceTitle = it)) }, label = { Text("Source title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.sourceDetail, { viewModel.updateCriteria(criteria.copy(sourceDetail = it)) }, label = { Text("Source detail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.createdAfter, { viewModel.updateCriteria(criteria.copy(createdAfter = it)) }, label = { Text("Created on/after (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.createdBefore, { viewModel.updateCriteria(criteria.copy(createdBefore = it)) }, label = { Text("Created on/before (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.updatedAfter, { viewModel.updateCriteria(criteria.copy(updatedAfter = it)) }, label = { Text("Updated on/after (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(criteria.updatedBefore, { viewModel.updateCriteria(criteria.copy(updatedBefore = it)) }, label = { Text("Updated on/before (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Easy", "Medium", "Hard", "Unrated").forEach { difficulty ->
                        FilterChip(selected = criteria.difficulty == difficulty, onClick = { viewModel.updateCriteria(criteria.copy(difficulty = difficulty)) }, label = { Text(difficulty) })
                    }
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Unattempted", "Weak", "Strong").forEach { performance ->
                        FilterChip(selected = criteria.performance == performance, onClick = { viewModel.updateCriteria(criteria.copy(performance = performance)) }, label = { Text(performance) })
                    }
                }
            }
        }
        if (savedFilterPresets.isNotEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                savedFilterPresets.forEach { preset ->
                    FilterChip(selected = false, onClick = {
                        val restored = preset.toCriteria()
                        search = restored.query
                        viewModel.updateCriteria(restored)
                    }, label = { Text("${preset.name} · ${presetCounts[preset.id] ?: "…"}") })
                }
                TextButton(onClick = { showManagePresets = true }) { Text("Manage") }
            }
        }
        if (selectionMode) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${selectedIds.size} selected", modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    val visible = (0 until rows.itemCount).mapNotNull { rows.peek(it)?.id }.toSet()
                    selectedIds = if (visible.isNotEmpty() && visible.all { it in selectedIds }) selectedIds - visible else selectedIds + visible
                }) { Text("Select loaded rows") }
                TextButton(onClick = { selectedIds = emptySet() }) { Text("Clear") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Draft", "Verified", "Incorrect", "Archived").forEach { status ->
                    TextButton(enabled = selectedIds.isNotEmpty(), onClick = { onBulkUpdate(selectedIds, status, null); selectedIds = emptySet() }) { Text(status) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Easy", "Medium", "Hard", "Unrated").forEach { difficulty ->
                    TextButton(enabled = selectedIds.isNotEmpty(), onClick = { onBulkUpdate(selectedIds, null, difficulty); selectedIds = emptySet() }) { Text(difficulty) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(enabled = selectedIds.isNotEmpty(), onClick = { bulkTagAdd = true; bulkTagText = ""; showBulkTagDialog = true }) { Text("Add tag") }
                TextButton(enabled = selectedIds.isNotEmpty(), onClick = { bulkTagAdd = false; bulkTagText = ""; showBulkTagDialog = true }) { Text("Remove tag") }
                TextButton(enabled = selectedIds.isNotEmpty() && collections.isNotEmpty(), onClick = { bulkCollectionAdd = true; bulkCollectionId = ""; showBulkCollectionDialog = true }) { Text("Add collection") }
                TextButton(enabled = selectedIds.isNotEmpty() && collections.isNotEmpty(), onClick = { bulkCollectionAdd = false; bulkCollectionId = ""; showBulkCollectionDialog = true }) { Text("Remove collection") }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rows.itemCount, key = rows.itemKey { it.id }) { index ->
                val row: QuestionLibraryRow? = rows[index]
                if (row != null) {
                    Card(Modifier.fillMaxWidth().clickable { if (selectionMode) selectedIds = if (row.id in selectedIds) selectedIds - row.id else selectedIds + row.id else onOpenQuestion(row.id) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectionMode) Checkbox(row.id in selectedIds, { checked -> selectedIds = if (checked) selectedIds + row.id else selectedIds - row.id })
                                Text(row.type, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.weight(1f))
                                Text(row.status, style = MaterialTheme.typography.labelSmall)
                                if (!selectionMode) {
                                    IconButton(onClick = { onOpenQuestion(row.id) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, "Edit") }
                                    IconButton(onClick = { onDeleteQuestion(row.id) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, "Delete") }
                                }
                            }
                            Text(row.prompt.take(360).let { if (row.prompt.length > 360) "$it…" else it }, style = MaterialTheme.typography.titleMedium, maxLines = 4)
                            val meta = listOf(row.subject, row.chapter, row.topic, row.exam).filter { it.isNotBlank() }.joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Difficulty: ${row.difficulty}", style = MaterialTheme.typography.labelSmall)
                            if (!selectionMode) Row { Spacer(Modifier.weight(1f)); TextButton(onClick = { onScheduleQuestion(row.id, "now") }) { Text("Due now") }; TextButton(onClick = { onScheduleQuestion(row.id, "week") }) { Text("7 days") }; TextButton(onClick = { onScheduleQuestion(row.id, "reset") }) { Text("Reset") } }
                        }
                    }
                }
            }
            when (val state = rows.loadState.append) {
                is LoadState.Loading -> item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                is LoadState.Error -> item { Column(Modifier.padding(16.dp)) { Text("Couldn't load more: ${state.error.localizedMessage.orEmpty()}"); TextButton(onClick = rows::retry) { Text("Retry") } } }
                else -> Unit
            }
            if (rows.loadState.refresh is LoadState.Loading && rows.itemCount == 0) item { CircularProgressIndicator(Modifier.padding(24.dp)) }
            if (rows.loadState.refresh is LoadState.Error && rows.itemCount == 0) item { Text("Couldn't load Library. Pull to retry or tap retry."); TextButton(onClick = rows::retry) { Text("Retry") } }
            if (rows.loadState.refresh is LoadState.NotLoading && rows.itemCount == 0) item { Text("No questions match these filters.", Modifier.padding(24.dp)) }
        }
    }
    if (showBulkTagDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBulkTagDialog = false },
            title = { Text(if (bulkTagAdd) "Add tag to selected" else "Remove tag from selected") },
            text = { OutlinedTextField(bulkTagText, { bulkTagText = it.filterNot { ch -> ch == ',' }.take(40) }, label = { Text("Tag") }, singleLine = true) },
            confirmButton = { TextButton(enabled = bulkTagText.isNotBlank(), onClick = { onBulkTag(selectedIds, bulkTagText.trim(), bulkTagAdd); selectedIds = emptySet(); showBulkTagDialog = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { showBulkTagDialog = false }) { Text("Cancel") } }
        )
    }
    if (showBulkCollectionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBulkCollectionDialog = false },
            title = { Text(if (bulkCollectionAdd) "Add selected to collection" else "Remove selected from collection") },
            text = { Column { collections.forEach { collection -> FilterChip(selected = bulkCollectionId == collection.id, onClick = { bulkCollectionId = collection.id }, label = { Text(collection.name) }) } } },
            confirmButton = { TextButton(enabled = bulkCollectionId.isNotBlank(), onClick = { onBulkCollection(selectedIds, bulkCollectionId, bulkCollectionAdd); selectedIds = emptySet(); showBulkCollectionDialog = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { showBulkCollectionDialog = false }) { Text("Cancel") } }
        )
    }
    if (showTagManager) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTagManager = false },
            title = { Text("Manage tags") },
            text = { Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(newTagName, { newTagName = it.filterNot { ch -> ch == ',' }.take(40) }, label = { Text("New tag") }, singleLine = true, modifier = Modifier.weight(1f))
                    TextButton(enabled = newTagName.isNotBlank(), onClick = { val clean = newTagName.trim(); if (clean.isNotEmpty() && tags.none { it.name.equals(clean, true) }) { onAddTag(clean); newTagName = "" } }) { Text("Add") }
                }
                tags.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tag.name)
                            Text("Used by ${tagUsageCounts[tag.id] ?: "…"} questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { editingTag = tag; editedTagName = tag.name }) { Text("Rename") }
                        TextButton(onClick = { deletingTag = tag }) { Text("Delete") }
                    }
                }
            } },
            confirmButton = { TextButton(onClick = { showTagManager = false }) { Text("Done") } }
        )
    }
    if (editingTag != null) {
        val original = editingTag!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editingTag = null },
            title = { Text("Rename tag") },
            text = { OutlinedTextField(editedTagName, { editedTagName = it.filterNot { ch -> ch == ',' }.take(40) }, singleLine = true, label = { Text("Tag name") }) },
            confirmButton = { TextButton(enabled = editedTagName.isNotBlank() && !tags.any { it.id != original.id && it.name.equals(editedTagName.trim(), true) }, onClick = { onRenameTag(original.name, editedTagName.trim()); editingTag = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editingTag = null }) { Text("Cancel") } }
        )
    }
    if (deletingTag != null) {
        val pendingTag = deletingTag!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deletingTag = null },
            title = { Text("Delete tag?") },
            text = { Text("Delete ‘${pendingTag.name}’ and remove it from all questions?") },
            confirmButton = { TextButton(onClick = { onDeleteTag(pendingTag); deletingTag = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deletingTag = null }) { Text("Cancel") } }
        )
    }
    if (showManagePresets) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showManagePresets = false },
            title = { Text("Manage smart filters") },
            text = { Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) { savedFilterPresets.forEach { preset ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { presetPendingDelete = preset }) { Text("Delete") }
                }
            } } },
            confirmButton = { TextButton(onClick = { showManagePresets = false }) { Text("Done") } }
        )
    }
    if (presetPendingDelete != null) {
        val pending = presetPendingDelete!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { presetPendingDelete = null },
            title = { Text("Delete saved filter?") },
            text = { Text("Delete ‘${pending.name}’? This does not delete any questions.") },
            confirmButton = { TextButton(onClick = { onDeletePreset(pending); presetPendingDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { presetPendingDelete = null }) { Text("Cancel") } }
        )
    }
    if (showPresetEditor) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPresetEditor = false },
            title = { Text("Save smart filter") },
            text = { OutlinedTextField(presetName, { presetName = it }, label = { Text("Name") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { val clean = presetName.trim(); if (clean.isNotEmpty()) { onSavePreset(clean, criteria); showPresetEditor = false } }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showPresetEditor = false }) { Text("Cancel") } }
        )
    }
}
