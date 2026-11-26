#!/usr/bin/env python3
"""Static acceptance-preparation gate; does not run Android builds/tests."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = []
def check(name: str, condition: bool) -> None:
    checks.append((name, bool(condition)))

app_gradle = (ROOT / "app/build.gradle.kts").read_text()
db = (ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
workflow = (ROOT / ".github/workflows/android-apk.yml").read_text()
runbook = (ROOT / "V3_FINAL_ACCEPTANCE_RUNBOOK.md").read_text()
plan = ROOT / "StudyForge_Plan.txt"

check("application identity remains 2.1.24 / code 28", 'versionName = "2.1.24"' in app_gradle and "versionCode = 28" in app_gradle)
check("Room schema is 16", "version = 16" in db)
check("15->16 migration is registered", "MIGRATION_15_16" in db and "Migration(15, 16)" in db)
check("no destructive migration fallback", "fallbackToDestructiveMigration" not in db)
backup_source = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
check("backup schema remains 15", '.put("schemaVersion", 15)' in backup_source and "schemaVersion in 1..15" in backup_source)
check("CI includes Android lint, JUnit and emulator instrumentation", all(token in workflow for token in (":app:lintDebug", ":app:testDebugUnitTest", ":app:connectedDebugAndroidTest")))
check("CI includes all current V3.0 readiness gates", all(f"verify_v3_batch{i}_readiness.py" in workflow for i in range(2, 9)))
check("CI includes 10k/50k synthetic benchmark", "benchmark_v3_scalability.py" in workflow)
check("final Android acceptance runbook exists", "Physical-device acceptance" in runbook and "populated" in runbook)
check("original plan file exists", plan.is_file() and plan.stat().st_size > 0)
check("signing contract retains expected alias", "studyforge-release" in workflow)

for name, passed in checks:
    print(("PASS" if passed else "FAIL") + ": " + name)
print(f"V3 final acceptance preparation: {sum(p for _, p in checks)}/{len(checks)} checks passed")
if not all(p for _, p in checks):
    raise SystemExit(1)
print("LIMIT: this is a static preparation gate; Android compilation, JUnit, Room runtime, populated migration, backup/restore and device acceptance require CI/device execution.")
