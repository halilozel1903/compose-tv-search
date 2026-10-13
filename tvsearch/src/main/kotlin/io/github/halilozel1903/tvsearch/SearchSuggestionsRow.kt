package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.halilozel1903.tvsearch.core.QueryText

/**
 * A titled row of focusable chips: recent searches (with a clock icon) or suggestions (with a
 * magnifier). The part of each chip that matches [highlight] is drawn in bold.
 *
 * Focus comes back to the chip that was focused last. [TvSearchState.requestFocus] with
 * [SearchFocusTarget.Suggestions] focuses the first chip.
 *
 * Draws nothing when [suggestions] is empty.
 */
@Composable
public fun SearchSuggestionsRow(
    title: String,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: TvSearchState? = null,
    icon: SearchIcon = SearchIcon.Search,
    highlight: String = "",
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
) {
    if (suggestions.isEmpty()) return
    val listState = rememberLazyListState()
    val first = remember { FocusRequester() }
    // The focus restorer's fallback has to sit on a composed chip: the first visible one.
    val fallback = remember { FocusRequester() }
    val fallbackIndex by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex } }
    val pending = state?.pendingFocus
    LaunchedEffect(pending, suggestions) {
        if (pending != SearchFocusTarget.Suggestions) return@LaunchedEffect
        listState.scrollToItem(0)
        repeat(10) {
            withFrameNanos { }
            if (runCatching { first.requestFocus() }.isSuccess) {
                state.consumePendingFocus(SearchFocusTarget.Suggestions)
                return@LaunchedEffect
            }
        }
    }
    Column(modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .fadingEdges(start = listState.canScrollBackward, end = listState.canScrollForward)
                .onFocusChanged { if (it.hasFocus) state?.focusArea = SearchFocusArea.Suggestions }
                .focusRestorer(fallback),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(suggestions, key = { _, text -> text }) { index, text ->
                SuggestionChip(
                    text = text,
                    icon = icon,
                    highlight = highlight,
                    onClick = { onSuggestionClick(text) },
                    modifier = Modifier
                        .then(if (index == 0) Modifier.focusRequester(first) else Modifier)
                        .then(if (index == fallbackIndex) Modifier.focusRequester(fallback) else Modifier),
                )
            }
        }
    }
}

/** One pill of [SearchSuggestionsRow]. */
@Composable
public fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SearchIcon? = SearchIcon.Search,
    highlight: String = "",
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.onSurface.copy(alpha = 0.1f),
            contentColor = colors.onSurface,
            focusedContainerColor = colors.onSurface,
            focusedContentColor = colors.surface,
            pressedContainerColor = colors.onSurface.copy(alpha = 0.85f),
            pressedContentColor = colors.surface,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
    ) {
        Row(
            modifier = Modifier.padding(start = if (icon != null) 12.dp else 18.dp, end = 18.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) SearchIconImage(icon, tint = LocalContentColor.current, size = 18.dp)
            Text(
                text = highlighted(text, highlight),
                style = MaterialTheme.typography.labelLarge,
                color = LocalContentColor.current,
                maxLines = 1,
            )
        }
    }
}

/** [text] with the first case insensitive occurrence of [query] in bold. */
internal fun highlighted(text: String, query: String): AnnotatedString {
    val cleaned = QueryText.clean(query)
    val folded = QueryText.fold(text)
    val start = if (cleaned.isEmpty() || folded.length != text.length) -1 else folded.indexOf(QueryText.fold(cleaned))
    if (start < 0) return AnnotatedString(text)
    val end = (start + cleaned.length).coerceAtMost(text.length)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(start, end)) }
        append(text.substring(end))
    }
}
