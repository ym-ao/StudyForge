package com.studyforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.studyforge.app.data.ReviewLogEntity
import com.studyforge.app.data.ReviewScheduler
import kotlinx.coroutines.delay
import org.json.JSONArray
import java.text.DateFormat
import java.util.Date

private val mistakeCategories = listOf(
    "Concept gap", "Wrong formula", "Calculation / algebra", "Unit conversion",
    "Sign / direction", "Misread question", "Forgot fact", "Guess / uncertainty"
)

@Composable
fun StudyScreen(
    questions: List<QuestionEntity>,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf("Due") }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var mistakeCategory by rememberSaveable { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val dueQuestions = questions.filter { it.dueAt == null || it.dueAt <= now }
    val activeQuestions = when (mode) {
        "Due" -> dueQuestions
        else -> questions
    }

    LaunchedEffect(mode, activeQuestions.size) {
        index = 0
        revealed = false
        checked = false
        selectedAnswer = null
        mistakeCategory = ""
    }

    if (mode == "Exam") {
        ExamModeScreen(
            questions = questions,
            onReview = onReview,
            onExit = { mode = "Due" }
        )
        return
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text("Study", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Due", "All", "Practice", "Exam").forEach { item ->
                FilterChip(selected = mode == item, onClick = { mode = item }, label = { Text(item) })
            }
        }
        when (mode) {
            "Due" -> Text("Due now: ${dueQuestions.size} • New questions are included", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Practice" -> Text("Practice mode does not change your review schedule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Text("Review every question in your library.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (activeQuestions.isEmpty()) {
            StudyEmptyState(
                title = if (mode == "Due") "You're caught up" else "Nothing to study yet",
                message = if (mode == "Due") "No cards are due right now. Try Practice or come back when your next review is due." else "Add flashcards or MCQs in your library, then return here."
            )
            return@Column
        }

        val safeIndex = index.coerceIn(0, activeQuestions.lastIndex)
        val q = activeQuestions[safeIndex]
        val options = remember(q.id, q.optionsJson) {
            runCatching {
                JSONArray(q.optionsJson).let { array -> (0 until array.length()).map { array.optString(it) } }
            }.getOrDefault(emptyList())
        }
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(q.type.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${safeIndex + 1} / ${activeQuestions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(q.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                val metadata = listOf(q.subject, q.chapter, q.topic).filter { it.isNotBlank() }.joinToString(" • ")
                if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (q.latex.isNotBlank()) {
                    HorizontalDivider()
                    Text("Equation source", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    Text(q.latex, style = MaterialTheme.typography.bodyLarge)
                }

                if (q.type == "MCQ" && options.isNotEmpty()) {
                    options.forEachIndexed { optionIndex, option ->
                        val prefix = ('A'.code + optionIndex).toChar()
                        val isCorrectOption = checked && option == q.answer
                        val background = when {
                            isCorrectOption -> MaterialTheme.colorScheme.tertiaryContainer
                            selectedAnswer == option -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !checked) { selectedAnswer = option },
                            colors = CardDefaults.cardColors(containerColor = background)
                        ) {
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
                        Text(
                            if (right) "Correct answer" else "Not quite — review the correct answer.",
                            fontWeight = FontWeight.Bold,
                            color = if (right) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                        Text("Answer: ${q.answer}", fontWeight = FontWeight.SemiBold)
                        if (!right && mode != "Practice") {
                            Text("What caused the mistake?", fontWeight = FontWeight.SemiBold)
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                mistakeCategories.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        pair.forEach { category ->
                                            FilterChip(
                                                selected = mistakeCategory == category,
                                                onClick = { mistakeCategory = if (mistakeCategory == category) "" else category },
                                                label = { Text(category) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
            if (mode == "Practice") {
                Button(onClick = {
                    index = (safeIndex + 1) % activeQuestions.size
                    revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""
                }, modifier = Modifier.fillMaxWidth()) { Text("Next question") }
            } else {
                Text("How well did you know this?", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(ReviewScheduler.AGAIN, ReviewScheduler.HARD, ReviewScheduler.GOOD, ReviewScheduler.EASY).forEach { rating ->
                        OutlinedButton(
                            onClick = {
                                val wasCorrect = if (q.type == "MCQ") selectedAnswer == q.answer else null
                                onReview(q, rating, wasCorrect, if (wasCorrect == false) mistakeCategory else null, "Review")
                                if (mode == "Due") index = 0 else index = (safeIndex + 1) % activeQuestions.size
                                revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(rating) }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { index = if (safeIndex == 0) activeQuestions.lastIndex else safeIndex - 1; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = "" },
                modifier = Modifier.weight(1f)
            ) { Text("Previous") }
            OutlinedButton(
                onClick = { index = (safeIndex + 1) % activeQuestions.size; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = "" },
                modifier = Modifier.weight(1f)
            ) { Text("Next") }
        }
        Text("Again schedules a short relearning step. Hard, Good and Easy set progressively longer review intervals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExamModeScreen(
    questions: List<QuestionEntity>,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit,
    onExit: () -> Unit
) {
    val mcqs = remember(questions) { questions.filter { it.type == "MCQ" && runCatching { JSONArray(it.optionsJson).length() >= 2 }.getOrDefault(false) } }
    var examQuestions by remember { mutableStateOf(emptyList<QuestionEntity>()) }
    val answers = remember { mutableStateMapOf<String, String>() }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(15 * 60) }
    var started by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val finishExam: () -> Unit = {
        if (!submitted) {
            submitted = true
            examQuestions.forEach { question ->
                val correct = answers[question.id] == question.answer
                onReview(
                    question,
                    if (correct) ReviewScheduler.GOOD else ReviewScheduler.AGAIN,
                    correct,
                    if (correct) null else "Exam answer incorrect",
                    "Exam"
                )
            }
        }
    }

    LaunchedEffect(started, submitted) {
        if (started && !submitted) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds -= 1
            }
            if (!submitted && remainingSeconds == 0) finishExam()
        }
    }

    if (!started) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Exam mode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("A timed MCQ session. Your answers are checked after submission, and the results update your review schedule.")
            Text("Available MCQs: ${mcqs.size}")
            Text("Test length: up to 10 questions • Time limit: 15 minutes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = {
                    examQuestions = mcqs.shuffled().take(10)
                    answers.clear(); currentIndex = 0; remainingSeconds = 15 * 60; submitted = false; started = true
                },
                enabled = mcqs.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start exam") }
            OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Back to study") }
            if (mcqs.isEmpty()) Text("Add at least one MCQ with two or more options to start an exam.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    if (submitted) {
        val score = examQuestions.count { answers[it.id] == it.answer }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Exam results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("$score / ${examQuestions.size}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("${if (examQuestions.isEmpty()) 0 else score * 100 / examQuestions.size}% correct")
                    Text("Your answers have been added to review history.", style = MaterialTheme.typography.bodySmall)
                }
            }
            examQuestions.forEachIndexed { i, question ->
                val correct = answers[question.id] == question.answer
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Question ${i + 1} • ${if (correct) "Correct" else "Incorrect"}", fontWeight = FontWeight.Bold, color = if (correct) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        Text(question.prompt)
                        Text("Your answer: ${answers[question.id] ?: "Not answered"}", style = MaterialTheme.typography.bodySmall)
                        Text("Correct answer: ${question.answer}", fontWeight = FontWeight.SemiBold)
                        if (question.explanation.isNotBlank()) Text(question.explanation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Button(onClick = { started = false; submitted = false; examQuestions = emptyList(); onExit() }, modifier = Modifier.fillMaxWidth()) { Text("Finish") }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, examQuestions.lastIndex)
    val question = examQuestions[safeIndex]
    val options = remember(question.id, question.optionsJson) {
        runCatching { JSONArray(question.optionsJson).let { array -> (0 until array.length()).map { array.optString(it) } } }.getOrDefault(emptyList())
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Exam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}", color = if (remainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Text("Question ${safeIndex + 1} of ${examQuestions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(question.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                options.forEachIndexed { optionIndex, option ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { answers[question.id] = option },
                        colors = CardDefaults.cardColors(containerColor = if (answers[question.id] == option) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A'.code + optionIndex).toChar()}.  ", fontWeight = FontWeight.Bold)
                            Text(option)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { currentIndex = (safeIndex - 1).coerceAtLeast(0) }, enabled = safeIndex > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { currentIndex = (safeIndex + 1).coerceAtMost(examQuestions.lastIndex) }, enabled = safeIndex < examQuestions.lastIndex, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        Text("Answered: ${answers.size} / ${examQuestions.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = finishExam, modifier = Modifier.fillMaxWidth()) { Text("Submit exam") }
    }
}

@Composable
fun ProgressScreen(questions: List<QuestionEntity>, reviewLogs: List<ReviewLogEntity>) {
    val now = System.currentTimeMillis()
    val due = questions.count { it.dueAt == null || it.dueAt <= now }
    val newCount = questions.count { it.learningState == "New" }
    val learning = questions.count { it.learningState == "Learning" || it.learningState == "Relearning" }
    val mastered = questions.count { it.intervalDays >= 21.0 && it.learningState == "Review" }
    val scoredLogs = reviewLogs.filter { it.wasCorrect != null }
    val correct = scoredLogs.count { it.wasCorrect == true }
    val accuracy = if (scoredLogs.isEmpty()) 0 else correct * 100 / scoredLogs.size
    val cutoff = now - 30L * 24L * 60L * 60L * 1000L
    val recentLogs = reviewLogs.filter { it.reviewedAt >= cutoff }
    val recentScored = recentLogs.filter { it.wasCorrect != null }
    val recentAccuracy = if (recentScored.isEmpty()) 0 else recentScored.count { it.wasCorrect == true } * 100 / recentScored.size
    val byId = questions.associateBy { it.id }
    val weakTopics = reviewLogs.filter { it.wasCorrect == false }
        .groupBy { log ->
            val q = byId[log.questionId]
            when {
                q == null -> "Deleted question"
                q.topic.isNotBlank() -> q.topic
                q.chapter.isNotBlank() -> q.chapter
                q.subject.isNotBlank() -> q.subject
                else -> "Uncategorized"
            }
        }.mapValues { it.value.size }.toList().sortedByDescending { it.second }.take(6)
    val mistakes = reviewLogs.filter { it.mistakeCategory.isNotBlank() }
        .groupBy { it.mistakeCategory }.mapValues { it.value.size }.toList().sortedByDescending { it.second }.take(8)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Your review activity and the areas that need more attention.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressStat("Due now", due.toString(), Modifier.weight(1f))
            ProgressStat("Total reviews", reviewLogs.size.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressStat("Accuracy", "$accuracy%", Modifier.weight(1f))
            ProgressStat("30-day accuracy", "$recentAccuracy%", Modifier.weight(1f))
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Learning status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("New: $newCount")
                Text("Learning / relearning: $learning")
                Text("Long intervals (21+ days): $mastered")
                Text("Questions in library: ${questions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProgressListCard("Topics with most incorrect answers", weakTopics.map { it.first to "${it.second} incorrect" }, "No incorrect answers recorded yet.")
        ProgressListCard("Mistake categories", mistakes.map { it.first to "${it.second} times" }, "Mistake categories appear here when you classify an incorrect answer.")
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (reviewLogs.isEmpty()) {
                    Text("No review history yet. Complete a study session to start tracking progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    reviewLogs.take(8).forEach { log ->
                        val q = byId[log.questionId]
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(q?.prompt ?: "Question no longer in library", maxLines = 2, fontWeight = FontWeight.Medium)
                            Text("${log.rating} • ${log.reviewMode} • ${formatReviewDate(log.reviewedAt)}${if (log.mistakeCategory.isNotBlank()) " • ${log.mistakeCategory}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (log != reviewLogs.take(8).last()) HorizontalDivider()
                    }
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
            else rows.forEach { (label, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.weight(1f))
                    Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
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
