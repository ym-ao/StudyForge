package com.studyforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.studyforge.app.data.StudySettingsEntity
import kotlinx.coroutines.delay
import org.json.JSONArray
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

private val mistakeCategories = listOf(
    "Concept gap", "Wrong formula", "Calculation / algebra", "Unit conversion",
    "Sign / direction", "Misread question", "Forgot fact", "Guess / uncertainty"
)

@Composable
fun StudyScreen(
    questions: List<QuestionEntity>,
    reviewLogs: List<ReviewLogEntity>,
    settings: StudySettingsEntity,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf("Due") }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var mistakeCategory by rememberSaveable { mutableStateOf("") }
    var stepByStepStage by rememberSaveable { mutableIntStateOf(0) }
    var selectedTopic by rememberSaveable { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val midnight = remember(now / 60_000L) {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val logsToday = reviewLogs.filter { it.reviewedAt >= midnight }
    val newReviewedToday = logsToday.count { it.reviewMode == "New" }
    val reviewsToday = logsToday.count { it.reviewMode != "New" }
    val due = questions.filter { it.dueAt == null || it.dueAt <= now }
    val remainingNew = (settings.newCardsPerDay - newReviewedToday).coerceAtLeast(0)
    val remainingReviews = (settings.reviewCardsPerDay - reviewsToday).coerceAtLeast(0)
    val orderReviews = when (settings.reviewOrder) {
        "Oldest" -> compareBy<QuestionEntity> { it.dueAt ?: 0L }
        "Weakest" -> compareByDescending<QuestionEntity> { it.incorrectCount }.thenBy { it.dueAt ?: 0L }
        "Random" -> null
        else -> compareBy<QuestionEntity> { it.dueAt ?: 0L }
    }
    val dueNew = remember(due, settings.reviewOrder, remainingNew) {
        due.filter { it.learningState == "New" }.let { if (settings.reviewOrder == "Random") it.shuffled() else it }.take(remainingNew)
    }
    val dueReview = remember(due, settings.reviewOrder, remainingReviews) {
        due.filter { it.learningState != "New" }.let { list -> if (orderReviews == null) list.shuffled() else list.sortedWith(orderReviews) }.take(remainingReviews)
    }
    val dueQuestions = dueNew + dueReview
    val randomQuestions = remember(questions, mode) { if (mode == "Random") questions.shuffled() else emptyList() }
    val topics = remember(questions) { questions.map { it.topic.trim() }.filter { it.isNotBlank() }.distinct().sorted() }
    val activeQuestions = when (mode) {
        "Due" -> dueQuestions
        "Random" -> randomQuestions
        "Ordered" -> questions.sortedWith(compareBy<QuestionEntity> { it.subject.lowercase() }.thenBy { it.chapter.lowercase() }.thenBy { it.topic.lowercase() }.thenBy { it.createdAt })
        "Weakest" -> questions.sortedWith(compareByDescending<QuestionEntity> { it.incorrectCount }.thenByDescending { it.fsrsDifficulty })
        "Topic" -> if (selectedTopic.isBlank()) questions else questions.filter { it.topic.equals(selectedTopic, true) }
        else -> questions
    }

    LaunchedEffect(mode, activeQuestions.size, selectedTopic) {
        index = 0
        revealed = false
        checked = false
        selectedAnswer = null
        mistakeCategory = ""
        stepByStepStage = 0
    }

    if (mode == "Exam") {
        ExamModeScreen(questions = questions, settings = settings, onReview = onReview, onExit = { mode = "Due" })
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("Study", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Due", "All", "Practice", "Random", "Ordered", "Topic", "Weakest", "Step-by-step", "Exam").forEach { item ->
                FilterChip(selected = mode == item, onClick = { mode = item; if (item == "Topic" && selectedTopic.isBlank()) selectedTopic = topics.firstOrNull().orEmpty() }, label = { Text(item) })
            }
        }
        if (mode == "Topic" && topics.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                topics.forEach { topic -> FilterChip(selected = selectedTopic == topic, onClick = { selectedTopic = topic }, label = { Text(topic) }) }
            }
        }
        when (mode) {
            "Due" -> Text("Due: ${dueQuestions.size} • New remaining today: $remainingNew • Reviews remaining today: $remainingReviews", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Practice" -> Text("Practice mode never changes your FSRS schedule or review history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Weakest" -> Text("Questions with the most recorded incorrect answers appear first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Ordered" -> Text("Questions are grouped by subject, chapter, and topic.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Topic" -> Text("Focused topic: ${selectedTopic.ifBlank { "choose a topic" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            "Step-by-step" -> Text("Reveal the answer first, then reveal the explanation as a separate step.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Text("Study selection: $mode • ${activeQuestions.size} questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        val options = remember(q.id, q.optionsJson) {
            runCatching { JSONArray(q.optionsJson).let { array -> (0 until array.length()).map { array.optString(it) } } }.getOrDefault(emptyList())
        }
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(q.type.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${safeIndex + 1} / ${activeQuestions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RichContentText(q.prompt, style = MaterialTheme.typography.titleLarge)
                val metadata = listOf(q.subject, q.chapter, q.topic).filter { it.isNotBlank() }.joinToString(" • ")
                if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (q.latex.isNotBlank()) { HorizontalDivider(); LatexFormulaView(q.latex, Modifier.fillMaxWidth()) }

                if (q.type == "MCQ" && options.isNotEmpty()) {
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
                } else {
                    if (!revealed) Button(onClick = { revealed = true; stepByStepStage = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Reveal answer") }
                    else {
                        HorizontalDivider()
                        Text("Answer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        RichContentText(q.answer, style = MaterialTheme.typography.titleMedium)
                        if (mode == "Step-by-step" && stepByStepStage < 2) {
                            Button(onClick = { stepByStepStage = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Next: reveal explanation") }
                        }
                    }
                }
                if ((revealed || checked) && q.explanation.isNotBlank() && (mode != "Step-by-step" || stepByStepStage >= 2 || q.type == "MCQ")) {
                    HorizontalDivider()
                    Text("Explanation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    RichContentText(q.explanation)
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
                            val wasCorrect = if (q.type == "MCQ") selectedAnswer == q.answer else null
                            val reviewMode = if (q.learningState == "New") "New" else if (mode == "Exam") "Exam" else "Review"
                            onReview(q, rating, wasCorrect, if (wasCorrect == false) mistakeCategory else null, reviewMode)
                            if (mode == "Due") index = 0 else index = (safeIndex + 1) % activeQuestions.size
                            revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0
                        }, modifier = Modifier.weight(1f)) { Text(rating) }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { index = if (safeIndex == 0) activeQuestions.lastIndex else safeIndex - 1; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0 }, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { index = (safeIndex + 1) % activeQuestions.size; revealed = false; checked = false; selectedAnswer = null; mistakeCategory = ""; stepByStepStage = 0 }, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        Text("Ratings: Again, Hard, Good, and Easy update FSRS memory state. Practice mode does not record reviews.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExamModeScreen(
    questions: List<QuestionEntity>,
    settings: StudySettingsEntity,
    onReview: (QuestionEntity, String, Boolean?, String?, String) -> Unit,
    onExit: () -> Unit
) {
    val mcqs = remember(questions) { questions.filter { it.type == "MCQ" && runCatching { JSONArray(it.optionsJson).length() >= 2 }.getOrDefault(false) } }
    var examQuestions by remember { mutableStateOf(emptyList<QuestionEntity>()) }
    val answers = remember { mutableStateMapOf<String, String>() }
    val marked = remember { mutableStateMapOf<String, Boolean>() }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(settings.examDurationMinutes * 60) }
    var started by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var confirmSubmit by remember { mutableStateOf(false) }

    val finishExam: () -> Unit = {
        if (!submitted) {
            submitted = true
            examQuestions.forEach { question ->
                val chosen = answers[question.id]
                if (chosen != null) {
                    val correct = chosen == question.answer
                    onReview(question, if (correct) ReviewScheduler.GOOD else ReviewScheduler.AGAIN, correct, if (correct) null else "Exam answer incorrect", if (question.learningState == "New") "New" else "Exam")
                }
            }
        }
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
            Text("Timed MCQ exams use your saved exam settings. Only answered questions update your review history.")
            Text("Available MCQs: ${mcqs.size}")
            Text("Configured exam: ${settings.examQuestionCount} questions • ${settings.examDurationMinutes} minutes • ${if (settings.examRandomize) "random order" else "library order"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                val ordered = if (settings.examRandomize) mcqs.shuffled() else mcqs.sortedBy { it.createdAt }
                examQuestions = ordered.take(settings.examQuestionCount)
                answers.clear(); marked.clear(); currentIndex = 0; remainingSeconds = settings.examDurationMinutes * 60; submitted = false; started = true
            }, enabled = mcqs.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Start exam") }
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
                    Text("Marked for review: ${marked.values.count { it }} • Unanswered: ${examQuestions.count { answers[it.id] == null }}", style = MaterialTheme.typography.bodySmall)
                    Text("Answered questions have been added to review history.", style = MaterialTheme.typography.bodySmall)
                }
            }
            examQuestions.forEachIndexed { i, question ->
                val correct = answers[question.id] == question.answer
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Question ${i + 1} • ${if (answers[question.id] == null) "Unanswered" else if (correct) "Correct" else "Incorrect"}${if (marked[question.id] == true) " • Marked" else ""}", fontWeight = FontWeight.Bold, color = if (correct) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        RichContentText(question.prompt)
                        Text("Your answer: ${answers[question.id] ?: "Not answered"}")
                        Text("Correct answer: ${question.answer}", fontWeight = FontWeight.SemiBold)
                        if (question.explanation.isNotBlank()) RichContentText(question.explanation)
                    }
                }
            }
            Button(onClick = { started = false; submitted = false; examQuestions = emptyList(); onExit() }, modifier = Modifier.fillMaxWidth()) { Text("Finish") }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, examQuestions.lastIndex)
    val question = examQuestions[safeIndex]
    val options = remember(question.id, question.optionsJson) { runCatching { JSONArray(question.optionsJson).let { array -> (0 until array.length()).map { array.optString(it) } } }.getOrDefault(emptyList()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Exam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}", color = if (remainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Text("Question ${safeIndex + 1} of ${examQuestions.size}${if (marked[question.id] == true) " • Marked for review" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RichContentText(question.prompt, style = MaterialTheme.typography.titleLarge)
                if (question.latex.isNotBlank()) LatexFormulaView(question.latex, Modifier.fillMaxWidth())
                options.forEachIndexed { optionIndex, option ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { answers[question.id] = option }, colors = CardDefaults.cardColors(containerColor = if (answers[question.id] == option) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A'.code + optionIndex).toChar()}.  ", fontWeight = FontWeight.Bold)
                            Text(option)
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = { marked[question.id] = marked[question.id] != true }, modifier = Modifier.fillMaxWidth()) { Text(if (marked[question.id] == true) "Remove review mark" else "Mark for review") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { currentIndex = (safeIndex - 1).coerceAtLeast(0) }, enabled = safeIndex > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { currentIndex = (safeIndex + 1).coerceAtMost(examQuestions.lastIndex) }, enabled = safeIndex < examQuestions.lastIndex, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        Text("Answered: ${answers.size} / ${examQuestions.size} • Marked: ${marked.values.count { it }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { confirmSubmit = true }, modifier = Modifier.fillMaxWidth()) { Text("Submit exam") }
    }
    if (confirmSubmit) AlertDialog(
        onDismissRequest = { confirmSubmit = false },
        title = { Text("Submit exam?") },
        text = { Text("You answered ${answers.size} of ${examQuestions.size} questions. Unanswered questions will not be added to review history.") },
        confirmButton = { TextButton(onClick = { confirmSubmit = false; finishExam() }) { Text("Submit") } },
        dismissButton = { TextButton(onClick = { confirmSubmit = false }) { Text("Continue exam") } }
    )
}

@Composable
fun ProgressScreen(questions: List<QuestionEntity>, reviewLogs: List<ReviewLogEntity>) {
    val now = System.currentTimeMillis()
    val due = questions.count { it.dueAt == null || it.dueAt <= now }
    val newCount = questions.count { it.learningState == "New" }
    val learning = questions.count { it.learningState == "Learning" || it.learningState == "Relearning" }
    val mastered = questions.count { it.fsrsStability >= 21.0 && it.learningState == "Review" }
    val scoredLogs = reviewLogs.filter { it.wasCorrect != null }
    val correct = scoredLogs.count { it.wasCorrect == true }
    val accuracy = if (scoredLogs.isEmpty()) 0 else correct * 100 / scoredLogs.size
    val cutoff = now - 30L * 24L * 60L * 60L * 1000L
    val recentLogs = reviewLogs.filter { it.reviewedAt >= cutoff }
    val recentScored = recentLogs.filter { it.wasCorrect != null }
    val recentAccuracy = if (recentScored.isEmpty()) 0 else recentScored.count { it.wasCorrect == true } * 100 / recentScored.size
    val byId = questions.associateBy { it.id }
    val weakTopics = reviewLogs.filter { it.wasCorrect == false }.groupBy { log ->
        val q = byId[log.questionId]
        when { q == null -> "Deleted question"; q.topic.isNotBlank() -> q.topic; q.chapter.isNotBlank() -> q.chapter; q.subject.isNotBlank() -> q.subject; else -> "Uncategorized" }
    }.mapValues { it.value.size }.toList().sortedByDescending { it.second }.take(6)
    val mistakes = reviewLogs.filter { it.mistakeCategory.isNotBlank() }.groupBy { it.mistakeCategory }.mapValues { it.value.size }.toList().sortedByDescending { it.second }.take(8)
    val weakSubjects = questions.groupBy { it.subject.ifBlank { "Uncategorized" } }.mapValues { (_, items) -> items.sumOf { it.incorrectCount } }.toList().sortedByDescending { it.second }.take(6)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Analytics are calculated from recorded reviews, not sample data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Due now", due.toString(), Modifier.weight(1f)); ProgressStat("Total reviews", reviewLogs.size.toString(), Modifier.weight(1f)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressStat("Accuracy", "$accuracy%", Modifier.weight(1f)); ProgressStat("30-day accuracy", "$recentAccuracy%", Modifier.weight(1f)) }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Learning status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("New: $newCount"); Text("Learning / relearning: $learning"); Text("FSRS stability 21+ days: $mastered")
                Text("Questions in library: ${questions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        ProgressListCard("Topics with most incorrect answers", weakTopics.map { it.first to "${it.second} incorrect" }, "No incorrect answers recorded yet.")
        ProgressListCard("Subjects with most recorded mistakes", weakSubjects.filter { it.second > 0 }.map { it.first to "${it.second} incorrect" }, "No subject-level mistakes recorded yet.")
        ProgressListCard("Mistake categories", mistakes.map { it.first to "${it.second} times" }, "Mistake categories appear here when you classify an incorrect answer.")
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (reviewLogs.isEmpty()) Text("No review history yet. Complete a study session to start tracking progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else reviewLogs.take(8).forEachIndexed { index, log ->
                    val q = byId[log.questionId]
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(q?.prompt ?: "Question no longer in library", maxLines = 2, fontWeight = FontWeight.Medium)
                        Text("${log.rating} • ${log.reviewMode} • ${formatReviewDate(log.reviewedAt)}${if (log.mistakeCategory.isNotBlank()) " • ${log.mistakeCategory}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (index != reviewLogs.take(8).lastIndex) HorizontalDivider()
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
