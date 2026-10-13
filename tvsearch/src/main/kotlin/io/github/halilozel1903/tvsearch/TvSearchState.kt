package io.github.halilozel1903.tvsearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import io.github.halilozel1903.tvsearch.core.KeyboardCursor
import io.github.halilozel1903.tvsearch.core.KeyboardKey
import io.github.halilozel1903.tvsearch.core.QueryEditor
import io.github.halilozel1903.tvsearch.core.RecentSearches
import io.github.halilozel1903.tvsearch.core.SearchConfig
import io.github.halilozel1903.tvsearch.core.debounceQuery

/** Where focus can be sent with [TvSearchState.requestFocus]. */
public sealed interface SearchFocusTarget {
    /** The keyboard key the user focused last (the first key on a fresh screen). */
    public data object Keyboard : SearchFocusTarget

    /** The keyboard key typing [char]; ignored when the layout has no such key. */
    public data class Key(public val char: Char) : SearchFocusTarget

    /** The voice search button. */
    public data object VoiceButton : SearchFocusTarget

    /** The first suggestion or recent search chip. */
    public data object Suggestions : SearchFocusTarget

    /** The result at [index] (clamped to the results); the screen falls back to the keyboard when there are none. */
    public data class Result(public val index: Int = 0) : SearchFocusTarget
}

/** The part of the search screen that holds focus. */
public enum class SearchFocusArea { VoiceButton, Keyboard, Suggestions, Results }

/**
 * State of a search screen: the typed query, the debounced query to search, recent searches and
 * focus memory for the keyboard and the results.
 *
 * Create it with [rememberTvSearchState], which also runs the debounce. Search with
 * [debouncedQuery], not [query]: it only changes once typing pauses for
 * [SearchConfig.debounceMillis], and right away when the query is cleared or [submit]ted.
 */
