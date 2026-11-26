package com.studyforge.app.data

import org.junit.Assert.*
import org.junit.Test

class NumericalTemplateEngineTest {
    private fun template() = NumericalTemplateEngine.Template(
        id = "speed-distance-v1",
        prompt = "A vehicle travels {distance} km in {time} h. Find its speed.",
        explanation = "Use speed = {distance} / {time}.",
        variables = listOf(
            NumericalTemplateEngine.Variable("distance", 100.0, 100.0, unit = "km"),
            NumericalTemplateEngine.Variable("time", 2.0, 2.0, unit = "h")
        ),
        constraints = listOf("time > 0", "distance >= 0"),
        answerExpression = "distance / time",
        answerUnit = "km/h",
        absoluteTolerance = 0.01
    )

    @Test fun sameSeedAndTemplateVersionReproduceExactVariant() {
        val t = template()
        val first = NumericalTemplateEngine.generate(t, 1234L).getOrThrow()
        val second = NumericalTemplateEngine.generate(t, 1234L).getOrThrow()
        assertEquals(first, second)
        assertEquals(50.0, first.answer, 0.0)
        assertTrue(first.prompt.contains("100 km"))
    }

    @Test fun constraintsRejectInvalidGeneratedVariant() {
        val t = template().copy(variables = listOf(
            NumericalTemplateEngine.Variable("distance", 1.0, 1.0),
            NumericalTemplateEngine.Variable("time", 0.0, 0.0)
        ))
        assertTrue(NumericalTemplateEngine.generate(t, 7L).isFailure)
    }

    @Test fun evaluatorSupportsArithmeticPrecedenceAndComparisons() {
        val t = template()
        val result = NumericalTemplateEngine.generate(t, 1L).getOrThrow()
        assertEquals(50.0, result.answer, 0.0)
        assertTrue(NumericalTemplateEngine.validate(t).valid)
    }

    @Test fun divisionByZeroAndUnknownVariablesFailSafely() {
        val t = template()
        assertTrue(NumericalTemplateEngine.generate(t.copy(answerExpression = "distance / (time - 2)"), 1L).isFailure)
        assertFalse(NumericalTemplateEngine.validate(t.copy(answerExpression = "secret + 1")).valid)
    }

    @Test fun unitConversionAndToleranceAreApplied() {
        val t = template()
        val variant = NumericalTemplateEngine.generate(t, 1L).getOrThrow()
        assertTrue(NumericalTemplateEngine.isCorrect(t, variant, "50000", "m/h"))
        assertFalse(NumericalTemplateEngine.isCorrect(t, variant, "51000", "m/h"))
        assertFalse(NumericalTemplateEngine.isCorrect(t, variant, "50", "kg"))
    }

    @Test fun distractorsAreStableDistinctAndAvoidAnswerTolerance() {
        val first = NumericalTemplateEngine.deterministicDistractors(42.0, 99L, 4, 0.01)
        val second = NumericalTemplateEngine.deterministicDistractors(42.0, 99L, 4, 0.01)
        assertEquals(first, second)
        assertEquals(4, first.toSet().size)
        assertTrue(first.all { kotlin.math.abs(it - 42.0) > 0.01 })
    }

    @Test fun numericalTemplateCodecRoundTripsAndAttachesToDedicatedContentBlock() {
        val original = template()
        val payload = NumericalTemplateCodec.encode(original)
        val decoded = NumericalTemplateCodec.decode(payload).getOrThrow()
        assertEquals(original, decoded)
        val block = NumericalTemplateCodec.toContentBlock("question-1", original)
        assertEquals(NumericalTemplateCodec.CONTENT_BLOCK_KIND, block.kind)
        assertEquals(original, NumericalTemplateCodec.fromContentBlocks("question-1", listOf(block)).getOrThrow())
    }

