package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StudyQueueQueryTest {
    @Test fun acceptsEveryExistingStudyMode() {
        StudySessionRules.supportedModes.forEach { mode -> StudyQueueQuery(mode = mode).validate() }
        StudyQueueQuery(mode = "New").validate()
        StudyQueueQuery(mode = "DueNew").validate()
        StudyQueueQuery(mode = "DueReview", reviewOrder = "Weakest").validate()
        StudyQueueQuery(mode = "Exam", randomize = true).validate()
    }

    @Test fun rejectsUnknownModesAndUnboundedLimits() {
        assertThrows(IllegalArgumentException::class.java) { StudyQueueQuery(mode = "Unknown").validate() }
        assertThrows(IllegalArgumentException::class.java) { StudyQueueQuery(limit = 0).validate() }
        assertThrows(IllegalArgumentException::class.java) { StudyQueueQuery(limit = 501).validate() }
        assertThrows(IllegalArgumentException::class.java) { StudyQueueQuery(reviewOrder = "Unsupported").validate() }
    }

    @Test fun defaultQueueIsBounded() {
        val query = StudyQueueQuery()
        query.validate()
        assertEquals("Due", query.mode)
        assertEquals(20, query.limit)
    }
}
