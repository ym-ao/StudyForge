# StudyForge — Version 3 Delivery Plan

**Planning baseline:** 2026-11-26  
**Status:** V3.0 implementation in progress through Batch 6; not release-ready.  
**Current source baseline:** 2.1.24 / versionCode 28 / Room schema 16 / JSON backup schema 15.  
**Source of truth:** the current source tree and this plan; the original `StudyForge_Plan.txt` remains preserved and unchanged.

## Current implementation progress — 2026-11-26

The user explicitly authorized beginning V3 work. Implementation is proceeding on the V3 planning-baseline source package. This does not mean V2 release acceptance is complete: the bottom-navigation fix still needs exact-APK confirmation, Progress/other-screen polish remains open, and final CI, populated migration, backup/restore, and signing/update checks remain release gates. V3 work is kept as source work only; no APK version or Room schema version is bumped in this batch.

### V3.0 batch 1 — data-layer foundation (implemented in source; Android build pending)

- Added Room Paging 3, Paging runtime and Compose dependencies.
- Added a lightweight `QuestionLibraryRow` projection that excludes answer, explanation, LaTeX and rich content payloads.
- Added parameterized Room paging and count queries for text, type, status, difficulty, subject, chapter, topic, tag, exam, source, date, collection and performance criteria.
- Added deterministic `updatedAt DESC, id ASC` ordering.
- Added `QuestionRepository` with 30-row pages, prefetch distance 10 and bounded 90-row loaded window.
- Added `LibraryViewModel` with criteria-driven cancellation/replacement of stale paging streams.
- Added a reusable Compose `PagedQuestionRows` renderer with stable keys and refresh/append loading, empty, error and retry states.
- Added bounded Study queue ID snapshots (maximum 500) and ordered card hydration in SQLite-safe chunks; this prevents rating-induced sort changes from shifting the active session order.
- Added Room runtime regression coverage for stable page order, DB filters, lightweight result rows, queue snapshot order and rating-time reordering.
- Kept Room schema 15 and JSON backup schema 15 unchanged. FSRS rules and existing UI flows are not changed in this batch.

### V3.0 batch 2 — production Library Paging integration (source implemented; runtime validation pending)

- Replaced the active Library destination with `PagedLibraryScreen`, which collects the `LibraryViewModel` Paging flow through `collectAsLazyPagingItems()` and renders the lightweight `QuestionLibraryRow` projection with stable IDs.
- Moved the active Library's query, type/status/difficulty, subject/chapter/topic/tag, exam/source, date, collection and performance criteria into the criteria-driven ViewModel/Room query path. Search remains debounced; changing criteria replaces the paging stream.
- Replaced in-memory saved-preset match calculations with Room `COUNT(*)` queries. Preset chips show fresh counts and presets can be applied, saved and deleted with confirmation.
- Reworked active Library selection to retain stable question IDs across loaded pages. “Select loaded rows” is explicit; status/difficulty/tag/collection bulk actions hydrate selected entities in SQLite-safe chunks and preserve per-question version snapshots and transactional membership changes.
- Edit/open, delete and manual scheduling resolve the current record by stable ID on demand; rich answer/explanation/media payloads are not part of paged rows.
- Added the `lifecycle-viewmodel-compose` dependency and kept the Room page/count query tag matching case-insensitive substring-compatible with the former Library filter. Added a Room instrumentation assertion for that behavior.
- Updated the Batch 2 readiness verifier. It now passes its source-shape checks for active production wiring, Paging collection, Room counts and ID-based selection/bulk operations.
- Restored global tag-manager create/rename/delete UI and database-backed tag usage counts; tag rename/delete load only questions containing the exact tag token. Removed the unused legacy full-list Library composable after feature parity migration. No Room schema, JSON backup schema, FSRS, versionName/versionCode, or signing identity changes.

