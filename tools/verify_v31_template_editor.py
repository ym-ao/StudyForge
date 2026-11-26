from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = (root / 'app/src/main/java/com/studyforge/app/ui/NumericalTemplateEditor.kt').read_text()
app = (root / 'app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt').read_text()
engine = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateEngine.kt').read_text()
codec = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateCodec.kt').read_text()
tests = (root / 'app/src/test/java/com/studyforge/app/data/NumericalTemplateEngineTest.kt').read_text()
checks = {
    'numerical editor integrated in question editor': 'NumericalTemplateEditor(existingTemplatePayload)' in app,
    'question save blocks invalid template drafts': 'type == "Numerical" && numericalTemplateError != null' in app,
    'variable and constraint authoring': 'Variables' in ui and 'Constraints (one expression per line)' in ui,
    'formula and units authoring': 'Answer formula' in ui and 'Answer unit' in ui,
    'seeded variant preview': 'NumericalTemplateEngine.generate(t, seedText.toLong())' in ui,
    'draft and verified states': 'status = "DRAFT"' in ui and 'status = "VERIFIED"' in ui,
    'status persisted in template codec': 'authoringStatus' in codec,
    'status constrained by engine': 'template.authoringStatus !in setOf("DRAFT", "VERIFIED")' in engine,
    'status round-trip regression test': 'templateAuthoringStatusRoundTripsAndRejectsUnknownStatus' in tests,
}
for name, passed in checks.items(): print(('PASS' if passed else 'FAIL') + ' ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
if not all(checks.values()): raise SystemExit(1)
