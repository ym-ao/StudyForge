package com.studyforge.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Bounded, versioned JSON interchange for numerical templates. Stored as a dedicated content block. */
object NumericalTemplateCodec {
    const val CONTENT_BLOCK_KIND = "numerical_template"
    const val MAX_PAYLOAD_CHARS = 65_536
    private const val MAX_PROMPT_CHARS = 8_000
    private const val MAX_EXPLANATION_CHARS = 12_000
    private const val MAX_EXPRESSION_CHARS = 512

    fun encode(template: NumericalTemplateEngine.Template): String {
        require(NumericalTemplateEngine.validate(template).valid) { NumericalTemplateEngine.validate(template).errors.joinToString(" ") }
        require(template.id.length <= 128 && template.prompt.length <= MAX_PROMPT_CHARS && template.explanation.length <= MAX_EXPLANATION_CHARS)
        require(template.answerExpression.length <= MAX_EXPRESSION_CHARS && template.answerUnit.length <= 32)
        require(template.constraints.size <= 32 && template.constraints.all { it.length <= MAX_EXPRESSION_CHARS })
        require(template.acceptedEquivalentExpressions.size <= 16 && template.acceptedEquivalentExpressions.all { it.length <= MAX_EXPRESSION_CHARS })
        val root = JSONObject()
            .put("format", "studyforge-numerical-template")
            .put("formatVersion", 1)
            .put("id", template.id)
            .put("templateVersion", template.version)
            .put("prompt", template.prompt)
            .put("explanation", template.explanation)
            .put("variables", JSONArray().apply { template.variables.forEach { v -> put(JSONObject()
                .put("name", v.name).put("minimum", v.minimum).put("maximum", v.maximum)
                .put("decimals", v.decimals).put("unit", v.unit)) } })
            .put("constraints", JSONArray(template.constraints))
            .put("answerExpression", template.answerExpression)
            .put("answerUnit", template.answerUnit)
            .put("absoluteTolerance", template.absoluteTolerance)
            .put("relativeTolerance", template.relativeTolerance)
            .put("decimalPlaces", template.decimalPlaces)
            .put("significantFigures", template.significantFigures)
            .put("acceptedEquivalentExpressions", JSONArray(template.acceptedEquivalentExpressions))
            .put("authoringStatus", template.authoringStatus)
        val payload = root.toString()
        require(payload.length <= MAX_PAYLOAD_CHARS) { "Numerical template payload is too large." }
        return payload
    }

    fun decode(payload: String): Result<NumericalTemplateEngine.Template> = runCatching {
        require(payload.length in 2..MAX_PAYLOAD_CHARS) { "Numerical template payload size is invalid." }
        val root = JSONObject(payload)
        require(root.optString("format") == "studyforge-numerical-template") { "Unknown numerical template format." }
        require(root.optInt("formatVersion", -1) == 1) { "Unsupported numerical template format version." }
        fun boundedString(key: String, max: Int): String = root.optString(key).also {
            require(it.length <= max) { "Field '$key' is too long." }
        }
        val variablesJson = root.optJSONArray("variables") ?: JSONArray()
        require(variablesJson.length() in 0..32) { "A template must contain 1 to 32 variables." }
        val variables = (0 until variablesJson.length()).map { i ->
            val v = variablesJson.getJSONObject(i)
            NumericalTemplateEngine.Variable(
                name = v.getString("name"), minimum = v.getDouble("minimum"), maximum = v.getDouble("maximum"),
                decimals = v.optInt("decimals", 0), unit = v.optString("unit")
            )
        }
        fun stringArray(key: String, maxItems: Int, maxLength: Int): List<String> {
            val array = root.optJSONArray(key) ?: JSONArray()
            require(array.length() <= maxItems) { "Too many values in '$key'." }
            return (0 until array.length()).map { array.getString(it).also { value -> require(value.length <= maxLength) { "An item in '$key' is too long." } } }
        }
        val template = NumericalTemplateEngine.Template(
            id = boundedString("id", 128), version = root.optInt("templateVersion", 0),
            prompt = boundedString("prompt", MAX_PROMPT_CHARS), explanation = boundedString("explanation", MAX_EXPLANATION_CHARS),
            variables = variables, constraints = stringArray("constraints", 32, MAX_EXPRESSION_CHARS),
            answerExpression = boundedString("answerExpression", MAX_EXPRESSION_CHARS),
            answerUnit = boundedString("answerUnit", 32),
            absoluteTolerance = root.optDouble("absoluteTolerance", Double.NaN),
            relativeTolerance = root.optDouble("relativeTolerance", Double.NaN),
            decimalPlaces = root.optInt("decimalPlaces", -1), significantFigures = root.optInt("significantFigures", -1),
            acceptedEquivalentExpressions = stringArray("acceptedEquivalentExpressions", 16, MAX_EXPRESSION_CHARS),
            authoringStatus = root.optString("authoringStatus", "DRAFT")
        )
        val validation = NumericalTemplateEngine.validate(template)
        require(validation.valid) { validation.errors.joinToString(" ") }
        template
    }

    fun toContentBlock(questionId: String, template: NumericalTemplateEngine.Template, position: Int = 0, blockId: String = template.id): QuestionContentBlockEntity {
        require(questionId.isNotBlank() && position >= 0 && blockId.isNotBlank())
        return QuestionContentBlockEntity(
            id = blockId, questionId = questionId, position = position,
            kind = CONTENT_BLOCK_KIND, text = encode(template)
        )
    }

    fun fromContentBlocks(questionId: String, blocks: List<QuestionContentBlockEntity>): Result<NumericalTemplateEngine.Template> {
        val matching = blocks.filter { it.questionId == questionId && it.kind == CONTENT_BLOCK_KIND }
        if (matching.size != 1) return Result.failure(IllegalArgumentException("Expected exactly one numerical template block."))
        return decode(matching.single().text)
    }
}
