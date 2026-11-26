package com.studyforge.app.data

/** Reconstructs the same generated variant for a question within a stable study/exam attempt. */
object NumericalTemplateAttemptRules {
    fun seedFor(attemptKey: String, questionId: String): Long {
        require(attemptKey.isNotBlank() && questionId.isNotBlank())
        // FNV-1a 64-bit over a namespaced key; deterministic across process/device restarts.
        var hash = -0x340d631b7bdddcdbL
        ("studyforge-numerical-attempt|$attemptKey|$questionId").forEach { char ->
            hash = hash xor char.code.toLong()
            hash *= 0x100000001b3L
        }
        return hash
    }

    fun generate(template: NumericalTemplateEngine.Template, attemptKey: String, questionId: String): Result<NumericalTemplateEngine.Variant> =
        NumericalTemplateEngine.generate(template, seedFor(attemptKey, questionId))

    fun isCorrect(
        template: NumericalTemplateEngine.Template,
        attemptKey: String,
        questionId: String,
        response: String,
        responseUnit: String = ""
    ): Boolean = generate(template, attemptKey, questionId).getOrNull()?.let {
        NumericalTemplateEngine.isCorrect(template, it, response, responseUnit)
    } ?: false
}
