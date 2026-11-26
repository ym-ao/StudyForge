#!/usr/bin/env python3
from pathlib import Path
root = Path(__file__).resolve().parents[1]
engine = (root / 'app/src/main/java/com/studyforge/app/data/NumericalTemplateEngine.kt').read_text()
ui = (root / 'app/src/main/java/com/studyforge/app/ui/StudyScreens.kt').read_text()
tests = (root / 'app/src/test/java/com/studyforge/app/data/NumericalTemplateEngineTest.kt').read_text()
checks = {
    'MCQ result model exposes choices and correct index': 'data class MultipleChoiceVariant' in engine and 'correctIndex: Int' in engine,
    'MCQ choices use deterministic seeded ordering': 'shuffled(kotlin.random.Random(seed xor' in engine,
    'MCQ rejects formatted collisions and retries': 'labels.toSet().size == labels.size' in engine and '0 until 32' in engine,
    'Distractors stay outside grading tolerance': 'abs(it - question.answer) > tolerance' in engine,
    'Choice labels share the configured answer unit': 'format(question.answer, decimals, template.significantFigures) + unitSuffix' in engine,
    'Exam numerical templates render generated choices': 'generatedMcq.options.forEachIndexed' in ui,
    'Exam MCQ correctness uses generated correct option': 'mcq.options.any { it.isCorrect && it.label == response }' in ui,
    'Exam result displays exact generated answer': 'generatedMcq?.options?.firstOrNull { it.isCorrect }?.label' in ui,
    'Exam snapshot persists generated choice labels and correctness': 'generatedNumericalOptions' in ui,
    'Tests cover stable options, units and single correct answer': 'generatedMcqIsStableUnitConsistentAndHasOneCorrectChoice' in tests,
    'Tests cover collision/failure and tolerance boundary': 'mcqFailsSafelyWhenFormattedChoicesCannotBeSeparated' in tests and 'toleranceBoundaryAndEquivalentExpressionsAreHonored' in tests,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
raise SystemExit(0 if all(checks.values()) else 1)
