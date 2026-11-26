# StudyForge V3 testing and resource-use strategy

**Policy:** use resource-intensive development and testing tools deliberately. Do not run the heaviest toolchain after every small source edit. Preserve fast feedback with lightweight checks, then spend the larger build/emulator/benchmark budget at planned checkpoints, after a substantial core feature is complete, or when investigating a bug.

## 1. Testing cadence

### Every implementation batch — lightweight checks

Run relevant low-cost checks where available:

- inspect the diff and ensure the change is scoped;
- run the relevant static/source-contract verifier(s);
- run a small targeted host-side test or focused unit test if it is quick and available;
- inspect schema/version/signing/FSRS invariants when the change touches them;
- do not claim Android compilation or runtime correctness from static checks.

Do not start an emulator or run the entire instrumentation suite for a documentation-only change or a small isolated UI/text adjustment unless there is a concrete reason.

### Heavy verification triggers

Use the full or broad Android toolchain when at least one of these conditions is met:

1. A planned milestone checkpoint is reached (V3.5, V3.9, V3.10).
2. A substantial core feature or coherent feature group is complete and ready for integration verification.
3. A bug, regression, crash, migration issue, build failure, or suspicious behavior is encountered and needs diagnosis.
4. A change affects high-risk foundations: Room schema/migrations, backup/restore, FSRS/scheduling, paging/query behavior, persistence, navigation/state ownership, or release/signing.
5. Final release/acceptance verification is due.

For a bug, start with the narrowest reproducer and targeted test. Escalate to full build, broader regression tests, instrumentation, and emulator testing when the failure's scope or risk warrants it.

## 2. Planned V3 checkpoints

| Checkpoint | Purpose | Expected verification |
|---|---|---|
| **V3.5** | First broad integration checkpoint | Full debug build, lint, unit tests, relevant Room instrumentation/migration tests, regression suite, and emulator smoke tests if supported |
| **V3.9** | Second broad integration checkpoint | Repeat full suite; emphasize cross-feature regressions, data integrity, performance/query plans, migration and backup/restore |
| **V3.10** | Final V3 acceptance/release candidate | Full acceptance runbook, clean build, lint, unit + instrumentation tests, CI evidence, emulator checks, signed/update continuity checks where credentials are available, and user physical-device acceptance |

These are planned checkpoints, not permission to defer a known serious defect. If a critical bug appears before a checkpoint, investigate and test it immediately.

The current V3.0 Batch 9 handoff has its own immediate acceptance gates. This future cadence does **not** defer the current V3.0 acceptance work: follow `HANDOFF_FOR_NEW_CHAT.md` and `V3_FINAL_ACCEPTANCE_RUNBOOK.md` first.

## 3. Feature-completion gates

For each substantial core feature:

1. Finish the coherent implementation slice.
2. Add or update targeted tests alongside the feature.
3. Run lightweight checks during development.
4. When the feature is complete, run its relevant integration tests and a broader regression selection.
5. Record known gaps before starting dependent work.

Do not run the entire heavyweight suite after every tiny subtask. Equally, do not stack multiple unfinished core features and postpone all verification until the end.

## 4. Resource-aware tool selection

| Tool/check | When to use |
|---|---|
| Static Python/source verifiers | Frequently; usually each implementation batch |
| Focused Kotlin/host tests | When relevant; especially during feature development or bug diagnosis |
| Gradle compile/build | At feature integration points, checkpoints, and when build/configuration bugs arise |
| Full unit-test suite + lint | At V3.5, V3.9, V3.10 and after major core feature groups |
| Room instrumentation / migration tests | After database-related feature groups, schema changes, and milestone checkpoints |
| Android emulator | Milestones, UI/runtime integration, crash reproduction, and device-specific bug investigation |
| 10k/50k performance benchmarks | After meaningful query/data-path optimization and at performance checkpoints, not after unrelated UI edits |
| Physical phone testing | User acceptance at major milestones and final release; emulator success is not a substitute |

If the environment has limited disk, RAM, CPU, or ephemeral storage, prioritize JDK + Gradle Wrapper + required SDK/build-tools first. Add emulator images only after checking virtualization and available resources.

## 5. Stop-the-line conditions

Do not continue building dependent features on top of an unresolved high-severity issue involving:

- data loss, corruption, migration failure, or backup/restore;
- incorrect FSRS scheduling or review-state transitions;
- crashes or inability to complete a core study/exam workflow;
- security, signing, or release-update continuity;
- a failing build or broad regression that makes the source baseline unreliable.

Fix or explicitly isolate the issue, add a regression test where possible, and document any user-approved deferral.

## 6. Reporting rules

Every milestone report must state:

- what was tested and what was not;
- exact commands and environment/tool versions;
- test counts and pass/fail totals where available;
- whether results came from host, Android emulator, CI, or physical phone;
- remaining blockers and whether any were explicitly deferred.

Never describe a skipped test as passed. Never infer emulator/device success from a successful host build. Keep GitHub Actions as independent verification.

## 7. Required handoff behavior

Every new development session must read, in order:

1. `HANDOFF_FOR_NEW_CHAT.md`
2. `TOOLING.md`
3. `TESTING_STRATEGY.md`
4. `V3_FINAL_ACCEPTANCE_RUNBOOK.md` when working on V3.0 acceptance

Before using resource-intensive tools, inspect what is already installed and what the current task actually needs. Reuse verified installations and caches. Follow the cadence above rather than reflexively running the full suite after every small batch.
