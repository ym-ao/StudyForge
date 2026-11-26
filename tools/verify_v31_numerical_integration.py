#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
ui = (root / 'app/src/main/java/com/studyforge/app/ui/StudyScreens.kt').read_text()
rules = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateAttemptRules.kt').read_text()
tests = (root / 'app/src/test/java/com/studyforge/app/data/NumericalTemplateAttemptRulesTest.kt').read_text()
checks = {
    'Study loads dedicated numerical template blocks': 'NumericalTemplateCodec.fromContentBlocks(q.id, activeQuestionBlocks)' in ui,
    'Only verified templates opt into generated Study questions': 'takeIf { it.authoringStatus == "VERIFIED" }' in ui,
    'Study displays generated prompt and explanation': 'val displayPrompt = numericalVariant?.prompt' in ui and 'val displayExplanation = numericalVariant?.explanation' in ui,
    'Study grading uses tolerance and unit-aware engine': 'NumericalTemplateEngine.isCorrect(numericalTemplate, numericalVariant, response)' in ui,
    'Exam loads template content blocks': 'examContentBlocks = questions.filter { it.type == "Numerical" }.associate' in ui,
    'Exam grading uses generated attempt variant': 'NumericalTemplateEngine.isCorrect(template, variant, response)' in ui,
    'Exam attempt snapshot records seed and generated answer': 'numericalAttemptSeed' in ui and 'correctAnswer' in ui,
    'Stable attempt key reconstructs seed': 'studyforge-numerical-attempt|' in rules,
    'Regression tests cover resume, isolation and units': all(x in tests for x in ['sameAttemptAndQuestionReconstructSameVariant', 'differentQuestionGetsIndependentSeed', 'gradingUsesAttemptVariantAndUnits']),
    'Legacy exact-match grading remains fallback': 'response.trim().replace(",", "") == q.answer.trim().replace(",", "")' in ui,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
raise SystemExit(0 if all(checks.values()) else 1)
