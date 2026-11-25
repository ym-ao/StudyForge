package com.studyforge.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Stores content-only snapshots; restoring a version never rewinds scheduling or review history. */
object QuestionVersionRules {
    data class Snapshot(
        val question: QuestionEntity,
        val contentBlocks: List<QuestionContentBlockEntity>,
        val parts: List<QuestionPartEntity>,
        val collectionIds: List<String>
    )

    data class FieldChange(val path: String, val previous: String, val current: String)

    /** Full content diff; scheduling/review fields are intentionally outside version snapshots. */
    fun compare(previousSnapshotJson: String, currentSnapshotJson: String): List<FieldChange> {
        val previous = flatten(JSONObject(previousSnapshotJson))
        val current = flatten(JSONObject(currentSnapshotJson))
        return (previous.keys + current.keys).distinct().sorted().mapNotNull { path ->
            val before = previous[path]
            val after = current[path]
            if (before == after) null else FieldChange(path, before ?: "<not present>", after ?: "<not present>")
        }
    }

    private fun flatten(root: JSONObject): Map<String, String> {
        val result = linkedMapOf<String, String>()
        fun visit(value: Any?, path: String) {
            when (value) {
                is JSONObject -> {
                    val keys = value.keys().asSequence().toList().sorted()
                    if (keys.isEmpty() && path.isNotEmpty()) result[path] = "{}"
                    keys.forEach { key ->
                        // Avoid rendering or duplicating embedded image payloads in a diff.
                        val child = value.opt(key)
                        val childPath = if (path.isEmpty()) key else "$path.$key"
                        if (key == "mediaBase64") {
                            val size = (child as? String).orEmpty().length
                            result[childPath] = if (size == 0) "<no embedded media>" else "<embedded media, $size characters>"
                        } else visit(child, childPath)
                    }
                }
                is JSONArray -> {
                    if (value.length() == 0 && path.isNotEmpty()) result[path] = "[]"
                    for (i in 0 until value.length()) {
                        val item = value.opt(i)
                        val stableKey = if (item is JSONObject && item.optString("id").isNotBlank()) "[id=${item.optString("id")}]" else "[$i]"
                        visit(item, path + stableKey)
                    }
                }
                JSONObject.NULL -> result[path] = "null"
                null -> result[path] = "null"
                else -> result[path] = value.toString()
            }
        }
        visit(root, "")
        return result
    }

    fun createSnapshot(
        question: QuestionEntity,
        blocks: List<QuestionContentBlockEntity>,
        parts: List<QuestionPartEntity>,
        collectionIds: List<String>
    ): String {
        val root = JSONObject()
            .put("id", question.id).put("prompt", question.prompt).put("answer", question.answer)
            .put("explanation", question.explanation).put("subject", question.subject)
            .put("chapter", question.chapter).put("topic", question.topic).put("type", question.type)
            .put("optionsJson", question.optionsJson).put("latex", question.latex).put("tagsCsv", question.tagsCsv)
            .put("status", question.status).put("difficulty", question.difficulty).put("exam", question.exam)
            .put("sourceTitle", question.sourceTitle).put("sourceDetail", question.sourceDetail)
        root.put("contentBlocks", JSONArray().apply { blocks.forEach { b -> put(JSONObject()
            .put("id", b.id).put("position", b.position).put("kind", b.kind).put("text", b.text)
            .put("mediaBase64", b.mediaBase64).put("mediaMimeType", b.mediaMimeType).put("altText", b.altText)) } })
        root.put("parts", JSONArray().apply { parts.forEach { p -> put(JSONObject()
            .put("id", p.id).put("position", p.position).put("prompt", p.prompt).put("answer", p.answer)
            .put("explanation", p.explanation).put("points", p.points).put("optionsJson", p.optionsJson)
            .put("absoluteTolerance", p.absoluteTolerance).put("relativeTolerance", p.relativeTolerance)
            .put("acceptedAnswers", p.acceptedAnswers)) } })
        root.put("collectionIds", JSONArray(collectionIds.distinct()))
        return root.toString()
    }

    fun restore(snapshotJson: String, current: QuestionEntity): Snapshot {
        val root = JSONObject(snapshotJson)
        require(root.optString("id") == current.id) { "Version belongs to a different question" }
        val restored = current.copy(
            prompt = root.optString("prompt", current.prompt), answer = root.optString("answer", current.answer),
            explanation = root.optString("explanation", current.explanation), subject = root.optString("subject", current.subject),
            chapter = root.optString("chapter", current.chapter), topic = root.optString("topic", current.topic),
            type = root.optString("type", current.type), optionsJson = root.optString("optionsJson", current.optionsJson),
            latex = root.optString("latex", current.latex), tagsCsv = root.optString("tagsCsv", current.tagsCsv),
            status = root.optString("status", current.status), difficulty = root.optString("difficulty", current.difficulty),
            exam = root.optString("exam", current.exam),
            sourceTitle = root.optString("sourceTitle", current.sourceTitle),
            sourceDetail = root.optString("sourceDetail", current.sourceDetail),
            updatedAt = System.currentTimeMillis()
        )
        val blocks = mutableListOf<QuestionContentBlockEntity>()
        val blockArray = root.optJSONArray("contentBlocks") ?: JSONArray()
        for (i in 0 until blockArray.length()) {
            val b = blockArray.getJSONObject(i)
            blocks += QuestionContentBlockEntity(
                id = b.optString("id"), questionId = current.id, position = b.optInt("position", i),
                kind = b.optString("kind", "TEXT"), text = b.optString("text"),
                mediaBase64 = b.optString("mediaBase64"), mediaMimeType = b.optString("mediaMimeType"), altText = b.optString("altText")
            )
        }
        val parts = mutableListOf<QuestionPartEntity>()
        val partArray = root.optJSONArray("parts") ?: JSONArray()
        for (i in 0 until partArray.length()) {
            val p = partArray.getJSONObject(i)
            parts += QuestionPartEntity(
                id = p.optString("id"), questionId = current.id, position = p.optInt("position", i),
                prompt = p.optString("prompt"), answer = p.optString("answer"), explanation = p.optString("explanation"),
                points = p.optDouble("points", 1.0).takeIf { it.isFinite() && it > 0.0 } ?: 1.0,
                optionsJson = p.optString("optionsJson", "[]"),
                absoluteTolerance = p.optDouble("absoluteTolerance", 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: 0.0,
                relativeTolerance = p.optDouble("relativeTolerance", 0.0).takeIf { it.isFinite() && it in 0.0..1.0 } ?: 0.0,
                acceptedAnswers = p.optString("acceptedAnswers")
            )
        }
        val ids = root.optJSONArray("collectionIds") ?: JSONArray()
        val collectionIds = (0 until ids.length()).map { ids.optString(it) }.filter { it.isNotBlank() }.distinct()
        return Snapshot(restored, blocks, parts, collectionIds)
    }
}
