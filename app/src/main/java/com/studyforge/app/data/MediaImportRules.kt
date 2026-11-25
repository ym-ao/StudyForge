package com.studyforge.app.data

import java.util.Base64

/**
 * Validates embedded image payloads before they are persisted from an imported backup.
 * The editor's image picker uses the same 1.5 MB decoded-byte limit.
 */
object MediaImportRules {
    const val MAX_IMAGE_BYTES = 1_500_000
    private const val MAX_BASE64_CHARS = ((MAX_IMAGE_BYTES + 2) / 3) * 4

    /** Returns canonical no-wrap Base64, or null for empty, malformed, or oversized data. */
    fun sanitizeBase64Image(encoded: String, maxBytes: Int = MAX_IMAGE_BYTES): String? {
        if (maxBytes <= 0 || encoded.isEmpty()) return null
        val safeCharacterLimit = ((maxBytes + 2) / 3) * 4
        if (encoded.length > safeCharacterLimit || encoded.length > MAX_BASE64_CHARS) return null
        val decoded = runCatching { Base64.getDecoder().decode(encoded) }.getOrNull() ?: return null
        if (decoded.isEmpty() || decoded.size > maxBytes) return null
        return Base64.getEncoder().encodeToString(decoded)
    }
}
