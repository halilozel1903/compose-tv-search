package io.github.halilozel1903.tvsearch.core

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Turns a flow of raw keyboard input into the queries worth searching:
 *
 * - every value becomes [SearchConfig.effectiveQuery] (cleaned, `""` when too short),
 * - a non-empty query is emitted only after [SearchConfig.debounceMillis] without a change,
 * - an empty query is emitted right away, so clearing the field clears the results at once,
 * - the same query is never emitted twice in a row (typing "ab" then backspace is not a new search).
 */
@OptIn(FlowPreview::class)
public fun Flow<String>.debounceQuery(config: SearchConfig = SearchConfig()): Flow<String> =
    map { config.effectiveQuery(it) }
        .distinctUntilChanged()
        .debounce { query -> if (query.isEmpty()) 0L else config.debounceMillis }
        .distinctUntilChanged()

/**
 * A debouncer driven by an explicit clock, for code that is not built on flows (and for tests).
 *
 * Call [input] for every change and [poll] from a timer or frame callback; [poll] returns the query
 * to search once it has been stable for [SearchConfig.debounceMillis].
 */
public class QueryDebouncer(private val config: SearchConfig = SearchConfig()) {
    private var pending: String? = null
    private var dueAt: Long = 0L

    /** The last query [poll] or [flush] returned. */
    public var current: String = ""
        private set

    /** Records new input at [nowMillis]. An empty effective query is due immediately. */
    public fun input(query: String, nowMillis: Long) {
        val effective = config.effectiveQuery(query)
        if (effective == current) {
            pending = null
            return
        }
        pending = effective
        dueAt = if (effective.isEmpty()) nowMillis else nowMillis + config.debounceMillis
    }

    /** The query to search now, or `null` when nothing new is due yet. */
    public fun poll(nowMillis: Long): String? {
        val value = pending ?: return null
        if (nowMillis < dueAt) return null
        pending = null
        current = value
        return value
    }

    /** The pending query right away (for a submit), or `null` when nothing is pending. */
    public fun flush(): String? {
        val value = pending ?: return null
        pending = null
        current = value
        return value
    }

    /** `true` while input is waiting for its debounce delay. */
    public val isPending: Boolean get() = pending != null
}
