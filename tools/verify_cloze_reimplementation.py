#!/usr/bin/env python3
"""Static source-contract checks for the Cloze reimplementation; not an Android runtime test."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

rules = read("app/src/main/java/com/studyforge/app/data/ClozeRules.kt")
interchange = read("app/src/main/java/com/studyforge/app/data/QuestionInterchange.kt")
editor = read("app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt")
study = read("app/src/main/java/com/studyforge/app/ui/StudyScreens.kt")
rule_tests = read("app/src/test/java/com/studyforge/app/data/ClozeRulesTest.kt")
import_tests = read("app/src/test/java/com/studyforge/app/data/QuestionInterchangeTest.kt")
version_tests = read("app/src/test/java/com/studyforge/app/data/QuestionVersionRulesTest.kt")
room_tests = read("app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt")

assert "data class Deletion(" in rules and "fun parse(text: String)" in rules
assert "val hint: String?" in rules and "fun indices(text: String)" in rules
assert "fun mask(text: String, clozeIndex: Int" in rules
assert "MAX_PROMPT_LENGTH = 100_000" in rules
assert "pattern = Regex" not in rules
assert 'if (prompt.isBlank() || (answer.isBlank() && type != "Cloze")) null' in interchange
assert 'ClozeRules.answers(parsedRow.prompt)' in interchange
assert 'Cloze rows need at least one valid' in interchange
assert 'ClozeRules.mask(prompt)' in editor
assert 'remember(q.id, q.prompt) { ClozeRules.answers(q.prompt) }' in study
assert 'remember(q.id, q.prompt) { ClozeRules.mask(q.prompt) }' in study
for case in (
    "masksAndExtractsMultipleClozeDeletions",
    "parsesHintsAndReturnsDistinctIndicesInAppearanceOrder",
    "supportsMultilineAnswersAndCaseInsensitiveMarker",
    "malformedOrEmptyMarkersArePreservedAndDoNotCountAsCloze",
    "longInputIsRejectedSafely",
):
    assert case in rule_tests, f"Missing Cloze parser regression: {case}"
assert "clozeCsvImportValidatesMarkupAndDerivesAnswerFromPrompt" in import_tests
assert "clozeCsvRoundTripsWithTheDeletionTextAsTheCanonicalAnswer" in import_tests
assert "clozeQuestionSnapshotRestoresMarkersAndCollectionMembership" in version_tests
assert "clozeQuestionCanBeVersionedAndAddedToCollection" in room_tests
assert "Collection update failed:" in editor and "Could not save question:" in editor

print("PASS: bounded Cloze parser, indexed masking, hint parsing, and malformed-input safety are present")
print("PASS: CSV import validates Cloze content and derives canonical answers from source markers")
print("PASS: Study/editor Cloze display uses cached masked text and parsed answers")
print("PASS: parser, CSV, version-history, and Room collection regression coverage is wired")
print("LIMIT: static source contract only; Android unit/instrumentation and physical-device tests are still required")
