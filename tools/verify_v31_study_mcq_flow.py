#!/usr/bin/env python3
"""Source-contract regression checks for generated numerical MCQs in Study."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = (root / 'app/src/main/java/com/studyforge/app/ui/StudyScreens.kt').read_text()
engine = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateEngine.kt').read_text()
checks = {
    'Study only loads verified numerical templates': 'it.authoringStatus == "VERIFIED"' in ui,
    'Study variant seed uses queue fingerprint and question ID': 'NumericalTemplateAttemptRules.generate(it, studyAttemptKey, q.id)' in ui,
    'Study MCQ options are generated from the same stable seed': 'NumericalTemplateEngine.generateMultipleChoice(it, NumericalTemplateAttemptRules.seedFor(studyAttemptKey, q.id))' in ui,
    'Study renders generated numerical choices as selectable cards': 'if (numericalMcq != null)' in ui and 'selectedAnswer = option.label' in ui,
    'Study requires an answer before checking': 'enabled = !selectedAnswer.isNullOrBlank()' in ui,
    'Study checks numerical answers with tolerance-aware engine': 'NumericalTemplateEngine.isCorrect(numericalTemplate, numericalVariant, response)' in ui,
    'Study reveals generated answer after checking': 'RichContentText("Answer: $displayNumericalAnswer"' in ui,
    'Study shows generated explanation after reveal/check': 'RichContentText(displayExplanation)' in ui,
    'Study resets selected choice when queue changes': 'LaunchedEffect(mode, selectedTopic, activeSessionPresetId, queueFingerprint)' in ui and 'selectedAnswer = null' in ui,
    'MCQ distractors are rejected when within accepted tolerance': 'abs(it - question.answer) > tolerance' in engine,
}
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'} {name}")
print(f"RESULT {sum(checks.values())}/{len(checks)}")
if not all(checks.values()):
    raise SystemExit(1)