Global tag-manager CRUD has now been restored in the paged destination. Tag usage counts and tag-to-question lookups are database-backed; create/rename/delete and bulk tag edits remain transactional at the app boundary. The root app still observes the full question list for Home/Study/Progress and other legacy consumers; this batch removes that list from the active Library destination, not from the whole app. The Study screen has not adopted bounded queue snapshots yet.

### V3.0 batch 3 — database-side bounded Study queue contract (source implemented; integration/runtime validation pending)

- Added `StudyQueueQuery`, an immutable session-start criteria object covering existing Study modes, Topic selection, saved-preset subject/chapter/topic/tag/difficulty/exam/type/status/performance filters, collection membership, and a validated 1–500 question bound.
- Added a parameterized Room query that selects only question IDs, applies supported criteria in SQLite, and orders Due, Ordered, Weakest, Random, and default modes explicitly with a stable question-ID tie-breaker. Random order is materialized once in the returned ID snapshot, so later rating updates cannot reshuffle the active snapshot.
- Added a repository entry point for the richer queue contract while retaining the original queue API for compatibility. Card hydration remains on-demand and reorders fetched entities to the snapshot ID order.
- Added Room instrumentation coverage for saved-preset filters, collection membership, Due/Ordered ordering, and Weakest selection, plus pure query-contract validation tests and a source-readiness verifier.
- No Room schema, backup schema, FSRS policy, app version, or signing identity changes.

**Boundary:** this batch establishes the database-side queue contract; the current `StudyScreen` still receives the legacy full question list and has not yet switched to this API. Session progress/process-recreation persistence, per-day Due quotas/order parity, and full Study UI integration remain open. This is not the V3.0 Study migration acceptance gate.


### V3.0 batch 5 — Home and Progress core aggregates (source implemented; Android runtime pending)

- Added compact Room aggregate projections for Home dashboard totals: question count, collection count, summed question review count, and due count. Home no longer derives those dashboard metrics by scanning the observed question list.
- Added a Room aggregate projection for Progress headline metrics: total/due/new/learning/mastered question counts; total/scored/correct reviews; 30-day scored/correct reviews; last-seven-day review count; and distinct active review days. Progress headline values now consume this projection rather than recounting the complete question and review-log lists.
- Added a Room instrumentation test source that seeds mixed New/Learning/Review records and scored logs, then asserts Home and Progress aggregate values. Added `verify_v3_batch5_readiness.py` to CI and the source-integrity gate.
- No Room schema, JSON backup schema, FSRS, app version, versionCode, package identity, signing configuration, or original `StudyForge_Plan.txt` changes.

**Boundary:** this is the core aggregate migration, not removal of every full-list observation. `StudyForgeApp` still observes full questions for legacy tag discovery, editor/export/backup and other consumers; Progress still uses full review logs and question rows for detailed topic/type/subject/chapter analytics and recent review prompt resolution; exam attempts remain observed as a full list for score distribution, median/trend, and history. Those detailed projections and bounded recent-history queries remain open. Android Gradle/KSP, Room instrumentation, CI and device execution remain unverified in this environment.





### V3.0 batch 7 — lazy rich-content hydration and scalability diagnostics (source implemented; Android runtime pending)

- Removed root full-table `observeContentBlocks()` and `observeQuestionParts()` collectors. Study loads ordered rich blocks only for the active question; Exam loads multipart parts only for the bounded active queue IDs and blocks exam start until hydration succeeds.
- Added a bounded Room `IN (:questionIds)` parts query and instrumentation test source for per-question/active-snapshot hydration.
- Added a reproducible host-side SQLite synthetic benchmark at 10k/50k questions for Library paging, Due queue, collection paging, recent reviews and active-card rich content, including `EXPLAIN QUERY PLAN`. Added a 9-check source gate and CI steps.
- Host functional checks pass at both sizes. Query plans identify unindexed scans for Library first-page order and Due queue order, and a scan for collection paging. The content-block query uses the `(questionId, position)` index. No speculative Room index migration was introduced; index and collection-query optimization remain explicit next work.
- Host benchmark timings are diagnostics only, not Android device performance evidence. Android compilation, JUnit, Room instrumentation, CI execution, memory/frame profiling and physical-device benchmarks remain unverified.
- No Room/backup schema, FSRS, app version, versionCode, signing identity or original `StudyForge_Plan.txt` changes.

