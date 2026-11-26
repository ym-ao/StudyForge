package com.studyforge.app.data

import java.util.Locale
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow

/** Pure, deterministic numerical-template primitives. Expressions are parsed; never evaluated as code. */
object NumericalTemplateEngine {
    const val CURRENT_TEMPLATE_VERSION = 1

    data class Variable(
        val name: String,
        val minimum: Double,
        val maximum: Double,
        val decimals: Int = 0,
        val unit: String = ""
    )

    data class Template(
        val id: String,
        val version: Int = CURRENT_TEMPLATE_VERSION,
        val prompt: String,
        val explanation: String = "",
        val variables: List<Variable>,
        val constraints: List<String> = emptyList(),
        val answerExpression: String,
        val answerUnit: String = "",
        val absoluteTolerance: Double = 0.0,
        val relativeTolerance: Double = 0.0,
        val decimalPlaces: Int = 2,
        val significantFigures: Int = 0,
        val acceptedEquivalentExpressions: List<String> = emptyList(),
        val authoringStatus: String = "DRAFT"
    )

    data class Variant(
        val templateId: String,
        val templateVersion: Int,
        val seed: Long,
        val values: Map<String, Double>,
        val answer: Double,
        val prompt: String,
        val explanation: String,
        val answerUnit: String
    )

    data class MultipleChoiceOption(val value: Double, val label: String, val isCorrect: Boolean)
    data class MultipleChoiceVariant(val question: Variant, val options: List<MultipleChoiceOption>, val correctIndex: Int)

    data class Validation(val errors: List<String>) { val valid: Boolean get() = errors.isEmpty() }

    fun validate(template: Template): Validation {
        val errors = mutableListOf<String>()
        if (template.id.isBlank()) errors += "Template ID is required."
        if (template.version <= 0) errors += "Template version must be positive."
        if (template.authoringStatus !in setOf("DRAFT", "VERIFIED")) errors += "Template status must be Draft or Verified."
        if (template.prompt.isBlank()) errors += "Prompt template is required."
        if (template.answerExpression.isBlank()) errors += "Answer formula is required."
        if (template.variables.size > 32) errors += "A template may define at most 32 variables."
        val names = mutableSetOf<String>()
        template.variables.forEach { v ->
            if (!v.name.matches(Regex("[A-Za-z][A-Za-z0-9_]{0,31}"))) errors += "Invalid variable name '${v.name}'."
            if (!names.add(v.name)) errors += "Duplicate variable '${v.name}'."
            if (!v.minimum.isFinite() || !v.maximum.isFinite() || v.minimum > v.maximum) errors += "Variable '${v.name}' has invalid bounds."
            if (v.decimals !in 0..8) errors += "Variable '${v.name}' decimal places must be between 0 and 8."
            if (!UnitRegistry.isSupported(v.unit)) errors += "Unsupported variable unit '${v.unit}'."
        }
        if (!UnitRegistry.isSupported(template.answerUnit)) errors += "Unsupported answer unit '${template.answerUnit}'."
        if (!template.absoluteTolerance.isFinite() || template.absoluteTolerance < 0.0) errors += "Absolute tolerance must be finite and non-negative."
        if (!template.relativeTolerance.isFinite() || template.relativeTolerance !in 0.0..1.0) errors += "Relative tolerance must be between 0 and 1."
        if (template.decimalPlaces !in 0..10) errors += "Decimal places must be between 0 and 10."
        if (template.significantFigures !in 0..10) errors += "Significant figures must be between 0 and 10."
        val known = names
        (listOf(template.answerExpression) + template.constraints + template.acceptedEquivalentExpressions).forEach { expression ->
            try {
                val refs = Expression.references(expression)
                val unknown = refs - known - setOf("pi", "e")
                if (unknown.isNotEmpty()) errors += "Unknown variable(s): ${unknown.sorted().joinToString()}"
                Expression.evaluate(expression, emptyMap(), allowMissing = true)
            } catch (e: IllegalArgumentException) { errors += (e.message ?: "Invalid expression.") }
        }
        return Validation(errors.distinct())
    }

    fun generate(template: Template, seed: Long): Result<Variant> = runCatching {
        val validation = validate(template)
        require(validation.valid) { validation.errors.joinToString(" ") }
        val random = Random(seed)
        val values = linkedMapOf<String, Double>()
        template.variables.forEach { variable ->
            val raw = if (variable.minimum == variable.maximum) variable.minimum else
                variable.minimum + random.nextDouble() * (variable.maximum - variable.minimum)
            val scale = 10.0.pow(variable.decimals)
            val rounded = kotlin.math.round(raw * scale) / scale
            require(rounded.isFinite()) { "Generated value for '${variable.name}' overflowed." }
            values[variable.name] = rounded
        }
        template.constraints.forEachIndexed { index, constraint ->
            require(Expression.evaluate(constraint, values) != 0.0) { "Generated variant violates constraint ${index + 1}." }
        }
        val answer = Expression.evaluate(template.answerExpression, values)
        require(answer.isFinite()) { "Answer formula produced a non-finite result." }
        val rendered = template.variables.associate { variable ->
            variable.name to (format(values.getValue(variable.name), 10, 0) + variable.unit.let { if (it.isBlank()) "" else " $it" })
        }
        Variant(template.id, template.version, seed, values.toMap(), answer,
            substitute(template.prompt, rendered), substitute(template.explanation, rendered), template.answerUnit)
    }

