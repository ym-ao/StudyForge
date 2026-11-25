#!/usr/bin/env python3
"""Static guard for safe embedded-image import in schema-15 JSON backups."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
rules = (ROOT / "app/src/main/java/com/studyforge/app/data/MediaImportRules.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/MediaImportRulesTest.kt").read_text(encoding="utf-8")

assert "MediaImportRules.sanitizeBase64Image(o.optString(\"mediaBase64\"))" in ui
assert "MAX_IMAGE_BYTES = 1_500_000" in rules
assert "Base64.getDecoder().decode(encoded)" in rules
assert "decoded.size > maxBytes" in rules
for name in (
    "validImagePayloadIsPreservedInCanonicalBase64",
    "malformedBase64IsRejected",
    "emptyPayloadIsRejected",
    "payloadOverDecodedByteLimitIsRejected",
    "encodedLengthIsRejectedBeforeDecodingWhenOverLimit",
):
    assert name in tests, f"Missing regression test: {name}"

print("PASS: backup image import validates Base64 and enforces the 1.5 MB decoded limit")
print("LIMIT: this is a source contract check; Android JSON backup/restore has not run")
