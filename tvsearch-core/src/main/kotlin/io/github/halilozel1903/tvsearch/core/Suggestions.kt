package io.github.halilozel1903.tvsearch.core

/** Builds the suggestion chips shown above the results. */
public object Suggestions {

    /** Extra score for an entry that is also a recent search, so your own history comes first. */
    public const val RECENT_BONUS: Int = 150

    /**
     * Up to [limit] completions of [query] taken from [recent] searches and [candidates] (titles,
     * genres, popular searches), best match first ([SearchMatcher]), recent searches boosted by
     * [RECENT_BONUS]. Duplicates and the query itself are left out.
     *
     * For an empty query this returns the first [limit] [recent] searches.
     */
    public fun rank(
        query: String,
        candidates: Iterable<String>,
        recent: List<String> = emptyList(),
        limit: Int = 6,
    ): List<String> {
        if (limit <= 0) return emptyList()
        val cleanedQuery = QueryText.clean(query)
        if (QueryText.fold(cleanedQuery).isEmpty()) return RecentSearches(recent, limit).items
        val seen = HashSet<String>()
        seen += QueryText.fold(cleanedQuery)
        val scored = buildList<Pair<String, Int>> {
            val entries = recent.asSequence().map { it to true } + candidates.asSequence().map { it to false }
            for ((entry, isRecent) in entries) {
                val cleaned = QueryText.clean(entry)
                if (cleaned.isEmpty() || !seen.add(QueryText.fold(cleaned))) continue
                val score = SearchMatcher.score(cleanedQuery, cleaned) ?: continue
                add(cleaned to score + if (isRecent) RECENT_BONUS else 0)
            }
        }
        return scored
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first.length })
            .take(limit)
            .map { it.first }
    }
}
