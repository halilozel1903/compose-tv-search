package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

private val RowShape = RoundedCornerShape(14.dp)
private val ArtworkShape = RoundedCornerShape(8.dp)

/**
 * A focusable result row: a 16:9 [artwork] on the left, the [title], a [subtitle] (year, genre,
 * length) and an optional two line [description]. Focus lights up the row, draws a border and
 * scales it up slightly.
 *
 * @param badge a short label over the artwork, such as `"NEW"`.
 * @param artworkWidth the width of the artwork; its height is 9/16 of it.
 */
@Composable
public fun SearchResultRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    description: String? = null,
    badge: String? = null,
    artworkWidth: Dp = 128.dp,
    interactionSource: MutableInteractionSource? = null,
    artwork: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape = RowShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.onSurface.copy(alpha = 0.05f),
            contentColor = colors.onSurface,
            focusedContainerColor = colors.surfaceVariant,
            focusedContentColor = colors.onSurface,
            pressedContainerColor = colors.surfaceVariant,
            pressedContentColor = colors.onSurface,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, colors.onSurface), shape = RowShape),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = colors.primary.copy(alpha = 0.35f), elevation = 12.dp),
        ),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(artworkWidth, artworkWidth * 9f / 16f)
                    .clip(ArtworkShape)
                    .background(colors.surfaceVariant),
            ) {
                artwork()
                if (badge != null) ResultBadge(badge, Modifier.align(Alignment.TopStart).padding(6.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!description.isNullOrEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * A focusable poster card for [SearchResultsLayout.Grid]: the [artwork] in a 2:3 frame that scales
 * up with a border and glow on focus, and the title below it.
 */
@Composable
public fun SearchResultCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    aspectRatio: Float = 2f / 3f,
    interactionSource: MutableInteractionSource? = null,
    artwork: @Composable BoxScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            shape = CardDefaults.shape(shape = ArtworkShape),
            colors = CardDefaults.colors(containerColor = colors.surfaceVariant),
            scale = CardDefaults.scale(focusedScale = 1.08f),
            border = CardDefaults.border(
                focusedBorder = Border(border = BorderStroke(3.dp, colors.onSurface), shape = ArtworkShape),
            ),
            glow = CardDefaults.glow(
                focusedGlow = Glow(elevationColor = colors.primary.copy(alpha = 0.5f), elevation = 16.dp),
            ),
            interactionSource = source,
        ) {
            Box(Modifier.fillMaxSize()) { artwork() }
        }
        Spacer(Modifier.height(if (focused) 12.dp else 8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (focused) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ResultBadge(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = 1,
    )
}
