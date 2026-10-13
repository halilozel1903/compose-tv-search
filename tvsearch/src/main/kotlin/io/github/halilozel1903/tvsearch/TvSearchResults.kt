package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.tvsearch.core.ResultFocus

/** How [TvSearchResults] arranges the results. */
public sealed interface SearchResultsLayout {
    /** One result per row, for [SearchResultRow]s. */
    public data object Rows : SearchResultsLayout

    /** A grid with [columns] results per row, for [SearchResultCard]s. */
    public data class Grid(public val columns: Int = 4) : SearchResultsLayout {
        init {
            require(columns >= 1) { "columns must be at least 1" }
        }
    }
}

/**
 * The focusable results of a search, as rows or as a grid.
 *
 * - Focus restoration: coming back from the keyboard or the suggestions focuses the result that
 *   was focused last; if it is gone, the one at the same position; the first one after a new query.
 * - [TvSearchState.requestFocus] with [SearchFocusTarget.Result] scrolls to a result and focuses it.
 * - Each new query starts scrolled to the top.
 *
 * [itemContent] draws one result and must contain a focusable element, such as
 * [SearchResultRow], [SearchResultCard] or a tv-material `Card`.
 *
 * @param key a stable, unique key per result (an id), used for focus memory and item reuse.
 */
@Composable
public fun <T> TvSearchResults(
    results: List<T>,
    key: (T) -> Any,
    state: TvSearchState,
    modifier: Modifier = Modifier,
    layout: SearchResultsLayout = SearchResultsLayout.Rows,
    contentPadding: PaddingValues = TvSearchDefaults.ResultsContentPadding,
    spacing: Dp = 12.dp,
    itemContent: @Composable (item: T) -> Unit,
) {
    if (results.isEmpty()) return
    val keys = remember(results) { results.map(key) }
    val requesters = remember { HashMap<Any, FocusRequester>() }
    val fallback = remember { FocusRequester() }
    val query = state.debouncedQuery

    when (layout) {
        SearchResultsLayout.Rows -> {
            val listState = rememberSaveable(query, saver = LazyListState.Saver) { LazyListState() }
            val visible by remember(listState) {
                derivedStateOf { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            }
            val fallbackIndex = fallbackIndex(keys, state, visible, listState.firstVisibleItemIndex)
            PendingResultFocus(state, keys, requesters, isVisible = { it in visible }, scrollTo = { listState.scrollToItem(it) })
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fadingEdges(top = listState.canScrollBackward, bottom = listState.canScrollForward)
                    .resultsFocusGroup(state, fallback),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(spacing),
            ) {
                itemsIndexed(results, key = { _, item -> key(item) }) { index, item ->
                    ResultItem(keys[index], index, index == fallbackIndex, fallback, requesters, state) { itemContent(item) }
                }
            }
        }
        is SearchResultsLayout.Grid -> {
            val gridState = rememberSaveable(query, saver = LazyGridState.Saver) { LazyGridState() }
            val visible by remember(gridState) {
                derivedStateOf { gridState.layoutInfo.visibleItemsInfo.map { it.index } }
            }
            val fallbackIndex = fallbackIndex(keys, state, visible, gridState.firstVisibleItemIndex)
            PendingResultFocus(state, keys, requesters, isVisible = { it in visible }, scrollTo = { gridState.scrollToItem(it) })
            LazyVerticalGrid(
                columns = GridCells.Fixed(layout.columns),
                state = gridState,
                modifier = modifier
                    .fadingEdges(top = gridState.canScrollBackward, bottom = gridState.canScrollForward)
                    .resultsFocusGroup(state, fallback),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                itemsIndexed(results, key = { _, item -> key(item) }) { index, item ->
                    ResultItem(keys[index], index, index == fallbackIndex, fallback, requesters, state) { itemContent(item) }
                }
            }
        }
    }
}

private fun Modifier.resultsFocusGroup(state: TvSearchState, fallback: FocusRequester): Modifier =
    onFocusChanged { if (it.hasFocus) state.focusArea = SearchFocusArea.Results }
        .focusRestorer(fallback)

/** The remembered result when it is composed, otherwise the first visible one. */
private fun fallbackIndex(keys: List<Any>, state: TvSearchState, visible: List<Int>, firstVisible: Int): Int {
    val wanted = ResultFocus.restoreIndex(keys, state.lastFocusedResultKey, state.lastFocusedResultIndex)
    return if (visible.isEmpty() || wanted in visible) wanted else firstVisible.coerceIn(0, keys.lastIndex)
}

@Composable
private fun PendingResultFocus(
    state: TvSearchState,
    keys: List<Any>,
    requesters: Map<Any, FocusRequester>,
    isVisible: (Int) -> Boolean,
    scrollTo: suspend (Int) -> Unit,
) {
    val pending = state.pendingFocus
    LaunchedEffect(pending, keys) {
        val target = pending as? SearchFocusTarget.Result ?: return@LaunchedEffect
        if (keys.isEmpty()) return@LaunchedEffect
        val index = target.index.coerceIn(0, keys.lastIndex)
        // Entering the list goes through its focus restorer, which moves focus to the remembered
        // result: make the target the remembered one first.
        state.targetResult(keys[index], index)
        withFrameNanos { }
        if (!isVisible(index)) scrollTo(index)
        // The item may need a frame or two to be composed after scrolling.
        repeat(10) {
            withFrameNanos { }
            val requester = requesters[keys[index]]
            if (requester != null && runCatching { requester.requestFocus() }.isSuccess) {
                state.consumePendingFocus(target)
                return@LaunchedEffect
            }
        }
    }
}

@Composable
private fun ResultItem(
    key: Any,
    index: Int,
    isFallback: Boolean,
    fallback: FocusRequester,
    requesters: HashMap<Any, FocusRequester>,
    state: TvSearchState,
    content: @Composable () -> Unit,
) {
    val requester = remember { FocusRequester() }
    DisposableEffect(key, requester) {
        requesters[key] = requester
        onDispose { if (requesters[key] === requester) requesters.remove(key) }
    }
    Box(
        Modifier
            .focusRequester(requester)
            .then(if (isFallback) Modifier.focusRequester(fallback) else Modifier)
            .onFocusChanged { if (it.hasFocus) state.onResultFocused(key, index) },
    ) {
        content()
    }
}
