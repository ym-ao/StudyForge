package com.studyforge.app.data

/** Pure collection-link transformations shared by Library bulk actions. */
object CollectionMembershipRules {
    /**
     * Returns a stable, duplicate-free collection ID list after adding or removing one ID.
     * Existing order is retained; a newly added ID is appended. Blank IDs are ignored.
     */
    fun update(existingIds: List<String>, collectionId: String, add: Boolean): List<String> {
        val normalizedId = collectionId.trim()
        val existing = existingIds.map(String::trim).filter(String::isNotEmpty).distinct()
        if (normalizedId.isEmpty()) return existing
        return if (add) {
            if (normalizedId in existing) existing else existing + normalizedId
        } else {
            existing.filterNot { it == normalizedId }
        }
    }
}
