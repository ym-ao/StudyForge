package com.studyforge.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val pattern = Regex("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*|`(.+?)`")
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
            else -> {
                builder.pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.25f), textDecoration = TextDecoration.None))
                builder.append(match.groups[3]!!.value)
                builder.pop()
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) builder.append(text.substring(cursor))
}
