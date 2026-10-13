package io.github.halilozel1903.tvsearch.core

/** Applies key presses to a query. Pure functions, no state. */
public object QueryEditor {

    /**
     * The query after pressing [key].
     *
     * - [KeyboardKey.Character] appends its character unless the query is [maxLength] long.
     * - [KeyboardKey.Space] appends a space, but not at the start and not after another space.
     * - [KeyboardKey.Backspace] removes the last character (a whole surrogate pair for emoji).
     * - [KeyboardKey.Clear] returns an empty query.
     */
    public fun apply(query: String, key: KeyboardKey, maxLength: Int = Int.MAX_VALUE): String = when (key) {
        is KeyboardKey.Character -> if (query.length >= maxLength) query else query + key.char
        is KeyboardKey.Space -> when {
            query.isEmpty() || query.last().isWhitespace() || query.length >= maxLength -> query
            else -> "$query "
        }
        is KeyboardKey.Backspace -> backspace(query)
        is KeyboardKey.Clear -> ""
    }

    /** [query] without its last character, keeping surrogate pairs together. */
    public fun backspace(query: String): String {
        if (query.isEmpty()) return query
        val end = query.length
        val drop = if (end >= 2 && query[end - 1].isLowSurrogate() && query[end - 2].isHighSurrogate()) 2 else 1
        return query.substring(0, end - drop)
    }
}
