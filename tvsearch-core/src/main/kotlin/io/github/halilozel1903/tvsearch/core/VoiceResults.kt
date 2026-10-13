package io.github.halilozel1903.tvsearch.core

/** Picks the text to search from a speech recognizer's answer. */
public object VoiceResults {

    /**
     * The best non-blank entry of [results], cleaned with [QueryText.clean]: the one with the highest
     * [confidences] score when the scores line up with the results, otherwise the first one.
     * `null` when there is nothing usable.
     */
    public fun best(results: List<String?>?, confidences: FloatArray? = null): String? {
        if (results.isNullOrEmpty()) return null
        val usable = results.indices.filter { !results[it].isNullOrBlank() }
        if (usable.isEmpty()) return null
        val pick = if (confidences != null && confidences.size == results.size) {
            usable.maxBy { confidences[it] }
        } else {
            usable.first()
        }
        return QueryText.clean(results[pick].orEmpty())
    }
}
