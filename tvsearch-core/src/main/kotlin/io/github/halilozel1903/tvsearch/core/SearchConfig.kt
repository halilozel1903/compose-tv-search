package io.github.halilozel1903.tvsearch.core

/**
 * How a search screen treats the query.
 *
 * @param debounceMillis how long typing has to pause before the query is searched.
 * @param minQueryLength shorter (cleaned) queries count as empty and show no results.
 * @param maxQueryLength the keyboard stops typing at this length.
 * @param recentCapacity how many recent searches are kept.
 */
public data class SearchConfig(
    public val debounceMillis: Long = 300L,
    public val minQueryLength: Int = 1,
    public val maxQueryLength: Int = 64,
    public val recentCapacity: Int = 8,
) {
    init {
        require(debounceMillis >= 0) { "debounceMillis must not be negative" }
        require(minQueryLength >= 1) { "minQueryLength must be at least 1" }
        require(maxQueryLength >= minQueryLength) { "maxQueryLength must be at least minQueryLength" }
        require(recentCapacity >= 0) { "recentCapacity must not be negative" }
    }

    /**
     * The query that is actually searched: [query] cleaned with [QueryText.clean], or `""` when it is
     * shorter than [minQueryLength].
     */
    public fun effectiveQuery(query: String): String {
        val cleaned = QueryText.clean(query)
        return if (cleaned.length < minQueryLength) "" else cleaned
    }
}
