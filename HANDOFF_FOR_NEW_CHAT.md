# CURRENT HANDOFF — 2026-11-27 (V3.1 Batch 7 Study MCQ regression gates)

The user requested the next update after V3.1 Batch 6. Continue from this exact source tree and this handoff.

## Implemented through Batch 6
- Integrated `VERIFIED` numerical templates into Study and timed Exam as an opt-in path.
- Study and Exam render generated prompts and substituted explanations; free-response numerical answers use tolerance-aware grading and supported unit conversion.
- Added deterministic generated-variant MCQ choices for verified numerical templates in Study and Exam. Distractors use the answer unit, avoid the accepted tolerance, reject formatted collisions, and use stable option order.
- Exam result view shows the exact generated answer; snapshots record generated prompt, expected answer, seed, template version and generated MCQ choices/correctness.
- `NumericalTemplateAttemptRules` derives stable deterministic seeds from attempt context + question ID.
- Draft templates and old Numerical cards keep legacy prompt/answer/exact-match behavior.
- Added MCQ and grading edge-case tests plus `verify_v31_numerical_mcq.py` to CI.
- Batch 6 extends expandable Progress > Exam history to show the exact generated numerical MCQ choices, selected choice and correct choice; ordinary MCQ/True-False history choices are also rendered from saved `optionsJson`. Existing snapshots without generated options remain compatible.
- Added `verify_v31_exam_history_review.py` to CI.
- Batch 7 adds `verify_v31_study_mcq_flow.py` (10 checks) to protect the existing Study numerical MCQ path: verified-template gating, stable queue/question seed, selectable choices, answer-required check, tolerance-aware grading, generated answer/explanation feedback, state reset, and distractor tolerance separation.

## Batch 7 CI failure investigation and focused fixes (2026-11-27)
- Inspected GitHub Actions run `45899413233` for commit `b5926fb72ce2990a476565bdef5c2e843f10e3da`. All Python regression checks and Android lint passed; `:app:testDebugUnitTest` failed (169 tests, 2 failures), so emulator tests and APK tasks were skipped.
- Failures: `NumericalTemplateAttemptRulesTest.gradingUsesAttemptVariantAndUnits` and `NumericalTemplateEngineTest.formattingSupportsDecimalPlacesAndSignificantFigures`.
- Root cause 1: `NumericalTemplateEngine.isCorrect` ignored a unit suffix parsed from the learner's response when no separate `responseUnit` argument was supplied. Thus a response such as `5.0 s` could be interpreted as the template's `m` unit.
- Root cause 2: binary floating-point + `kotlin.math.round` used ties-to-even for decimal formatting; `12.345` at two decimal places could render as `12.34` instead of the expected `12.35`.
- Fixed response-unit precedence: explicit `responseUnit`, then parsed response suffix, then template unit only when no unit was supplied.
- Fixed numeric formatting with `BigDecimal` + `RoundingMode.HALF_UP`, retaining trimmed plain-string output for decimal places and significant figures.
- Extended the attempt-rule test for `500 cm` conversion and explicit-unit conflict.
- Focused verification after fixes: numerical engine 11/11; numerical integration 10/10; numerical MCQ 11/11; exam-history review 7/7; Study MCQ flow 10/10. Engine + attempt helper compile with Kotlin 1.9.0; a standalone regression harness passed 7 assertions (correct/incorrect units, conversion, explicit-unit conflict, positive/negative half-up rounding, significant figures).
- GitHub Actions has not yet been rerun on these fixes. Android Gradle/JUnit, Room instrumentation, emulator, APK and physical-device verification are still pending.

## Verification and limits
- The handoff's prior baseline was 38/38 Python checks; focused post-fix verifiers listed above pass. Do not claim the entire workflow suite was rerun after the source fix.
- Numerical engine + attempt seed helper compiled independently with Kotlin 1.9.0.
- Android Gradle/Compose compilation, JUnit, Room instrumentation, emulator, APK and physical-device tests remain pending. Do not claim Android compilation or phone verification.
- Study seed is reconstructed from the active queue fingerprint + question ID. Exam seed is reconstructed from the saved exam start timestamp + question ID; completed Exam snapshots persist seed, generated prompt, expected answer and template version. No new persistent in-progress Study attempt table was added.
- Original `StudyForge_Plan.txt` SHA-256 unchanged: `8d5bebe805901cae7d243c41595458ccbe2a288d7ec49b01129f5443e4981abc`.
- No Room schema/migration, JSON backup schema, app version/versionCode, FSRS, signing or user-data changes.

## Next batch
- Run GitHub Actions for the Batch 7 source and resolve any Android/Compose/JUnit failures.
- On device, verify generated numerical MCQs in Study and Exam, answer selection/scoring, exact answer in results, and expanded Progress > Exam history choices for generated numerical MCQs and ordinary MCQs.
- Verify older saved exam attempts still open when `generatedNumericalOptions` is absent.
- Consider persistent in-progress Study attempts only if device testing demonstrates the queue fingerprint is not stable across a required resume path. Do not add a migration speculatively.
- Keep template use restricted to `VERIFIED`; do not change legacy Numerical grading by default.
