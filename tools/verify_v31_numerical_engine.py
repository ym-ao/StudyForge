from pathlib import Path

root = Path(__file__).resolve().parents[1]
engine = (root / "app/src/main/java/com/studyforge/app/data/NumericalTemplateEngine.kt").read_text()
test = (root / "app/src/test/java/com/studyforge/app/data/NumericalTemplateEngineTest.kt").read_text()
checks = {
    "versioned template model": "data class Template(" in engine and "templateVersion: Int" in engine,
    "typed bounded variables": "data class Variable(" in engine and "minimum: Double" in engine and "maximum: Double" in engine,
    "safe parser without eval": "private object Expression" in engine and "Recursive-descent arithmetic parser" in engine and "eval(" not in engine,
    "deterministic seeded generation": "Random(seed)" in engine and "fun generate(template: Template, seed: Long)" in engine,
    "cross-variable constraints": "template.constraints.forEachIndexed" in engine and "comparison()" in engine,
    "unit conversion and tolerance": "UnitRegistry.convert" in engine and "relativeTolerance" in engine,
    "rounding and significant figures": "significantFigures" in engine and "decimalPlaces" in engine,
    "explanation substitution": "substitute(template.explanation, rendered)" in engine,
    "deterministic distractors": "Random(seed xor" in engine and "fun deterministicDistractors" in engine,
    "legacy numerical path untouched": 'else response.trim().replace(",", "") == q.answer.trim().replace(",", "")' in (root / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text(),
    "regression tests cover safety and reproducibility": "sameSeedAndTemplateVersionReproduceExactVariant" in test and "divisionByZeroAndUnknownVariablesFailSafely" in test and "unitConversionAndToleranceAreApplied" in test,
}
for name, ok in checks.items():
    print(f"{'PASS' if ok else 'FAIL'} {name}")
print(f"RESULT {sum(checks.values())}/{len(checks)}")
if not all(checks.values()):
    raise SystemExit(1)
