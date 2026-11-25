package com.studyforge.app.data

/** Pure validation and ordering rules shared by the structured question editor. */
object QuestionAuthoringRules {
    fun validateBlocks(blocks: List<QuestionContentBlockEntity>): String? {
        blocks.forEachIndexed { index, block ->
            when (block.kind.uppercase()) {
                "DERIVATION" -> {
                    val issue = DerivationRules.validate(DerivationRules.parse(block.text))
                    if (issue != null) return "Derivation block ${index + 1}: $issue"
                }
                "TABLE" -> ContentBlockParsing.validateTable(block.text)?.let {
                    return "Table block ${index + 1}: $it"
                }
                "GRAPH" -> ContentBlockParsing.validateGraph(block.text)?.let {
                    return "Graph block ${index + 1}: $it"
                }
            }
        }
        return null
    }

    fun validateParts(parts: List<QuestionPartEntity>): String? {
        parts.forEachIndexed { index, part ->
            if (part.prompt.isBlank()) return "Part ${index + 1} needs a prompt."
            if (part.answer.isBlank()) return "Part ${index + 1} needs an answer."
            if (!part.points.isFinite() || part.points <= 0.0 || part.points > 10000.0) {
                return "Part ${index + 1} must have points greater than 0 and no more than 10000."
            }
            if (!part.absoluteTolerance.isFinite() || part.absoluteTolerance < 0.0 ||
                !part.relativeTolerance.isFinite() || part.relativeTolerance !in 0.0..1.0) {
                return "Part ${index + 1} has an invalid numeric tolerance."
            }
        }
        return null
    }

    fun normalizeBlocks(questionId: String, blocks: List<QuestionContentBlockEntity>): List<QuestionContentBlockEntity> =
        blocks.mapIndexed { index, block ->
            block.copy(
                questionId = questionId,
                position = index,
                kind = block.kind.trim().uppercase().take(24).ifBlank { "TEXT" },
                text = block.text.take(20000),
                altText = block.altText.take(500),
                mediaBase64 = block.mediaBase64.take(2_800_000)
            )
        }

    fun normalizeParts(questionId: String, parts: List<QuestionPartEntity>): List<QuestionPartEntity> =
        parts.mapIndexed { index, part ->
            part.copy(
                questionId = questionId,
                position = index,
                prompt = part.prompt.trim().take(10000),
                answer = part.answer.trim().take(10000),
                explanation = part.explanation.take(10000),
                points = part.points.coerceIn(0.01, 10000.0)
            )
        }
}
