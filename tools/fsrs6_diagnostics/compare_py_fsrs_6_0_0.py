#!/usr/bin/env python3
"""Reproduce the py-fsrs v6.0.0 test_memo_state fixture against StudyForge's exact Kotlin scheduler source.

Reference formulas/defaults are transcribed from:
https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/fsrs/fsrs.py
Fixture is from:
https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/tests/test_basic.py

Requires kotlinc and a JRE. No network access or third-party Python packages are needed.
"""
from __future__ import annotations
import math
import pathlib
import re
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[2]
SCHEDULER = ROOT / "app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt"

# Exact DEFAULT_PARAMETERS from py-fsrs commit 352c7a2 (fsrs/fsrs.py).
UPSTREAM = [
    0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966,
    0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127,
    2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2,
]
INCREMENTS = [0, 0, 1, 3, 8, 21, 0]
RATINGS = [1, 3, 3, 3, 3, 3, 3]  # Again, Good x 5, then final same-time Good


def upstream_reference_trace():
    p = UPSTREAM
    decay = -p[20]
    factor = 0.9 ** (1 / decay) - 1

    def initial_s(g):
        return max(p[g - 1], 0.001)

    # v6.0.0 clamps initial difficulty, including the Easy mean-reversion target.
    def initial_d(g):
        return min(max(p[4] - math.exp(p[5] * (g - 1)) + 1, 1), 10)

    def next_d(d, g):
        target = initial_d(4)
        delta = -(p[6] * (g - 3))
        damped = (10 - d) * delta / 9
        return min(max(p[7] * target + (1 - p[7]) * (d + damped), 1), 10)

    def short_s(s, g):
        increase = math.exp(p[17] * (g - 3 + p[18])) * s ** (-p[19])
        # v6.0.0 clamps the short-term increase for Good and Easy (not Hard).
        if g in (3, 4):
            increase = max(increase, 1)
        return max(s * increase, 0.001)

    def retrievability(days, s):
        return (1 + factor * max(days, 0) / s) ** decay

    def next_s(d, s, r, g):
        if g == 1:
            long_term = p[11] * d ** (-p[12]) * ((s + 1) ** p[13] - 1) * math.exp((1 - r) * p[14])
            short_term = s / math.exp(p[17] * p[18])
            return max(min(long_term, short_term), 0.001)
        hard_penalty = p[15] if g == 2 else 1
        easy_bonus = p[16] if g == 4 else 1
        growth = math.exp(p[8]) * (11 - d) * s ** (-p[9]) * (math.exp((1 - r) * p[10]) - 1) * hard_penalty * easy_bonus
        return max(s * (1 + growth), 0.001)

    stability = difficulty = None
    state, step = "Learning", 0
    last_day = None
    day = 0
    trace = []
    for index, (grade, increment) in enumerate(zip(RATINGS, INCREMENTS), 1):
        day += increment
        elapsed = None if last_day is None else day - last_day
        if stability is None and difficulty is None:
            stability, difficulty = initial_s(grade), initial_d(grade)
        elif state == "Learning":
            if elapsed is not None and elapsed < 1:
                stability = short_s(stability, grade)
            else:
                stability = next_s(difficulty, stability, retrievability(elapsed, stability), grade)
            difficulty = next_d(difficulty, grade)
        elif state == "Review":
            if elapsed is not None and elapsed < 1:
                stability = short_s(stability, grade)
            else:
                stability = next_s(difficulty, stability, retrievability(elapsed, stability), grade)
            difficulty = next_d(difficulty, grade)

        # Exact v6.0.0 Learning-state transitions for the fixture's two learning steps.
        if state == "Learning":
            if grade == 1:
                step = 0
            elif grade == 3:
                if step + 1 == 2:
                    state, step = "Review", None
                else:
                    step += 1
            elif grade == 4:
                state, step = "Review", None
        last_day = day
        trace.append((index, grade, day, state, step, stability, difficulty, elapsed))
    return trace


