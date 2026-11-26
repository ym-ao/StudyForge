package com.studyforge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.NumericalTemplateCodec
import com.studyforge.app.data.NumericalTemplateEngine
import java.util.UUID

/** Small authoring surface for versioned numerical templates. It never executes user code. */
@Composable
fun NumericalTemplateEditor(
    initialPayload: String,
    onChange: (payload: String?, error: String?) -> Unit
) {
    val decoded = remember(initialPayload) { if (initialPayload.isBlank()) null else NumericalTemplateCodec.decode(initialPayload).getOrNull() }
    var templatePrompt by remember(initialPayload) { mutableStateOf(decoded?.prompt ?: "Calculate the result when x = {x}.") }
    var explanation by remember(initialPayload) { mutableStateOf(decoded?.explanation ?: "Substitute x = {x} into the formula.") }
    var variables by remember(initialPayload) { mutableStateOf(decoded?.variables?.joinToString("\n") { "${it.name},${it.minimum},${it.maximum},${it.decimals},${it.unit}" } ?: "x,1,10,0,") }
    var constraints by remember(initialPayload) { mutableStateOf(decoded?.constraints?.joinToString("\n") ?: "") }
    var expression by remember(initialPayload) { mutableStateOf(decoded?.answerExpression ?: "x * 2") }
    var answerUnit by remember(initialPayload) { mutableStateOf(decoded?.answerUnit ?: "") }
    var tolerance by remember(initialPayload) { mutableStateOf(decoded?.absoluteTolerance?.toString() ?: "0") }
    var seedText by remember(initialPayload) { mutableStateOf("42") }
    var status by remember(initialPayload) { mutableStateOf(decoded?.authoringStatus ?: "DRAFT") }

    fun publish() {
        val parsedVariables = variables.lines().filter { it.isNotBlank() }.map { line ->
            val p = line.split(',', limit = 5).map { it.trim() }
            require(p.size == 5) { "Each variable must be: name,min,max,decimals,unit (use a blank final unit if unitless)." }
            NumericalTemplateEngine.Variable(p[0], p[1].toDouble(), p[2].toDouble(), p[3].toInt(), p[4])
        }
        val template = NumericalTemplateEngine.Template(
            id = decoded?.id ?: UUID.nameUUIDFromBytes((templatePrompt + expression).toByteArray()).toString(),
            version = decoded?.version ?: 1, prompt = templatePrompt, explanation = explanation,
            variables = parsedVariables, constraints = constraints.lines().filter { it.isNotBlank() },
            answerExpression = expression, answerUnit = answerUnit,
            absoluteTolerance = tolerance.toDouble(), relativeTolerance = decoded?.relativeTolerance ?: 0.0,
            decimalPlaces = decoded?.decimalPlaces ?: 2, significantFigures = decoded?.significantFigures ?: 0,
            acceptedEquivalentExpressions = decoded?.acceptedEquivalentExpressions ?: emptyList(), authoringStatus = status
        )
        val validation = NumericalTemplateEngine.validate(template)
        require(validation.valid) { validation.errors.joinToString("\n") }
        onChange(NumericalTemplateCodec.encode(template), null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Numerical template", style = MaterialTheme.typography.titleMedium)
        Text("Variables use one row each: name,min,max,decimals,unit. Leave unit blank for unitless values.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(templatePrompt, { templatePrompt = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid template") } }, Modifier.fillMaxWidth(), label = { Text("Question template (use {x})") }, minLines = 2)
        OutlinedTextField(explanation, { explanation = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid template") } }, Modifier.fillMaxWidth(), label = { Text("Explanation template") }, minLines = 2)
        OutlinedTextField(variables, { variables = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid variables") } }, Modifier.fillMaxWidth(), label = { Text("Variables") }, minLines = 2)
        OutlinedTextField(constraints, { constraints = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid constraints") } }, Modifier.fillMaxWidth(), label = { Text("Constraints (one expression per line)") }, minLines = 2)
        OutlinedTextField(expression, { expression = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid formula") } }, Modifier.fillMaxWidth(), label = { Text("Answer formula") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(answerUnit, { answerUnit = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid unit") } }, Modifier.weight(1f), label = { Text("Answer unit") }, singleLine = true)
            OutlinedTextField(tolerance, { tolerance = it; runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Invalid tolerance") } }, Modifier.weight(1f), label = { Text("Absolute tolerance") }, singleLine = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { status = "DRAFT"; runCatching { publish() }.onFailure { e -> onChange(null, e.message) } }, label = { Text(if (status == "DRAFT") "● Draft" else "Draft") })
            AssistChip(onClick = { status = "VERIFIED"; runCatching { publish() }.onFailure { e -> onChange(null, e.message) } }, label = { Text(if (status == "VERIFIED") "● Verified" else "Verified") })
        }
        OutlinedTextField(seedText, { seedText = it }, Modifier.fillMaxWidth(), label = { Text("Preview seed") }, singleLine = true)
        val preview = remember(templatePrompt, explanation, variables, constraints, expression, answerUnit, tolerance, seedText, status, initialPayload) {
            runCatching {
                val p = variables.lines().filter { it.isNotBlank() }.map { line -> line.split(',', limit = 5).map(String::trim).also { require(it.size == 5) } }
                val t = NumericalTemplateEngine.Template(
                    id = decoded?.id ?: "preview", version = decoded?.version ?: 1, prompt = templatePrompt, explanation = explanation,
                    variables = p.map { NumericalTemplateEngine.Variable(it[0], it[1].toDouble(), it[2].toDouble(), it[3].toInt(), it[4]) },
                    constraints = constraints.lines().filter { it.isNotBlank() }, answerExpression = expression, answerUnit = answerUnit,
                    absoluteTolerance = tolerance.toDouble(), authoringStatus = status
                )
                val validation = NumericalTemplateEngine.validate(t)
                require(validation.valid) { validation.errors.joinToString(" ") }
                val variant = NumericalTemplateEngine.generate(t, seedText.toLong()) .getOrThrow()
                "${variant.prompt}\nAnswer: ${NumericalTemplateEngine.format(variant.answer, t.decimalPlaces, t.significantFigures)} ${variant.answerUnit}\n${variant.explanation}"
            }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Seeded preview", style = MaterialTheme.typography.labelLarge)
                Text(preview.getOrElse { it.message ?: "Complete the fields to preview." }, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedButton(onClick = { runCatching { publish() }.onFailure { e -> onChange(null, e.message ?: "Template validation failed") } }, modifier = Modifier.fillMaxWidth()) { Text("Validate and save template draft") }
    }
}
