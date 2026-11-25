package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StudySessionRulesTest {
    private fun q(id:String, subject:String="Physics", chapter:String="Kinematics", topic:String="Motion", tag:String="high-yield", difficulty:String="Easy", exam:String="MDCAT") =
        QuestionEntity(id=id, prompt=id, answer="a", subject=subject, chapter=chapter, topic=topic, tagsCsv=tag, difficulty=difficulty, exam=exam)
    @Test fun appliesCollectionMembershipWhenSelected() {
        val items = listOf(q("a"), q("b"), q("c"))
        val preset = StudySessionPresetEntity("p", "Collection", collectionId = "collection-1")
        assertEquals(listOf("b", "c"), StudySessionRules.filterQuestions(items, preset, setOf("b", "c")).map { it.id })
        assertEquals(emptyList<String>(), StudySessionRules.filterQuestions(items, preset).map { it.id })
    }

    @Test fun appliesAllConfiguredCriteriaAndLimit() {
        val items = listOf(q("a"), q("b", topic="Forces"), q("c", difficulty="Hard"), q("d", exam="Board"))
        val preset = StudySessionPresetEntity("p", "Session", subject="Physics", chapter="Kinematics", topic="Motion", tag="high-yield", difficulty="Easy", exam="MDCAT", questionLimit=1)
        assertEquals(listOf("a"), StudySessionRules.filterQuestions(items, preset).map { it.id })
    }
}
