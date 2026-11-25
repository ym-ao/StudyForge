package com.studyforge.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionMembershipRulesTest {
    @Test fun addAppendsWithoutDuplicatingAndPreservesExistingOrder() {
        assertEquals(listOf("a", "b", "c"), CollectionMembershipRules.update(listOf("a", "b", "a"), "c", true))
    }

    @Test fun addingExistingCollectionIsNoOp() {
        assertEquals(listOf("a", "b"), CollectionMembershipRules.update(listOf("a", "b"), "a", true))
    }

    @Test fun removeDeletesOnlyRequestedCollection() {
        assertEquals(listOf("a", "c"), CollectionMembershipRules.update(listOf("a", "b", "c"), "b", false))
    }

    @Test fun removeMissingCollectionIsNoOp() {
        assertEquals(listOf("a", "b"), CollectionMembershipRules.update(listOf("a", "b"), "x", false))
    }

    @Test fun blankIdsAreIgnoredAndWhitespaceIsTrimmed() {
        assertEquals(listOf("a", "b"), CollectionMembershipRules.update(listOf(" a ", "", "a"), " b ", true))
        assertEquals(listOf("a", "b"), CollectionMembershipRules.update(listOf(" a ", " b "), " ", false))
    }
}
