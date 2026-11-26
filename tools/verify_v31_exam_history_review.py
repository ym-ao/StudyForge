#!/usr/bin/env python3
from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = (root / 'app/src/main/java/com/studyforge/app/ui/StudyScreens.kt').read_text()
checks = {
    'Exam history parses saved generated numerical choices': 'item.optJSONArray("generatedNumericalOptions")' in ui,
    'Generated choice review marks the correct option': 'isCorrect -> "✓ Correct answer"' in ui,
    'Generated choice review identifies the selected option': 'isSelected -> "✗ Your choice"' in ui,
    'Correct selected choice is explicitly identified': 'isCorrect && isSelected -> "✓ Correct • Your choice"' in ui,
    'Legacy MCQ choices remain reviewable from optionsJson': 'JSONArray(item.optString("optionsJson", "[]"))' in ui,
    'History retains expected answer and saved response': 'Text("Your answer: $response"' in ui and 'Text("Expected: $expected"' in ui,
    'History remains expandable by saved attempt ID': 'expandedAttemptId == attempt.id' in ui,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{sum(checks.values())}/{len(checks)} checks passed')
raise SystemExit(0 if all(checks.values()) else 1)
