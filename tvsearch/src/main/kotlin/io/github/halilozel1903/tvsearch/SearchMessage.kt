package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * A centered icon, title and message, for the empty search and the no results state.
 * See [SearchEmptyState] and [NoResultsState].
 */
@Composable
public fun SearchMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: SearchIcon = SearchIcon.Search,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.onSurface.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                SearchIconImage(icon, tint = colors.onSurfaceVariant, size = 34.dp)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** What the results area shows before anything is typed. */
@Composable
public fun SearchEmptyState(modifier: Modifier = Modifier, labels: TvSearchLabels = TvSearchLabels()) {
    SearchMessage(labels.emptyTitle, labels.emptyMessage, modifier, SearchIcon.Search)
}

/** What the results area shows when [query] found nothing. */
@Composable
public fun NoResultsState(query: String, modifier: Modifier = Modifier, labels: TvSearchLabels = TvSearchLabels()) {
    SearchMessage(labels.noResultsTitle(query), labels.noResultsMessage, modifier, SearchIcon.Clear)
}
