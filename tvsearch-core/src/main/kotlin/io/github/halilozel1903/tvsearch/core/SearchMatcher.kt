package io.github.halilozel1903.tvsearch.core

/**
 * Scores how well a text matches a query, ignoring case, accents and extra spaces.
 *
 * From best to worst:
 * 1. the text equals the query (1000),
 * 2. the text starts with the query (801 to 900),
 * 3. every query word starts a word of the text, or is part of one when it has three or more
 *    letters (201 to 599).
 *
 * Inside a group shorter texts score higher. Anything else does not match (`null`).
 */
public object SearchMatcher {
    private const val EXACT = 1000
    private const val PREFIX = 900
    private const val WORDS = 200

    /** Score taken off per field position in [filterByFields]. */
    public const val FIELD_PENALTY: Int = 10

    /** The score of [text] for [query], or `null` when it does not match. */
    public fun score(query: String, text: String): Int? {
        val q = QueryText.fold(QueryText.clean(query))
        val t = QueryText.fold(QueryText.clean(text))
        if (q.isEmpty() || t.isEmpty()) return null
        val lengthPenalty = minOf(t.length, 99)
        if (t == q) return EXACT
        if (t.startsWith(q)) return PREFIX - lengthPenalty
        val queryWords = QueryText.words(q)
        val textWords = QueryText.words(t)
        if (queryWords.isEmpty() || textWords.isEmpty()) return null
        var points = 0
        for (word in queryWords) {
            val best = textWords.maxOf { candidate ->
                when {
                    candidate == word -> 3
                    candidate.startsWith(word) -> 2
                    word.length >= 3 && candidate.contains(word) -> 1
                    else -> 0
                }
            }
            if (best == 0) return null
            points += best
        }
        val phraseBonus = if (t.contains(q)) 100 else 0
        return WORDS + points * 100 / queryWords.size + phraseBonus - lengthPenalty
    }

    /** `true` when [text] matches [query]. */
    public fun matches(query: String, text: String): Boolean = score(query, text) != null

    /**
     * The [items] whose [text] matches [query], best match first; equal scores keep their order.
     * An empty query matches nothing.
     */
    public fun <T> filter(query: String, items: Iterable<T>, limit: Int = Int.MAX_VALUE, text: (T) -> String): List<T> =
        filterByFields(query, items, limit) { listOf(text(it)) }

    /**
     * Like [filter], but an item can be found through several texts (a title, a genre, a cast
     * list). Its best scoring field counts, with [FIELD_PENALTY] taken off per position in the list,
     * so a match in the first field (the title) beats an equally good match in a later one.
     */
    public fun <T> filterByFields(
        query: String,
        items: Iterable<T>,
        limit: Int = Int.MAX_VALUE,
        fields: (T) -> List<String>,
    ): List<T> {
        if (limit <= 0) return emptyList()
        val scored = ArrayList<Pair<T, Int>>()
        for (item in items) {
            val best = fields(item)
                .mapIndexedNotNull { index, field -> score(query, field)?.minus(index * FIELD_PENALTY) }
                .maxOrNull() ?: continue
            scored += item to best
        }
        return scored.sortedByDescending { it.second }.take(limit).map { it.first }
    }
}
