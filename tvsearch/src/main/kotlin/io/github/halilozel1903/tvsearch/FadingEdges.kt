package io.github.halilozel1903.tvsearch

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fades out the content at the edges where a scrollable list has more to show, so a partly
 * visible item reads as "scroll for more" instead of being cut off. Layout direction is ignored:
 * [start] is the left edge and [end] the right one.
 */
internal fun Modifier.fadingEdges(
    top: Boolean = false,
    bottom: Boolean = false,
    start: Boolean = false,
    end: Boolean = false,
    length: Dp = 48.dp,
): Modifier {
    if (!top && !bottom && !start && !end) return this
    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val px = length.toPx()
            if (top) {
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = 0f, endY = px),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (bottom) {
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = size.height - px, endY = size.height),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (start) {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = px),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (end) {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = size.width - px, endX = size.width),
                    blendMode = BlendMode.DstIn,
                )
            }
        }
}
