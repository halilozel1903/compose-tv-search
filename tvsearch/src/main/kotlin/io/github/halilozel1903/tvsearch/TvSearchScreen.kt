package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.halilozel1903.tvsearch.core.KeyboardLayout

/**
 * A complete Android TV search screen: the voice button and query bar over the keyboard on the
 * left; recent searches or suggestions, then the results (or an empty or no results state) on the
 * right. Everything stays inside the TV safe area.
 *
 * The screen does not search by itself. Compute [results] (and [suggestions]) from
 * [TvSearchState.debouncedQuery]:
 *
 * ```kotlin
 * val state = rememberTvSearchState()
 * val results = remember(state.debouncedQuery) { catalog.search(state.debouncedQuery) }
 * TvSearchScreen(state, results, resultKey = { it.id }) { movie ->
 *     SearchResultRow(movie.title, onClick = { state.recordSearch(); open(movie) }) { Poster(movie) }
 * }
 * ```
 *
 * Call `state.recordSearch()` when a result is opened, so the query becomes a recent search.
 *
 * @param results the results for [TvSearchState.debouncedQuery].
 * @param resultKey a stable, unique key per result.
 * @param suggestions completions shown while there is a query, for example from
 * [io.github.halilozel1903.tvsearch.core.Suggestions.rank]. Recent searches are shown while the query is empty.
 * @param onSuggestionClick called for suggestion and recent search chips; the default searches the
 * text right away and moves focus to the first result.
 * @param voiceSearch shows the mic button when not `null`; see [rememberVoiceSearchLauncher].
 * @param keyboardLayout the keyboard keys.
 * @param resultsLayout rows or a grid.
 * @param isLoading `true` while your results are loading; shows "Searching…" instead of "No results".
 * @param keyboardFooter shown under the keyboard, for search tips or a note about the catalog.
 * @param resultContent draws one result; it must contain a focusable element such as [SearchResultRow].
 */
@Composable
public fun <T> TvSearchScreen(
    state: TvSearchState,
    results: List<T>,
    resultKey: (T) -> Any,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: (String) -> Unit = { text ->
        state.submit(text)
        state.requestFocus(SearchFocusTarget.Result(0))
    },
    voiceSearch: VoiceSearchLauncher? = null,
    keyboardLayout: KeyboardLayout = KeyboardLayout.Alphanumeric,
    uppercaseKeys: Boolean = false,
    resultsLayout: SearchResultsLayout = SearchResultsLayout.Rows,
    isLoading: Boolean = false,
    labels: TvSearchLabels = TvSearchLabels(),
    contentPadding: PaddingValues = TvSearchDefaults.SafeAreaPadding,
    emptyContent: @Composable () -> Unit = { SearchEmptyState(Modifier.fillMaxSize(), labels) },
    noResultsContent: @Composable (query: String) -> Unit = { NoResultsState(it, Modifier.fillMaxSize(), labels) },
    keyboardFooter: @Composable () -> Unit = {},
    resultContent: @Composable (item: T) -> Unit,
) {
    val voiceRequester = remember { FocusRequester() }
    val pending = state.pendingFocus
    val waiting = state.isSettling || isLoading

    // Focus requests the screen resolves itself: the voice button, and results that turned out empty.
    LaunchedEffect(pending, results.isEmpty(), waiting, voiceSearch != null) {
        when (pending) {
            SearchFocusTarget.VoiceButton -> {
                if (voiceSearch == null) {
                    state.requestFocus(SearchFocusTarget.Keyboard)
                    return@LaunchedEffect
                }
                repeat(10) {
                    withFrameNanos { }
                    if (runCatching { voiceRequester.requestFocus() }.isSuccess) {
                        state.consumePendingFocus(SearchFocusTarget.VoiceButton)
                        return@LaunchedEffect
                    }
                }
            }
            is SearchFocusTarget.Result -> if (results.isEmpty() && !waiting) state.requestFocus(SearchFocusTarget.Keyboard)
            else -> Unit
        }
    }

    val keyboardWidth = TvSearchDefaults.KeyWidth * keyboardLayout.columnCount +
        TvSearchDefaults.KeySpacing * (keyboardLayout.columnCount - 1)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(TvSearchDefaults.PaneSpacing),
    ) {
        Column(Modifier.width(keyboardWidth).fillMaxHeight()) {
            SearchQueryHeader(
                state = state,
                voiceSearch = voiceSearch,
                labels = labels,
                voiceButtonModifier = Modifier.focusRequester(voiceRequester),
            )
            Spacer(Modifier.height(22.dp))
            TvKeyboard(
                state = state,
                layout = keyboardLayout,
                uppercase = uppercaseKeys,
                labels = labels,
            )
            Spacer(Modifier.height(18.dp))
            keyboardFooter()
        }

        Column(Modifier.weight(1f).fillMaxHeight()) {
            val showRecent = state.query.isBlank()
            val chips = if (showRecent) state.recentSearches else suggestions
            SearchSuggestionsRow(
                title = if (showRecent) labels.recentSearches else labels.suggestions,
                suggestions = chips,
                onSuggestionClick = onSuggestionClick,
                state = state,
                icon = if (showRecent) SearchIcon.History else SearchIcon.Search,
                highlight = if (showRecent) "" else state.query,
            )
            if (chips.isNotEmpty()) Spacer(Modifier.height(14.dp))

            when {
                !state.hasQuery -> emptyContent()
                results.isEmpty() && waiting -> ResultsHeader(labels.searching, null)
                results.isEmpty() -> noResultsContent(state.debouncedQuery)
                else -> {
                    ResultsHeader(
                        title = labels.resultsTitle(state.debouncedQuery),
                        trailing = if (waiting) labels.searching else labels.resultCount(results.size),
                    )
                    Spacer(Modifier.height(4.dp))
                    TvSearchResults(
                        results = results,
                        key = resultKey,
                        state = state,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        layout = resultsLayout,
                        itemContent = resultContent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(title: String, trailing: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
