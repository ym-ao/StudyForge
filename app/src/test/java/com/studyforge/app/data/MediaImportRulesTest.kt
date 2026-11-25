package com.studyforge.app.data

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaImportRulesTest {
    @Test fun validImagePayloadIsPreservedInCanonicalBase64() {
        val original = byteArrayOf(0, 1, 2, 3, 4, 5, -1)
        val encoded = Base64.getEncoder().encodeToString(original)
        assertEquals(encoded, MediaImportRules.sanitizeBase64Image(encoded))
        assertEquals(original.toList(), Base64.getDecoder().decode(encoded).toList())
    }

    @Test fun malformedBase64IsRejected() {
        assertNull(MediaImportRules.sanitizeBase64Image("not base64!"))
        assertNull(MediaImportRules.sanitizeBase64Image("%%%"))
    }

    @Test fun emptyPayloadIsRejected() {
        assertNull(MediaImportRules.sanitizeBase64Image(""))
    }

    @Test fun payloadOverDecodedByteLimitIsRejected() {
        val encoded = Base64.getEncoder().encodeToString(ByteArray(11) { it.toByte() })
        assertNull(MediaImportRules.sanitizeBase64Image(encoded, maxBytes = 10))
    }

    @Test fun encodedLengthIsRejectedBeforeDecodingWhenOverLimit() {
        val tooLong = "A".repeat(((10 + 2) / 3) * 4 + 1)
        assertNull(MediaImportRules.sanitizeBase64Image(tooLong, maxBytes = 10))
    }
}
