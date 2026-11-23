package com.studyforge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.StudySettingsEntity

@Composable
fun SettingsScreen(settings: StudySettingsEntity, onSave: (StudySettingsEntity) -> Unit) {
    var retention by remember(settings) { mutableFloatStateOf(settings.desiredRetention.toFloat()) }
    var maxInterval by remember(settings) { mutableStateOf(settings.maxIntervalDays.toString()) }
    var newLimit by remember(settings) { mutableStateOf(settings.newCardsPerDay.toString()) }
    var reviewLimit by remember(settings) { mutableStateOf(settings.reviewCardsPerDay.toString()) }
    var examCount by remember(settings) { mutableStateOf(settings.examQuestionCount.toString()) }
    var examMinutes by remember(settings) { mutableStateOf(settings.examDurationMinutes.toString()) }
    var learningSteps by remember(settings) { mutableStateOf(settings.learningStepsMinutesCsv) }
    var relearningSteps by remember(settings) { mutableStateOf(settings.relearningStepsMinutesCsv) }
    var reviewOrder by remember(settings) { mutableStateOf(settings.reviewOrder) }
    var randomExam by remember(settings) { mutableStateOf(settings.examRandomize) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Study settings", style = MaterialTheme.typography.headlineSmall)
        Text("These settings are stored locally and included in JSON backups.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FSRS scheduling", style = MaterialTheme.typography.titleMedium)
                Text("Desired retention: ${(retention * 100).toInt()}%")
                Slider(value = retention, onValueChange = { retention = it }, valueRange = 0.80f..0.97f, steps = 16)
                OutlinedTextField(maxInterval, { maxInterval = it.filter(Char::isDigit).take(5) }, label = { Text("Maximum interval (days)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(learningSteps, { learningSteps = it.filter { ch -> ch.isDigit() || ch == ',' || ch == ' ' }.take(32) }, label = { Text("New-card retry delay (minutes)") }, supportingText = { Text("The first valid number is used after Again. Example: 10") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(relearningSteps, { relearningSteps = it.filter { ch -> ch.isDigit() || ch == ',' || ch == ' ' }.take(32) }, label = { Text("Relearning retry delay (minutes)") }, supportingText = { Text("The first valid number is used after Again. Example: 10") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(newLimit, { newLimit = it.filter(Char::isDigit).take(4) }, label = { Text("New cards per day") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(reviewLimit, { reviewLimit = it.filter(Char::isDigit).take(5) }, label = { Text("Review cards per day") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Review order")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Due first", "Oldest", "Random", "Weakest").forEach { order ->
                        FilterChip(selected = reviewOrder == order, onClick = { reviewOrder = order }, label = { Text(order) })
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Exam settings", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(examCount, { examCount = it.filter(Char::isDigit).take(3) }, label = { Text("Questions per exam") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(examMinutes, { examMinutes = it.filter(Char::isDigit).take(3) }, label = { Text("Exam duration (minutes)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FilterChip(selected = randomExam, onClick = { randomExam = !randomExam }, label = { Text(if (randomExam) "Randomize questions: on" else "Randomize questions: off") })
            }
        }
        Button(
            onClick = {
                onSave(settings.copy(
                    desiredRetention = retention.toDouble().coerceIn(0.80, 0.97),
                    maxIntervalDays = (maxInterval.toIntOrNull() ?: 36500).coerceIn(1, 36500),
                    newCardsPerDay = (newLimit.toIntOrNull() ?: 20).coerceIn(0, 500),
                    reviewCardsPerDay = (reviewLimit.toIntOrNull() ?: 200).coerceIn(0, 2000),
                    examQuestionCount = (examCount.toIntOrNull() ?: 20).coerceIn(1, 200),
                    examDurationMinutes = (examMinutes.toIntOrNull() ?: 30).coerceIn(1, 240),
                    learningStepsMinutesCsv = learningSteps.ifBlank { "10" },
                    relearningStepsMinutesCsv = relearningSteps.ifBlank { "10" },
                    reviewOrder = reviewOrder,
                    examRandomize = randomExam
                ))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save settings") }
        Spacer(Modifier.height(12.dp))
    }
}
