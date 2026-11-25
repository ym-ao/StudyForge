package com.studyforge.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.ratex.RaTeXView
import com.studyforge.app.data.ContentBlockParsing

/** Native Canvas-based math rendering. The renderer and fonts are packaged in the APK. */
@Composable
fun LatexFormulaView(latex: String, modifier: Modifier = Modifier) {
    val source = latex.trim().take(5000)
    if (source.isBlank()) return
    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = 42.dp),
        factory = { context ->
            RaTeXView(context).apply {
                this.latex = source
                fontSize = 22f
                displayMode = true
            }
        },
        update = { view ->
            view.latex = source
            view.fontSize = 22f
            view.displayMode = true
        }
    )
}

/** Small offline Markdown-like renderer for headings, bullets, bold, italic and inline code. */
@Composable
fun RichContentText(content: String, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    if (content.isBlank()) return
    val annotated = AnnotatedString.Builder()
    content.lineSequence().forEachIndexed { lineIndex, line ->
        if (lineIndex > 0) annotated.append("\n")
        val trimmed = line.trimStart()
        when {
            trimmed.startsWith("### ") -> appendStyledLine(annotated, trimmed.removePrefix("### "), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
            trimmed.startsWith("## ") -> appendStyledLine(annotated, trimmed.removePrefix("## "), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp))
            trimmed.startsWith("# ") -> appendStyledLine(annotated, trimmed.removePrefix("# "), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                annotated.append("• ")
                appendInlineMarkup(annotated, trimmed.drop(2))
            }
            else -> appendInlineMarkup(annotated, line)
        }
    }
    Text(annotated.toAnnotatedString(), modifier = modifier, style = style)
}

private fun appendStyledLine(builder: AnnotatedString.Builder, text: String, style: SpanStyle) {
    builder.pushStyle(style)
    appendInlineMarkup(builder, text)
    builder.pop()
}

private fun appendInlineMarkup(builder: AnnotatedString.Builder, text: String) {
    val pattern = Regex("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*|`(.+?)`|__(.+?)__|==(.+?)==")
    var cursor = 0
    for (match in pattern.findAll(text)) {
        if (match.range.first > cursor) builder.append(text.substring(cursor, match.range.first))
        when {
            match.groups[1] != null -> {
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(match.groups[1]!!.value)
                builder.pop()
            }
            match.groups[2] != null -> {
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(match.groups[2]!!.value)
                builder.pop()
            }
            match.groups[3] != null -> {
                builder.pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.25f), textDecoration = TextDecoration.None))
                builder.append(match.groups[3]!!.value)
                builder.pop()
            }
            match.groups[4] != null -> {
                builder.pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                builder.append(match.groups[4]!!.value)
                builder.pop()
            }
            else -> {
                builder.pushStyle(SpanStyle(background = Color.Yellow.copy(alpha = 0.35f)))
                builder.append(match.groups[5]!!.value)
                builder.pop()
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) builder.append(text.substring(cursor))
}

/** Renders image bytes embedded in the local question record; no network or URI permission is needed. */
@Composable
fun OfflineImageBlock(base64: String, description: String) {
    val bitmap = androidx.compose.runtime.remember(base64) {
        runCatching {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = description.ifBlank { "Question image" },
            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    } else {
        Text("Image unavailable or invalid", color = MaterialTheme.colorScheme.error)
    }
}

/** Table block format: pipe-separated cells, one row per line. */
@Composable
fun ContentTable(source: String) {
    val rows = androidx.compose.runtime.remember(source) { ContentBlockParsing.tableRows(source) }
    if (rows.isEmpty()) return
    val columns = rows.maxOf { it.size }.coerceAtMost(8)
    Column(Modifier.fillMaxWidth()) {
        rows.take(30).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(columns) { columnIndex ->
                    val cell = row.getOrNull(columnIndex).orEmpty()
                    androidx.compose.foundation.layout.Box(
                        Modifier.weight(1f).padding(1.dp)
                            .background(if (rowIndex == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(7.dp)
                    ) {
                        Text(cell, style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

/** Graph block format: numeric x,y pairs, one pair per line. Axes are normalized to the data range. */
@Composable
fun ContentGraph(source: String) {
    val points = androidx.compose.runtime.remember(source) { ContentBlockParsing.graphPoints(source) }
    if (points.size < 2) {
        Text("Enter at least two numeric x,y pairs, one per line.", style = MaterialTheme.typography.bodySmall)
        return
    }
    val minX = points.minOf { it.first }; val maxX = points.maxOf { it.first }
    val minY = points.minOf { it.second }; val maxY = points.maxOf { it.second }
    val spanX = (maxX - minX).takeIf { it != 0f } ?: 1f
    val spanY = (maxY - minY).takeIf { it != 0f } ?: 1f
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.tertiary
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().heightIn(min = 180.dp).height(210.dp).padding(8.dp)) {
        val left = 24f; val right = size.width - 12f; val top = 12f; val bottom = size.height - 24f
        drawLine(axisColor, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), 2f)
        drawLine(axisColor, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, bottom), 2f)
        val mapped = points.map { (x, y) ->
            androidx.compose.ui.geometry.Offset(left + (x - minX) / spanX * (right - left), bottom - (y - minY) / spanY * (bottom - top))
        }
        for (i in 0 until mapped.lastIndex) drawLine(lineColor, mapped[i], mapped[i + 1], 3f)
        mapped.forEach { drawCircle(pointColor, 4f, it) }
    }
    Text("x: $minX to $maxX    y: $minY to $maxY", style = MaterialTheme.typography.labelSmall)
}


/** Stepwise derivation viewer; the learner reveals one validated equation and explanation at a time. */
@Composable
fun DerivationBlock(source: String) {
    val steps = androidx.compose.runtime.remember(source) { source.lineSequence().mapNotNull { line ->
        val p = line.split('|', limit = 3)
        if (p.size < 2 || p[1].isBlank()) null else Triple(p[0].trim(), p[1].trim(), p.getOrNull(2).orEmpty().trim())
    }.take(40).toList() }
    var visibleCount by androidx.compose.runtime.remember(source) { androidx.compose.runtime.mutableIntStateOf(1) }
    if (steps.isEmpty()) return
    Column(Modifier.fillMaxWidth(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        Text("Derivation · $visibleCount / ${steps.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        steps.take(visibleCount).forEachIndexed { index, step ->
            Text("Step ${index + 1}: ${step.first.ifBlank { "Derivation" }}", fontWeight = FontWeight.SemiBold)
            LatexFormulaView(step.second)
            if (step.third.isNotBlank()) RichContentText(step.third)
        }
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.OutlinedButton(onClick = { visibleCount = (visibleCount - 1).coerceAtLeast(1) }, enabled = visibleCount > 1) { Text("Previous step") }
            androidx.compose.material3.Button(onClick = { visibleCount = (visibleCount + 1).coerceAtMost(steps.size) }, enabled = visibleCount < steps.size) { Text(if (visibleCount == steps.size) "Complete" else "Next step") }
        }
    }
}
