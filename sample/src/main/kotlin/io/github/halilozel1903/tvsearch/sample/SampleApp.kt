package io.github.halilozel1903.tvsearch.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.halilozel1903.tvsearch.SearchFocusTarget
import io.github.halilozel1903.tvsearch.SearchResultRow
import io.github.halilozel1903.tvsearch.TvSearchLabels
import io.github.halilozel1903.tvsearch.TvSearchScreen
import io.github.halilozel1903.tvsearch.VoiceSearchRequest
import io.github.halilozel1903.tvsearch.rememberTvSearchState
import io.github.halilozel1903.tvsearch.rememberVoiceSearchLauncher

/** The screenshot scenes, see [MainActivity]. */
enum class Scene(val id: String, val query: String, val focus: SearchFocusTarget) {
    Interactive("interactive", "", SearchFocusTarget.Keyboard),
    Empty("empty", "", SearchFocusTarget.Keyboard),
    Typed("typed", "light", SearchFocusTarget.Key('t')),
    Result("result", "light", SearchFocusTarget.Result(1)),
    ;

    companion object {
        fun from(id: String?): Scene = entries.firstOrNull { it.id == id } ?: Interactive
    }
}

private val SampleLabels = TvSearchLabels(placeholder = "Search the demo catalog")

@Composable
fun SampleApp(scene: Scene) {
    val state = rememberTvSearchState(
        initialQuery = scene.query,
        initialRecentSearches = DemoCatalog.recentSearches,
        initialFocus = scene.focus,
    )
    val voiceSearch = rememberVoiceSearchLauncher(state, VoiceSearchRequest(prompt = "Say a title from the demo catalog"))
    val query = state.debouncedQuery
    val results = remember(query) { DemoCatalog.search(query) }
    val recent = state.recentSearches
    val suggestions = remember(query, recent) { DemoCatalog.suggestions(query, recent) }

    TvSearchScreen(
        state = state,
        results = results,
        resultKey = { it.id },
        modifier = Modifier.fillMaxSize(),
        suggestions = suggestions,
        voiceSearch = voiceSearch,
        labels = SampleLabels,
        keyboardFooter = {
            Text(
                text = "Demo catalog: every title and picture here is made up and bundled with the sample.",
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) { title ->
        SearchResultRow(
            title = title.title,
            onClick = { state.recordSearch() },
            subtitle = DemoCatalog.meta(title),
            description = title.synopsis,
        ) {
            PosterArt(title, Modifier.fillMaxSize())
        }
    }
}
