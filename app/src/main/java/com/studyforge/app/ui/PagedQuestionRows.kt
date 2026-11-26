package com.studyforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.studyforge.app.data.QuestionLibraryRow

/**
 * Reusable lightweight Library list for V3.0. It deliberately renders only a
 * question summary; answer, LaTeX, images and content blocks load on detail open.
 */
@Composable
fun PagedQuestionRows(
    questions: LazyPagingItems<QuestionLibraryRow>,
    onOpenQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = questions.itemCount,
            key = questions.itemKey { it.id }
        ) { index ->
            val row = questions[index]
            if (row != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenQuestion(row.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(row.prompt, style = MaterialTheme.typography.titleSmall, maxLines = 3)
                    Text(
                        listOf(row.subject, row.chapter, row.topic).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        "${row.type} · ${row.status} · ${row.difficulty}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (val append = questions.loadState.append) {
            is LoadState.Loading -> item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            is LoadState.Error -> item {
                Column(Modifier.padding(16.dp)) {
                    Text("Couldn't load more questions: ${append.error.localizedMessage.orEmpty()}")
                    TextButton(onClick = questions::retry) { Text("Retry") }
                }
            }
            else -> Unit
        }

        if (questions.loadState.refresh is LoadState.Loading && questions.itemCount == 0) {
            item { CircularProgressIndicator(Modifier.padding(24.dp)) }
        }
        if (questions.loadState.refresh is LoadState.Error && questions.itemCount == 0) {
            val error = (questions.loadState.refresh as LoadState.Error).error
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Couldn't load the Library: ${error.localizedMessage.orEmpty()}")
                    TextButton(onClick = questions::retry) { Text("Retry") }
                }
            }
        }
        if (questions.loadState.refresh is LoadState.NotLoading && questions.itemCount == 0) {
            item { Text("No questions match these filters.", Modifier.padding(24.dp)) }
        }
    }
}