### V3.0 batch 6 — detailed analytics and remaining root full-list removal (source implemented; Android runtime pending)

- Moved detailed Progress type accuracy, weak topics, weak subjects, weak chapters and mistake-category counts into Room aggregate projections.
- Added an eight-row recent-review projection joined to question prompts, plus a database-grouped 14-day daily activity projection. The Progress screen no longer receives full question or review-log lists.
- Moved exam attempt count, average, best, median, latest-five-vs-previous-five trend and score-band counts into a Room aggregate; only the latest ten attempts are observed for history display.
- Replaced Study's full review-log input with scalar New/Review daily quota counts.
- Removed root-level full question, review-log and exam-attempt observations. The editor resolves its selected question by ID; legacy comma-separated tag discovery uses a recursive Room query.
- Added Room instrumentation coverage and a Batch 6 CI source gate. SQLite smoke tests verified the exam aggregate values and case-insensitive tag splitting.
- Preserved full explicit backup/export snapshots, Room schema 15, backup schema 15, app identity 2.1.24/code 28 and FSRS behavior.

**Boundary:** this removes the root's full question/review-log/exam-attempt observers, but other full-list flows remain where the current UI or explicit backup/export requires them (notably content blocks, question parts, collection links, and complete backup snapshots). Android Gradle/KSP compilation, Room instrumentation, CI/device execution and 10k/50k benchmarks remain unverified.

### Not yet complete in V3.0

- The active Library is paged, Study uses bounded queue snapshots, and the root no longer observes the full question/review-log/exam-attempt tables. Full-list rich-content, question-part, collection-link and explicit backup/export flows remain.
- Home dashboard and all current Progress headline/detail analytics use Room projections (Batches 5–6); recent review history and exam history are bounded. Collections and rich-content/query boundaries remain.
- Study must be switched from full-question-list derivation to the new bounded session snapshot API, with process-recreation persistence, daily quotas/order parity, and all mode semantics covered.
- Add measured indexes only after query-plan/benchmark evidence; run 10k/50k datasets and capture memory/query/frame metrics.
- Full Gradle, KSP, Room instrumentation, emulator and device validation remain pending.

## 1. Version 3 mission

Version 3 turns StudyForge from a feature-rich personal question library into a scalable, deterministic problem-solving and study system. Work is grouped into three substantial releases, not many small feature drops:

1. **3.0 — Scalable data and study foundation**
2. **3.1 — Deterministic numerical question engine**
3. **3.2 — Stepwise problem solving and advanced question formats**

The order is intentional. The data/query foundation comes first so advanced content and larger libraries do not deepen the current full-list loading bottleneck. Numerical and richer question formats build on that foundation.

## 2. Non-negotiable constraints

- Remain offline-first. Core Library, authoring, and Study flows must work without network access.
- Preserve existing question IDs, collection memberships, review logs, FSRS memory state, review history, presets, exam attempts, and question versions.
- Do not change FSRS scheduling policy as part of pagination or UI work. Any later scheduler change requires a separately approved specification and reference tests.
- Keep current backup imports compatible. Any schema change must be additive, migrated, runtime-tested on populated databases, and covered by old-backup fixtures.
- Prefer no Room schema change for paging itself; add indexes or a search table only when query plans and benchmarks justify them.
- Never silently turn imported or AI-generated content into Verified content.
- No credentials, signing keys, or private API secrets in source control or APKs.
- Do not call a batch complete based only on source edits. Record unit tests, Room runtime tests, CI build, migration/backup tests, and device acceptance separately.
- Do not modify `StudyForge_Plan.txt`; it is the preserved original blueprint.

