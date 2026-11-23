package com.studyforge.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionLibraryFilterTest {
    private fun question(
        id: String = "q1",
        difficulty: String = "Medium",
        exam: String = "MDCAT",
        correct: Int = 0,
        incorrect: Int = 0,
        reviews: Int = correct + incorrect
    ) = QuestionEntity(
        id = id,
        prompt = "What is velocity?",
        answer = "Displacement per unit time",
        explanation = "Rate of change of displacement",
        subject = "Physics",
        chapter = "Kinematics",
        topic = "Velocity",
        difficulty = difficulty,
        exam = exam,
        correctCount = correct,
        incorrectCount = incorrect,
        reviewCount = reviews
    )

    @Test fun emptyCriteriaMatchesAllQuestions() {
        assertTrue(QuestionLibraryFilter.matches(question(), LibraryFilterCriteria()))
    }

    @Test fun examSearchMatchesCaseInsensitively() {
        assertTrue(QuestionLibraryFilter.matches(question(exam = "MDCAT 2027"), LibraryFilterCriteria(query = "mdcat")))
    }

    @Test fun difficultyFilterSeparatesQuestions() {
        assertTrue(QuestionLibraryFilter.matches(question(difficulty = "Hard"), LibraryFilterCriteria(difficulty = "Hard")))
        assertFalse(QuestionLibraryFilter.matches(question(difficulty = "Easy"), LibraryFilterCriteria(difficulty = "Hard")))
    }

    @Test fun weakPerformanceRequiresMoreIncorrectThanCorrectAnswers() {
        assertTrue(QuestionLibraryFilter.matches(question(correct = 1, incorrect = 3), LibraryFilterCriteria(performance = "Weak")))
        assertFalse(QuestionLibraryFilter.matches(question(correct = 2, incorrect = 1), LibraryFilterCriteria(performance = "Weak")))
    }

    @Test fun strongPerformanceRequiresAtLeastOneCorrectAnswer() {
        assertTrue(QuestionLibraryFilter.matches(question(correct = 2, incorrect = 2), LibraryFilterCriteria(performance = "Strong")))
        assertFalse(QuestionLibraryFilter.matches(question(correct = 0, incorrect = 0, reviews = 1), LibraryFilterCriteria(performance = "Strong")))
    }

    @Test fun unattemptedUsesReviewCount() {
        assertTrue(QuestionLibraryFilter.matches(question(reviews = 0), LibraryFilterCriteria(performance = "Unattempted")))
        assertFalse(QuestionLibraryFilter.matches(question(correct = 1), LibraryFilterCriteria(performance = "Unattempted")))
    }

    @Test fun collectionFilterUsesQuestionIds() {
        val q = question(id = "inside")
        val criteria = LibraryFilterCriteria(collectionId = "collection-1")
        assertTrue(QuestionLibraryFilter.matches(q, criteria, setOf("inside")))
        assertFalse(QuestionLibraryFilter.matches(q, criteria, setOf("other")))
    }

    @Test fun everyActiveFilterMustMatch() {
        val q = question(difficulty = "Hard", exam = "Board", correct = 2, incorrect = 0)
        val criteria = LibraryFilterCriteria(type = "Flashcard", subject = "Physics", chapter = "Kinematics", topic = "Velocity", difficulty = "Hard", exam = "Board", performance = "Strong")
        assertTrue(QuestionLibraryFilter.matches(q, criteria))
        assertFalse(QuestionLibraryFilter.matches(q, criteria.copy(exam = "MDCAT")))
    }
}