def compile_and_run_studyforge():
    source = SCHEDULER.read_text()
    source = source[source.index("/** FSRS-6 scheduler"):source.index("\nsuspend fun StudyDatabase.recordGradedReview")]
    header = '''package com.studyforge.app.data
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
'''
    stubs = '''
data class QuestionEntity(
 val id:String, val prompt:String, val answer:String,
 val reviewCount:Int=0, val lastReviewedAt:Long?=null, val dueAt:Long?=null,
 val intervalDays:Double=0.0, val easeFactor:Double=2.5, val repetitions:Int=0,
 val lapseCount:Int=0, val learningState:String="New", val learningStepIndex:Int=-1,
 val fsrsStability:Double=0.0, val fsrsDifficulty:Double=0.0
)
data class StudySettingsEntity(val desiredRetention:Double=0.90,val maxIntervalDays:Int=36500,val learningStepsMinutesCsv:String="1,10",val relearningStepsMinutesCsv:String="10") { companion object { val DEFAULT=StudySettingsEntity() } }
fun main() {
 val base=java.time.Instant.parse("2022-11-29T12:30:00Z").toEpochMilli()
 val ratings=listOf("Again","Good","Good","Good","Good","Good","Good")
 val increments=listOf(0,0,1,3,8,21,0)
 var at=base
 var q=QuestionEntity("q","p","a")
 for(i in ratings.indices){
   at += increments[i]*86_400_000L
   q=ReviewScheduler.apply(q,ratings[i],at)
   println("${i+1}|${ratings[i]}|${(at-base)/86_400_000L}|${q.learningState}|${q.learningStepIndex}|${q.fsrsStability}|${q.fsrsDifficulty}")
 }
 val hardSettings=StudySettingsEntity(learningStepsMinutesCsv="1,10",relearningStepsMinutesCsv="10")
 val newHard=ReviewScheduler.apply(QuestionEntity("h","p","a"),"Hard",base,hardSettings)
 println("POLICY|newHard|${newHard.learningState}|${newHard.learningStepIndex}|${newHard.intervalDays*1440.0}")
 val again=ReviewScheduler.apply(QuestionEntity("r","p","a"),"Again",base,hardSettings)
 val learningHard=ReviewScheduler.apply(again,"Hard",again.dueAt!!,hardSettings)
 println("POLICY|learningHard|${learningHard.learningState}|${learningHard.learningStepIndex}|${learningHard.intervalDays*1440.0}")
 val review=QuestionEntity("rr","p","a",learningState="Review",fsrsStability=10.0,fsrsDifficulty=5.0,intervalDays=10.0,lastReviewedAt=base)
 val lapse=ReviewScheduler.apply(review,"Again",base+86_400_000L,hardSettings)
 val relearningHard=ReviewScheduler.apply(lapse,"Hard",lapse.dueAt!!,hardSettings)
 println("POLICY|relearningHard|${relearningHard.learningState}|${relearningHard.learningStepIndex}|${relearningHard.intervalDays*1440.0}")
 println("POLICY|elapsedAcrossMidnight|${ReviewScheduler.elapsedStudyDays(base,base+15L*60L*60L*1000L)}")
}
'''
    with tempfile.TemporaryDirectory(prefix="studyforge-fsrs6-") as tmp:
        tmp = pathlib.Path(tmp)
        kt, jar = tmp / "Harness.kt", tmp / "harness.jar"
        kt.write_text(header + source + stubs)
        subprocess.run(["kotlinc", str(kt), "-include-runtime", "-d", str(jar)], check=True, capture_output=True, text=True)
        result = subprocess.run(["java", "-jar", str(jar)], check=True, capture_output=True, text=True)
    rows = []
    policies = {}
    for line in result.stdout.splitlines():
        fields = line.split("|")
        if fields[0] == "POLICY":
            policies[fields[1]] = fields[2:]
            continue
        step = int(fields[4])
        rows.append((int(fields[0]), fields[1], int(fields[2]), fields[3], None if step < 0 else step, float(fields[5]), float(fields[6])))
    return rows, policies