## 3. Release batches

### Version 3.0 — Scalable data and study foundation

**Goal:** stop loading the entire question library into shared app memory and make Library/Study remain responsive as the collection grows.

#### Data architecture

- Add the compatible AndroidX Paging 3 runtime and Compose integration.
- Introduce a clear `QuestionRepository` and screen `ViewModel` boundary around database access; avoid keeping every question in a root composable state object.
- Add Room DAO paging queries returning `PagingSource<Int, QuestionEntity>` (or a projection/summary type where a full entity is unnecessary).
- Expose `Pager` / `Flow<PagingData<…>>` from the ViewModel and render through Compose lazy layouts with stable item keys and explicit loading, empty, error, and retry states.
- Keep paging configuration centralized and benchmark-tuned. Initial planning target: 30 rows per page with a modest prefetch distance; treat these as tunable values, not a permanent contract.
- Ensure every sort has a stable secondary key (question ID) to prevent duplicate/missing rows when records share the same sort value.

#### Database-backed Library operations

- Move text search, type/status/difficulty filters, subject/chapter/topic, tags, exam, source, date ranges, collection membership, and supported performance criteria into SQL/DAO queries.
- Keep query construction parameterized; do not concatenate untrusted user text into SQL.
- Add indexes only for measured hot query paths; capture query plans and verify that filtering does not degrade into a full-table scan for common filters.
- Debounce text entry, cancel obsolete queries, and reset paging correctly when criteria change.
- Preserve saved filter preset semantics and live counts. Counts must be computed by the database for the active criteria rather than by first loading every question.
- Preserve clear selection semantics: “select visible page/results” and “select all matching records” must be distinct. Bulk actions over all matches must execute transactionally without materializing the full result set in Compose memory.

#### Incremental Study queue

- Keep the learner-facing one-card-at-a-time Anki-style interaction.
- Load the active question and the next small amount of work on demand rather than loading every candidate question and its rich content up front.
- Use a stable session queue contract. Because rating a card changes due/sort fields, a naive offset-paged query can skip or repeat cards. Choose and test either a session-start queue snapshot of IDs/order or a stable keyset/cursor strategy with equivalent guarantees.
- Preserve Due/New/Practice/Exam mode semantics, rating actions, review-log order, and existing FSRS transitions.
- Loading or refreshing the Study queue must not double-record reviews or lose the active card after process recreation.

#### Rich content and memory

- Library rows show lightweight summaries. Do not instantiate LaTeX renderers, image decoders, graph renderers, or full content-block trees for every row.
- Render heavy math/media only for visible or opened content; use bounded caches and dispose resources when no longer needed.
- Fetch rich blocks/media for the active question when needed, not for the whole library.
- Rework backup export/import for large datasets so consistency is retained without avoidable whole-database in-memory duplication. Keep the current all-record-group consistency guarantee; use a tested chunk/stream strategy where practical.

#### V3.0 acceptance gates

- No Library or Study screen path requires collecting the entire `questions` table merely to display a page or start a bounded session.
- Search, filters, sort, saved presets, collection filters, and bulk actions work across page boundaries and after database edits.
- Tests prove no duplicate or missing rows when a row is edited/deleted while paging, when filters change, and when pages are invalidated.
- Tests prove a rated Study card is not repeated or skipped and that the session survives process recreation according to its documented policy.
- Run a 10,000-question functional dataset and a 50,000-question stress dataset with mixed question types, tags, collection links, and some math/media content.
- Record first-page query latency, scrolling/frame behavior, memory use, and query plans on a documented reference device; compare against a V2 baseline. Targets are set from measurements rather than guessed device-independent claims.
- Verify offline behavior, backup/restore, and all supported Room migrations on populated databases.

### Version 3.1 — Deterministic numerical question engine

**Goal:** replace exact-text-only numerical checking with a reusable, reproducible numerical-template system.