    fun isCorrect(template: Template, variant: Variant, response: String, responseUnit: String = ""): Boolean {
        if (response.isBlank()) return false
        val parsed = parseNumber(response) ?: return false
        val converted = UnitRegistry.convert(parsed.first, responseUnit.ifBlank { template.answerUnit }, template.answerUnit) ?: return false
        val allowed = maxOf(template.absoluteTolerance, abs(variant.answer) * template.relativeTolerance)
        if (abs(converted - variant.answer) <= allowed) return true
        return template.acceptedEquivalentExpressions.any { expression ->
            val equivalent = runCatching { Expression.evaluate(expression, variant.values) }.getOrNull()
            equivalent != null && abs(converted - equivalent) <= allowed
        }
    }

    /** Builds a reproducible MCQ set. Values share the answer unit and formatted labels must be unique. */
    fun generateMultipleChoice(
        template: Template,
        seed: Long,
        optionCount: Int = 4
    ): Result<MultipleChoiceVariant> = runCatching {
        require(optionCount in 2..6) { "MCQ option count must be between 2 and 6." }
        val question = generate(template, seed).getOrThrow()
        val tolerance = maxOf(template.absoluteTolerance, abs(question.answer) * template.relativeTolerance)
        val decimals = template.decimalPlaces
        val distractorCount = optionCount - 1
        var distractors: List<Double>? = null
        // Retry with independent deterministic streams when rounded labels collide.
        for (attempt in 0 until 32) {
            val candidates = deterministicDistractors(question.answer, seed + attempt * 104729L, distractorCount, tolerance)
            val labels = (candidates + question.answer).map { format(it, decimals, template.significantFigures) }
            if (labels.toSet().size == labels.size && candidates.all { abs(it - question.answer) > tolerance }) {
                distractors = candidates
                break
            }
        }
        val selected = requireNotNull(distractors) { "Could not create distinct formatted MCQ choices." }
        val unitSuffix = template.answerUnit.let { if (it.isBlank()) "" else " $it" }
        val options = (selected.map { MultipleChoiceOption(it, format(it, decimals, template.significantFigures) + unitSuffix, false) } +
            MultipleChoiceOption(question.answer, format(question.answer, decimals, template.significantFigures) + unitSuffix, true))
            .shuffled(kotlin.random.Random(seed xor 0x4D43515F53464F52L))
        MultipleChoiceVariant(question, options, options.indexOfFirst { it.isCorrect })
    }

    fun format(value: Double, decimalPlaces: Int, significantFigures: Int): String {
        require(value.isFinite()) { "Cannot format a non-finite number." }
        val rounded = if (significantFigures > 0 && value != 0.0) {
            val scale = 10.0.pow(significantFigures - 1 - kotlin.math.floor(kotlin.math.log10(abs(value))).toInt())
            kotlin.math.round(value * scale) / scale
        } else {
            val scale = 10.0.pow(decimalPlaces.coerceIn(0, 10))
            kotlin.math.round(value * scale) / scale
        }
        return String.format(Locale.US, "%s", rounded).let { if (it.endsWith(".0")) it.dropLast(2) else it }
    }

    fun deterministicDistractors(answer: Double, seed: Long, count: Int = 3, tolerance: Double = 0.0): List<Double> {
        require(answer.isFinite() && tolerance.isFinite() && tolerance >= 0.0)
        require(count in 1..8)
        val random = Random(seed xor 0x5DEECE66DL)
        val result = linkedSetOf<Double>()
        var attempts = 0
        while (result.size < count && attempts++ < count * 100) {
            val magnitude = maxOf(abs(answer) * (0.05 + random.nextDouble() * 0.5), 1.0)
            val sign = if (random.nextBoolean()) 1.0 else -1.0
            val candidate = answer + sign * magnitude
            if (candidate.isFinite() && abs(candidate - answer) > tolerance) result += candidate
        }
        require(result.size == count) { "Could not generate enough distinct distractors." }
        return result.toList()
    }

    private fun substitute(source: String, values: Map<String, String>): String =
        Regex("\\{([A-Za-z][A-Za-z0-9_]*)}").replace(source) { match -> values[match.groupValues[1]] ?: match.value }