def main():
    source = SCHEDULER.read_text()
    kotlin_match = re.search(r"private val weights = doubleArrayOf\((.*?)\)", source, re.S)
    if not kotlin_match:
        raise SystemExit("Could not locate ReviewScheduler weights")
    actual_weights = [float(x.strip()) for x in kotlin_match.group(1).replace("\n", " ").split(",") if x.strip()]
    if len(actual_weights) != 21:
        raise SystemExit(f"Expected 21 StudyForge weights, found {len(actual_weights)}")

    upstream = upstream_reference_trace()
    studyforge, policies = compile_and_run_studyforge()
    print("py-fsrs v6.0.0 test_memo_state — per-review comparison")
    print("Dates are cumulative increments [0, 0, 1, 3, 8, 21], followed by same-time Good.")
    print("#  day  rating  upstream S / D                 StudyForge S / D")
    for ref, sf in zip(upstream, studyforge):
        i, grade, day, state, step, s_ref, d_ref, elapsed = ref
        _, rating, sf_day, sf_state, sf_step, s_sf, d_sf = sf
        label = {1: "Again", 2: "Hard", 3: "Good", 4: "Easy"}[grade]
        print(f"{i}  {day:>3}  {label:<5}   {s_ref:>12.9f} / {d_ref:>10.9f}   {s_sf:>12.9f} / {d_sf:>10.9f}")

    print("\nParameter-vector comparison:")
    print(f"  py-fsrs v6.0.0: {UPSTREAM}")
    print(f"  StudyForge:     {actual_weights}")
    print(f"  vectors equal:  {actual_weights == UPSTREAM}")

    failures = []
    if actual_weights != UPSTREAM:
        failures.append("StudyForge weights differ from pinned py-fsrs v6.0.0 defaults")
    for ref, sf in zip(upstream, studyforge):
        i, grade, day, state, step, s_ref, d_ref, elapsed = ref
        _, rating, sf_day, sf_state, sf_step, s_sf, d_sf = sf
        if abs(s_ref - s_sf) > 1e-8 or abs(d_ref - d_sf) > 1e-8:
            failures.append(f"review {i}: memory-state mismatch")
        if state != sf_state or step != sf_step:
            failures.append(f"review {i}: scheduler-state mismatch upstream={state}/{step} StudyForge={sf_state}/{sf_step}")

    print("\nPinned v6.0.0 policy checks:")
    print("  - Initial Easy mean-reversion target is clamped.")
    print("  - Same-day short-term stability floor applies to Good and Easy, not Hard.")
    print("  - Hard at step zero keeps step zero and schedules midpoint(first, second), or 1.5x for one step.")
    print("  - Elapsed review days are completed 24-hour periods (timedelta.days semantics).")
    expected_policies = {
        "newHard": ["Learning", "0", "5.5"],
        "learningHard": ["Learning", "0", "5.5"],
        "relearningHard": ["Relearning", "0", "15.0"],
        "elapsedAcrossMidnight": ["0.0"],
    }
    for name, expected in expected_policies.items():
        actual = policies.get(name)
        if actual is None or actual[:len(expected)] != expected:
            failures.append(f"{name}: expected {expected}, got {actual}")
        print(f"  {name}: {'PASS' if actual is not None and actual[:len(expected)] == expected else 'FAIL'} ({actual})")
    print("\nUpstream expected rounded final state: S=49.4472, D=6.8271")
    print(f"Reference port final state:          S={upstream[-1][5]:.4f}, D={upstream[-1][6]:.4f}")
    print(f"StudyForge actual Kotlin final state: S={studyforge[-1][5]:.4f}, D={studyforge[-1][6]:.4f}")
    if round(upstream[-1][5], 4) != 49.4472 or round(upstream[-1][6], 4) != 6.8271:
        failures.append("reference port did not reproduce pinned upstream golden result")
    if failures:
        print("\nCONFORMANCE FAILURES:")
        for failure in failures:
            print(f"  - {failure}")
        raise SystemExit(1)
    print("\nPASS: StudyForge matches every memory-state and scheduler-state transition in the pinned fixture.")

if __name__ == "__main__":
    main()