- Introduce versioned numerical templates with prompt/explanation templates, typed variables, allowed ranges, constraints, units, formula definitions, answer format, and grading policy.
- Use a safe deterministic expression evaluator; never execute arbitrary code or `eval` imported/user text.
- Generate reproducible variants from a stored seed/variant ID. Save enough template/version metadata to reproduce the exact question and answer later.
- Validate variable bounds and cross-variable constraints before displaying a variant; reject impossible or invalid combinations.
- Support unit-aware answers and conversions, absolute and relative tolerances, accepted equivalent forms, fractions/decimals/scientific notation, rounding rules, and significant figures.
- Provide deterministic explanation substitution using the same generated variables and intermediate values used to calculate the answer.
- Add automatic MCQ distractor rules for supported numerical templates, with checks that distractors are distinct, valid, and do not accidentally equal the correct answer under the configured tolerance.
- Add template preview, test-variant generation, validation diagnostics, and draft/verified status handling in the editor.
- Include a safe fallback path for old ordinary Numerical questions so they continue to work during migration.

#### V3.1 acceptance gates

- Same seed + same template version always produces the same valid variant and answer.
- Invalid ranges, impossible constraints, division by zero, overflow, unsupported units, and malformed formulas fail with user-visible validation rather than crashing.
- Unit conversion, tolerance, significant-figure, rounding, and accepted-equivalent-form tests cover boundary values.
- MCQ distractors are deterministic for a seed and never collide with the correct answer within grading tolerance.
- Existing V2 numerical cards and backups continue to load and grade according to their documented legacy behavior.

### Version 3.2 — Stepwise problem solving and advanced question formats

**Goal:** make StudyForge useful for solving multi-step science and mathematics problems, not only recalling a final answer.

- Complete structured derivation authoring and Study rendering: ordered steps, optional intermediate expressions, learner reveal/attempt controls, and final solution reveal.
- Add richer multipart/partial-credit grading with explicit per-part answer rules, accepted alternatives, tolerances where appropriate, and manual marking only where automation cannot be reliable.
- Add selected advanced formats from the original blueprint in a deliberate order: multi-select; matching and ordering; image occlusion/diagram labeling. Each format must have an explicit persisted model, editor, preview, Study interaction, exam scoring, import/export, and accessibility behavior before it is considered complete.
- Make format-specific analytics reflect the actual grading model; avoid treating an unscored or manually pending response as incorrect.
- Keep content blocks extensible and versioned. Validate block payloads before save/import and render unsupported future block kinds safely.
- Improve accessible equation/diagram descriptions and keyboard/screen-reader interaction for new question formats.

#### V3.2 acceptance gates

- Derivation steps preserve order and content through edit, version restore, export/import, offline rendering, and Study.
- Each advanced format passes editor → persistence → Study → exam → grading → analytics → backup/restore round-trip tests.
- Partial credit and manual grading are transparent to the learner and reflected correctly in scores and analytics.
- Malformed/imported rich content fails safely and does not crash Library, Study, collection assignment, or backup restore.

## 4. Architecture sequence

```text
Room entities and DAO queries
          |
          v
QuestionRepository
          |
          v
Screen ViewModels (query/filter/session state)
          |
          +------------------------+
          |                        |
          v                        v
Library Pager / Flow        Study queue source
          |                        |
          v                        v
Compose LazyPagingItems     One active Study card
          |                        |
          +-----------+------------+
                      v
             On-demand rich content
            (LaTeX / images / graphs)
```

Paging 3 is intended for the Library and other genuinely long lists. Study should use a session-aware queue abstraction; blindly applying ordinary list offsets to a query whose sort keys change after each rating is not acceptable.

## 5. Cross-version testing strategy