    private fun parseNumber(raw: String): Pair<Double, String>? {
        val match = Regex("^\\s*([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)\\s*(.*?)\\s*$").matchEntire(raw) ?: return null
        val value = match.groupValues[1].toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
        return value to match.groupValues[2].trim()
    }

    private object UnitRegistry {
        private data class Unit(val dimension: String, val factor: Double)
        private val units = mapOf(
            "" to Unit("scalar", 1.0), "m" to Unit("length", 1.0), "cm" to Unit("length", .01), "mm" to Unit("length", .001), "km" to Unit("length", 1000.0),
            "s" to Unit("time", 1.0), "min" to Unit("time", 60.0), "h" to Unit("time", 3600.0),
            "kg" to Unit("mass", 1.0), "g" to Unit("mass", .001), "mg" to Unit("mass", .000001),
            "l" to Unit("volume", 1.0), "ml" to Unit("volume", .001), "k" to Unit("temperature", 1.0),
            "pa" to Unit("pressure", 1.0), "kpa" to Unit("pressure", 1000.0), "j" to Unit("energy", 1.0), "kj" to Unit("energy", 1000.0),
            "m/s" to Unit("speed", 1.0), "km/h" to Unit("speed", 1000.0 / 3600.0), "m/h" to Unit("speed", 1.0 / 3600.0), "cm/s" to Unit("speed", 0.01)
        )
        fun isSupported(unit: String) = unit.trim().lowercase() in units
        fun convert(value: Double, from: String, to: String): Double? {
            val a = units[from.trim().lowercase()] ?: return null
            val b = units[to.trim().lowercase()] ?: return null
            if (a.dimension != b.dimension) return null
            return (value * a.factor / b.factor).takeIf { it.isFinite() }
        }
    }

    /** Recursive-descent arithmetic parser. Whitelisted operators only; no function calls or property access. */
    private object Expression {
        private class Parser(private val source: String, private val variables: Map<String, Double>, private val allowMissing: Boolean) {
            private var index = 0
            fun parse(): Double {
                val value = comparison()
                whitespace()
                require(index == source.length) { "Unexpected token at position $index." }
                return value
            }
            private fun comparison(): Double {
                val left = expression()
                whitespace()
                return when {
                    takePair('<', '=') -> if (left <= expression()) 1.0 else 0.0
                    takePair('>', '=') -> if (left >= expression()) 1.0 else 0.0
                    takePair('=', '=') -> if (left == expression()) 1.0 else 0.0
                    takePair('!', '=') -> if (left != expression()) 1.0 else 0.0
                    take('<') -> if (left < expression()) 1.0 else 0.0
                    take('>') -> if (left > expression()) 1.0 else 0.0
                    else -> left
                }
            }
            private fun expression(): Double {
                var value = term()
                while (true) { whitespace(); value = when { take('+') -> value + term(); take('-') -> value - term(); else -> return finite(value) } }
            }
            private fun term(): Double {
                var value = power()
                while (true) { whitespace(); value = when { take('*') -> value * power(); take('/') -> { val divisor = power(); require(divisor != 0.0) { "Division by zero." }; value / divisor }; else -> return finite(value) } }
            }
            private fun power(): Double {
                var value = unary()
                whitespace()
                if (take('^')) value = value.pow(power())
                return finite(value)
            }
            private fun unary(): Double { whitespace(); return when { take('+') -> unary(); take('-') -> -unary(); take('(') -> comparison().also { whitespace(); require(take(')')) { "Missing closing parenthesis." } }; else -> atom() } }
            private fun atom(): Double {
                whitespace()
                val start = index
                while (index < source.length && (source[index].isLetterOrDigit() || source[index] == '_' || source[index] == '.')) index++
                require(index > start) { "Expected a number or variable at position $index." }
                val token = source.substring(start, index)
                token.toDoubleOrNull()?.let { return finite(it) }
                return when (token) {
                    "pi" -> Math.PI
                    "e" -> Math.E
                    else -> variables[token] ?: if (allowMissing) 1.0 else throw IllegalArgumentException("Unknown variable '$token'.")
                }
            }
            private fun finite(value: Double): Double { require(value.isFinite()) { "Expression overflowed or produced a non-finite result." }; return value }
            private fun whitespace() { while (index < source.length && source[index].isWhitespace()) index++ }
            private fun take(char: Char): Boolean { if (index < source.length && source[index] == char) { index++; return true }; return false }
            private fun takePair(first: Char, second: Char): Boolean { if (index + 1 < source.length && source[index] == first && source[index + 1] == second) { index += 2; return true }; return false }
        }
        fun evaluate(expression: String, variables: Map<String, Double>, allowMissing: Boolean = false): Double = Parser(expression, variables, allowMissing).parse()
        fun references(expression: String): Set<String> = Regex("[A-Za-z][A-Za-z0-9_]*").findAll(expression).map { it.value }.toSet()
    }
}