@Stable
public class TvSearchState(
    initialQuery: String = "",
    initialRecentSearches: List<String> = emptyList(),
    public val config: SearchConfig = SearchConfig(),
    initialFocus: SearchFocusTarget? = SearchFocusTarget.Keyboard,
) {
    /** What the keyboard (or voice search) has typed so far. */
    public var query: String by mutableStateOf(initialQuery.take(config.maxQueryLength))
        private set

    private var debounced by mutableStateOf(config.effectiveQuery(initialQuery.take(config.maxQueryLength)))

    /** The query to search: [query] cleaned and debounced; `""` means show no results. */
    public val debouncedQuery: String get() = debounced

    /** `true` while [query] differs from [debouncedQuery], that is while the user is still typing. */
    public val isSettling: Boolean get() = config.effectiveQuery(query) != debounced

    /** `true` when there is a query to show results for. */
    public val hasQuery: Boolean get() = debounced.isNotEmpty()

    private var recent by mutableStateOf(RecentSearches(initialRecentSearches, config.recentCapacity))

    /** Recent searches, most recent first. */
    public val recentSearches: List<String> get() = recent.items

    /** The keyboard key that has (or last had) focus. */
    public var keyboardCursor: KeyboardCursor by mutableStateOf(KeyboardCursor())

    /** The key of the result that had focus last, or `null`. */
    public var lastFocusedResultKey: Any? by mutableStateOf(null)
        private set

    /** The index of the result that had focus last. */
    public var lastFocusedResultIndex: Int by mutableIntStateOf(0)
        private set

    /** The part of the screen that has focus, or `null` when focus is elsewhere. */
    public var focusArea: SearchFocusArea? by mutableStateOf(null)
        internal set

    internal var pendingFocus: SearchFocusTarget? by mutableStateOf(initialFocus)
        private set

    /** Applies a keyboard key to [query]. */
    public fun type(key: KeyboardKey) {
        query = QueryEditor.apply(query, key, config.maxQueryLength)
    }

    /**
     * Replaces the query. With [immediate] the [debouncedQuery] follows at once (for a suggestion,
     * a voice result or a deep link); otherwise it follows after the debounce delay.
     */
    public fun updateQuery(text: String, immediate: Boolean = false) {
        query = text.take(config.maxQueryLength)
        if (immediate) applyDebounced(config.effectiveQuery(query))
    }

    /** Clears the query and the results. */
    public fun clear() {
        updateQuery("", immediate = true)
    }

    /** Searches [text] now (no debounce) and records it as a recent search. */
    public fun submit(text: String = query) {
        updateQuery(text, immediate = true)
        recordSearch(debounced)
    }

    /** Adds [text] to the recent searches, for example when a result of it is opened. */
    public fun recordSearch(text: String = debounced) {
        recent = recent.record(text)
    }

    /** Removes [text] from the recent searches. */
    public fun removeRecentSearch(text: String) {
        recent = recent.remove(text)
    }

    /** Forgets all recent searches. */
    public fun clearRecentSearches() {
        recent = recent.clear()
    }

    /** Moves focus to [target] as soon as it is on screen. */
    public fun requestFocus(target: SearchFocusTarget) {
        pendingFocus = target
    }

    internal fun consumePendingFocus(target: SearchFocusTarget) {
        if (pendingFocus == target) pendingFocus = null
    }

    /**
     * Marks the result a focus request is about to target, so the results' focus restorer, which
     * redirects focus entering the list to the remembered result, lands on that result.
     */
    internal fun targetResult(key: Any, index: Int) {
        lastFocusedResultKey = key
        lastFocusedResultIndex = index
    }

    internal fun onResultFocused(key: Any, index: Int) {
        lastFocusedResultKey = key
        lastFocusedResultIndex = index
        focusArea = SearchFocusArea.Results
    }

    private fun applyDebounced(value: String) {
        if (value == debounced) return
        debounced = value
        // A new search starts at its first result.
        lastFocusedResultKey = null
        lastFocusedResultIndex = 0
    }

    /** Debounces [query] into [debouncedQuery] until cancelled. [rememberTvSearchState] runs this for you. */
    public suspend fun runDebounce() {
        snapshotFlow { query }.debounceQuery(config).collect { applyDebounced(it) }
    }

    public companion object {
        /** Saves the query, recent searches and keyboard position. Focus goes back to the keyboard after a restore. */
        public fun saver(config: SearchConfig = SearchConfig()): Saver<TvSearchState, Any> =
            listSaver<TvSearchState, Any>(
                save = { state ->
                    listOf<Any>(
                        state.query,
                        ArrayList<String>(state.recentSearches),
                        state.keyboardCursor.row,
                        state.keyboardCursor.index,
                        state.keyboardCursor.preferredColumn,
                    )
                },
                restore = { saved ->
                    @Suppress("UNCHECKED_CAST")
                    TvSearchState(
                        initialQuery = saved[0] as String,
                        initialRecentSearches = saved[1] as List<String>,
                        config = config,
                    ).apply {
                        keyboardCursor = KeyboardCursor(saved[2] as Int, saved[3] as Int, saved[4] as Int)
                    }
                },
            )
    }
}

/**
 * Creates and remembers a [TvSearchState] that survives configuration changes and process death,
 * and runs its debounce while it is in the composition.
 *
 * @param initialQuery the query to start with, searched right away.
 * @param initialRecentSearches recent searches to start with (load them from your storage).
 * @param config debounce delay, query length limits and recent search capacity.
 * @param initialFocus where focus goes when the screen appears; `null` leaves focus alone.
 */
@Composable
public fun rememberTvSearchState(
    initialQuery: String = "",
    initialRecentSearches: List<String> = emptyList(),
    config: SearchConfig = SearchConfig(),
    initialFocus: SearchFocusTarget? = SearchFocusTarget.Keyboard,
): TvSearchState {
    val state = rememberSaveable(config, saver = TvSearchState.saver(config)) {
        TvSearchState(initialQuery, initialRecentSearches, config, initialFocus)
    }
    LaunchedEffect(state) { state.runDebounce() }
    return state
}
