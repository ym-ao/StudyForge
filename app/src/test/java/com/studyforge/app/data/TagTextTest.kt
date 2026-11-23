package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TagTextTest {
    @Test fun parsesAndDeduplicatesCaseInsensitively() {
        assertEquals(listOf("Physics", "MDCAT"), TagText.parse(" Physics, MDCAT, physics, ,"))
    }

    @Test fun renameMatchesWholeTagAndPreservesOtherTags() {
        assertEquals("Mechanics, Formula 10, MDCAT", TagText.rename("Mechanics, Formula 1, MDCAT", "Formula 1", "Formula 10"))
    }

    @Test fun renameAvoidsDuplicateNames() {
        assertEquals("Physics, MDCAT", TagText.rename("Physics, Chemistry", "Chemistry", "physics"))
    }

    @Test fun removeOnlyRemovesExactTag() {
        assertEquals("Formula 10, MDCAT", TagText.remove("Formula 1, Formula 10, MDCAT", "Formula 1"))
    }

    @Test fun emptyAndWhitespaceTagsAreIgnored() {
        assertEquals(emptyList<String>(), TagText.parse(" ,  , "))
    }
}
