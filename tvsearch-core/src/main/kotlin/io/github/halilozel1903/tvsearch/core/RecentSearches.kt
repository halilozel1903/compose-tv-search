package io.github.halilozel1903.tvsearch.core

/**
 * An immutable, most recent first list of searches.
 *
 * Entries are cleaned with [QueryText.clean]; blank entries are dropped and duplicates (ignoring
 * case, accents and extra spaces) keep only their most recent spelling. At most [capacity] entries
 * are kept.
 */
public class RecentSearches(items: List<String> = emptyList(), public val capacity: Int = 8) {

    init {
        require(capacity >= 0) { "capacity must not be negative" }
    }

    /** The searches, most recent first. */
    public val items: List<String> = normalize(items, capacity)

    /** The list after searching [query]: it moves to the front. */
    public fun record(query: String): RecentSearches {
        val cleaned = QueryText.clean(query)
        if (cleaned.isEmpty() || capacity == 0) return this
        return RecentSearches(listOf(cleaned) + items, capacity)
    }

    /** The list without [query]. */
    public fun remove(query: String): RecentSearches =
        RecentSearches(items.filterNot { QueryText.sameSearch(it, query) }, capacity)

    /** An empty list with the same capacity. */
    public fun clear(): RecentSearches = RecentSearches(emptyList(), capacity)

    /** Entries containing a word that starts with every word of [query], most recent first. */
    public fun matching(query: String): List<String> {
        val wanted = QueryText.words(query)
        if (wanted.isEmpty()) return items
        return items.filter { item ->
            val words = QueryText.words(item)
            wanted.all { token -> words.any { it.startsWith(token) } }
        }
    }

    /** A copy keeping at most [capacity] entries. */
    public fun withCapacity(capacity: Int): RecentSearches = RecentSearches(items, capacity)

    public val size: Int get() = items.size

    public fun isEmpty(): Boolean = items.isEmpty()

    override fun equals(other: Any?): Boolean =
        other is RecentSearches && other.items == items && other.capacity == capacity

    override fun hashCode(): Int = items.hashCode() * 31 + capacity

    override fun toString(): String = "RecentSearches($items, capacity=$capacity)"

    private companion object {
        fun normalize(items: List<String>, capacity: Int): List<String> {
            val seen = HashSet<String>()
            val result = ArrayList<String>(minOf(items.size, capacity))
            for (item in items) {
                if (result.size == capacity) break
                val cleaned = QueryText.clean(item)
                if (cleaned.isEmpty()) continue
                if (seen.add(QueryText.fold(cleaned))) result += cleaned
            }
            return result
        }
    }
}
