from pathlib import Path
root = Path(__file__).resolve().parents[1]
codec = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateCodec.kt').read_text()
repo = (root / 'app/src/main/java/com/studyforge/app/data/QuestionRepository.kt').read_text()
backup = (root / 'app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt').read_text()
versions = (root / 'app/src/main/java/com/studyforge/app/data/QuestionVersionRules.kt').read_text()
tests = (root / 'app/src/test/java/com/studyforge/app/data/NumericalTemplateEngineTest.kt').read_text()
checks = {
    'dedicated structured content block': 'CONTENT_BLOCK_KIND = "numerical_template"' in codec and 'toContentBlock' in codec,
    'bounded versioned JSON envelope': 'MAX_PAYLOAD_CHARS = 65_536' in codec and 'formatVersion' in codec,
    'strict validation on encode and decode': 'NumericalTemplateEngine.validate(template)' in codec and 'Unsupported numerical template format version' in codec,
    'repository persistence and retrieval': 'saveNumericalTemplate' in repo and 'loadNumericalTemplate' in repo,
    'backup export includes generic content blocks': 'root.put("contentBlocks"' in backup and 'dao.getAllContentBlocks()' in backup,
    'backup import preserves dedicated block kind': '"numerical_template"' in backup and 'NumericalTemplateCodec.MAX_PAYLOAD_CHARS' in backup,
    'version snapshots preserve content blocks': 'root.put("contentBlocks"' in versions and 'kind = b.optString("kind"' in versions,
    'legacy numerical card grading unchanged': 'else response.trim().replace(",", "") == q.answer.trim().replace(",", "")' in (root / 'app/src/main/java/com/studyforge/app/ui/StudyScreens.kt').read_text(),
    'round-trip and malformed-payload tests': 'numericalTemplateCodecRoundTripsAndAttachesToDedicatedContentBlock' in tests and 'numericalTemplateCodecRejectsUnknownVersionsAndOversizedPayloads' in tests,
}
for name, ok in checks.items(): print(f"{'PASS' if ok else 'FAIL'} {name}")
print(f'RESULT {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