- **Pure logic tests:** filter query mapping, sort keys, numerical parsing/evaluation, units, tolerance, random-seed reproducibility, derivation ordering, format validation.
- **Room tests:** query correctness, paging invalidation, indexes/query plans, bulk selection across pages, populated historical migrations, collection joins, deletion/cascade behavior.
- **Compose/instrumentation tests:** load/error/empty/retry states, rapid filter changes, scrolling, navigation, math/media deferred rendering, accessibility labels, no crash with every question type.
- **Data safety tests:** backup/export/import of all record groups, media, review history, collection membership, presets, attempts, versions, and legacy backups.
- **Performance tests:** 10k and 50k datasets, query latency, memory, scroll smoothness, and repeated search/filter cycles on a documented device.
- **CI gates:** full Gradle build, unit tests, lint, emulator instrumentation, migration suite, backup round-trip, and artifact/version consistency.
- **Device acceptance:** install as an update over the previous signed build; verify offline behavior and that no existing user data or signing identity is lost.

## 6. Explicitly out of scope for Version 3

- AI provider integrations, in-app AI generation, API publishing, and the web dashboard remain Version 4 scope.
- Accounts, cloud synchronization, multi-device conflict resolution, and sharing remain Version 5 scope.
- Do not add mandatory accounts, network dependence, analytics telemetry, or remote services to the local-first core.
- Do not combine a broad FSRS algorithm rewrite with the Paging migration.

## 7. V2 → V3 transition gate

V3.0 source implementation has started by user instruction. Before merging or releasing V3.0, close the remaining V2 release checks (or obtain explicit approval to defer them):

- Confirm the compact bottom-navigation label fix on the installed APK; the source package exists, but the user has not yet confirmed that exact build on-device.
- Complete the reported Progress-tab/other-screen visual polish with screenshots and verify Light and Dark themes.
- Run the current GitHub Actions workflow against the exact final V2 source and resolve all failures.
- Complete populated Room migration runtime tests and real backup/restore acceptance (including media/history/collections), or explicitly record any deferred gate with user approval.
- Verify update installation and signing-certificate continuity using the existing release identity; do not access or change credentials casually.

Once these checks are green or explicitly accepted as deferred, freeze V2 and designate the tested V2 source baseline for V3.0. The current V3 work is an isolated source workstream; do not ship it or merge it into a release build until the transition gate is resolved. Record any V2 hotfixes separately.

## 8. Release discipline

- Keep the current app at 2.1.24/versionCode 28 until a real new APK release is built.
- Proposed milestone tags are `3.0.0`, `3.1.0`, and `3.2.0`; exact Android versionCode values are assigned only when each APK release is actually created.
- Each release ZIP includes source, updated roadmap, implementation status, build status, checksums, tests, and a new handoff summary.
- No milestone is complete until its acceptance gates and data-safety checks pass.

### V3.0 batch 4 — production Study queue migration (source implemented; Android/runtime validation pending)

- Replaced the production `StudyScreen` full-question-list input with a repository callback that selects a bounded queue ID snapshot in Room and hydrates only those IDs in snapshot order.
- Added a distinct-topic Room flow. Due New and Review cards are queried independently to preserve their separate daily quotas and Oldest/Weakest/Random review ordering.
- Applied saved-session criteria in SQLite; retained Ordered, Weakest, Random and Topic modes; added SQL-side Exam eligibility for MCQ, True/False, Numerical and multipart questions with the configured randomize behavior.
- Saved active queue IDs and a criteria fingerprint through Compose saved state to rehydrate the same session queue when Android restores the screen. Added loading/error/retry states and tests for due partitions and Exam eligibility.
- Source gates and standalone SQLite execution pass. Android Gradle/KSP/Compose, Room instrumentation, process restoration, emulator/device, CI and 10k/50k performance testing remain pending.

**Remaining V3.0 work:** remove full-list consumers from Home/Progress and other destinations where appropriate; database-back review-log aggregates; lazy-load rich content/parts; measure 10k/50k query, memory and frame performance; run full Android acceptance. Review logs and rich content remain full-list inputs. This batch does not declare V3.0 complete.
