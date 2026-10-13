package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

/** Every text the search components show, so you can translate them. */
@Immutable
public data class TvSearchLabels(
    public val placeholder: String = "Search",
    public val voiceSearch: String = "Search by voice",
    public val space: String = "Space",
    public val backspace: String = "Backspace",
    public val clear: String = "Clear",
    public val recentSearches: String = "Recent searches",
    public val suggestions: String = "Suggestions",
    public val searching: String = "Searching…",
    public val emptyTitle: String = "Find something to watch",
    public val emptyMessage: String = "Type with the keyboard, pick a recent search or use the mic to search by voice.",
    public val noResultsMessage: String = "Check the spelling, or try a shorter or more general search.",
    public val noResultsTitle: (query: String) -> String = { "No results for “$it”" },
    public val resultsTitle: (query: String) -> String = { "Results for “$it”" },
    public val resultCount: (count: Int) -> String = { if (it == 1) "1 result" else "$it results" },
)

/** Colors of the keyboard keys. */
@Immutable
public data class TvKeyboardColors(
    public val key: Color,
    public val onKey: Color,
    public val actionKey: Color,
    public val onActionKey: Color,
    public val focusedKey: Color,
    public val onFocusedKey: Color,
)

/** Sizes, paddings and colors used by the search components. */
public object TvSearchDefaults {
    /** Width of a one column key. */
    public val KeyWidth: Dp = 46.dp

    /** Height of a key. */
    public val KeyHeight: Dp = 42.dp

    /** Space between keys. */
    public val KeySpacing: Dp = 6.dp

    /** Corner shape of keys. */
    public val KeyShape: RoundedCornerShape = RoundedCornerShape(10.dp)

    /** Focused keys grow by this factor. */
    public const val KeyFocusedScale: Float = 1.1f

    /** Space between the keyboard column and the results column. */
    public val PaneSpacing: Dp = 36.dp

    /**
     * Keeps content inside the TV safe area: 5% of a 960 x 540 dp screen (48 dp left and right,
     * 27 dp top and bottom), so no text is cut by overscan.
     */
    public val SafeAreaPadding: PaddingValues = PaddingValues(horizontal = 48.dp, vertical = 27.dp)

    /** Padding around the results, leaving room for the focus scale and border. */
    public val ResultsContentPadding: PaddingValues = PaddingValues(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)

    /** Height of the query bar and the voice button. */
    public val QueryBarHeight: Dp = 48.dp

    /** The keyboard colors from the current [MaterialTheme]: light keys focus to the inverse colors. */
    @Composable
    public fun keyboardColors(
        key: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        onKey: Color = MaterialTheme.colorScheme.onSurface,
        actionKey: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
        onActionKey: Color = MaterialTheme.colorScheme.onSurface,
        focusedKey: Color = MaterialTheme.colorScheme.onSurface,
        onFocusedKey: Color = MaterialTheme.colorScheme.surface,
    ): TvKeyboardColors = TvKeyboardColors(key, onKey, actionKey, onActionKey, focusedKey, onFocusedKey)
}
