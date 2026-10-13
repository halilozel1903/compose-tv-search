package io.github.halilozel1903.tvsearch.core

/**
 * Focus memory for the results: which result gets focus when the user comes back from the keyboard,
 * the suggestions or another screen.
 */
public object ResultFocus {

    /**
     * The index to focus in [keys] (the keys of the current results):
     *
     * - the index of [lastKey] when that result is still there (results were reordered or refreshed),
     * - otherwise [lastIndex] clamped to the results (the result there was removed),
     * - `-1` when there are no results.
     */
    public fun <K> restoreIndex(keys: List<K>, lastKey: K?, lastIndex: Int): Int {
        if (keys.isEmpty()) return -1
        if (lastKey != null) {
            val index = keys.indexOf(lastKey)
            if (index >= 0) return index
        }
        return lastIndex.coerceIn(0, keys.lastIndex)
    }
}