    @Test fun numericalTemplateCodecRejectsUnknownVersionsAndOversizedPayloads() {
        assertTrue(NumericalTemplateCodec.decode("{}").isFailure)
        assertTrue(NumericalTemplateCodec.decode("x".repeat(NumericalTemplateCodec.MAX_PAYLOAD_CHARS + 1)).isFailure)
    }

    @Test fun numericalTemplateCodecRejectsInvalidFormulaAndMissingTemplateBlock() {
        val invalid = template().copy(answerExpression = "system.exit(0)")
        assertTrue(runCatching { NumericalTemplateCodec.encode(invalid) }.isFailure)
        assertTrue(NumericalTemplateCodec.fromContentBlocks("question-1", emptyList()).isFailure)
    }

    @Test fun templateAuthoringStatusRoundTripsAndRejectsUnknownStatus() {
        val verified = template().copy(authoringStatus = "VERIFIED")
        assertEquals("VERIFIED", NumericalTemplateCodec.decode(NumericalTemplateCodec.encode(verified)).getOrThrow().authoringStatus)
        val legacyPayload = org.json.JSONObject(NumericalTemplateCodec.encode(template())).apply { remove("authoringStatus") }.toString()
        assertEquals("DRAFT", NumericalTemplateCodec.decode(legacyPayload).getOrThrow().authoringStatus)
        assertFalse(NumericalTemplateEngine.validate(template().copy(authoringStatus = "PUBLISHED")).valid)
    }

    @Test fun formattingSupportsDecimalPlacesAndSignificantFigures() {
        assertEquals("12.35", NumericalTemplateEngine.format(12.345, 2, 0))
        assertEquals("1200", NumericalTemplateEngine.format(1234.0, 2, 2))
    }

    @Test fun generatedMcqIsStableUnitConsistentAndHasOneCorrectChoice() {
        val verified = template().copy(authoringStatus = "VERIFIED")
        val first = NumericalTemplateEngine.generateMultipleChoice(verified, 777L).getOrThrow()
        val second = NumericalTemplateEngine.generateMultipleChoice(verified, 777L).getOrThrow()
        assertEquals(first, second)
        assertEquals(4, first.options.size)
        assertEquals(1, first.options.count { it.isCorrect })
        assertEquals(first.correctIndex, first.options.indexOfFirst { it.isCorrect })
        assertTrue(first.options.all { it.label.endsWith("km/h") })
        assertEquals(4, first.options.map { it.label }.toSet().size)
        assertTrue(first.options.filterNot { it.isCorrect }.all { kotlin.math.abs(it.value - first.question.answer) > .01 })
    }

    @Test fun mcqFailsSafelyWhenFormattedChoicesCannotBeSeparated() {
        val t = template().copy(decimalPlaces = 0, absoluteTolerance = 1.0)
        // A tiny answer and large tolerance leave no safe distinct distractor set.
        val tiny = t.copy(answerExpression = "0.01", absoluteTolerance = 1000.0)
        assertTrue(NumericalTemplateEngine.generateMultipleChoice(tiny, 9L).isFailure)
    }

    @Test fun toleranceBoundaryAndEquivalentExpressionsAreHonored() {
        val t = template().copy(absoluteTolerance = 0.5, acceptedEquivalentExpressions = listOf("(distance * 5) / (time * 5)"))
        val variant = NumericalTemplateEngine.generate(t, 4L).getOrThrow()
        assertTrue(NumericalTemplateEngine.isCorrect(t, variant, "50.5"))
        assertFalse(NumericalTemplateEngine.isCorrect(t, variant, "50.5001"))
        assertTrue(NumericalTemplateEngine.isCorrect(t, variant, "50"))
    }

    @Test fun constraintFailuresRemainExplicitAndFormattingIsDeterministic() {
        val invalid = template().copy(constraints = listOf("time < 0"))
        assertTrue(NumericalTemplateEngine.generate(invalid, 123L).isFailure)
        assertEquals("0.00123", NumericalTemplateEngine.format(0.001234, 5, 0))
        assertEquals("1200", NumericalTemplateEngine.format(1234.0, 2, 2))
    }

}
