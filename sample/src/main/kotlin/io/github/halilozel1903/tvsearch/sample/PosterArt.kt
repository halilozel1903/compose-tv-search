package io.github.halilozel1903.tvsearch.sample

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Generated artwork for the demo catalog: a gradient and a simple motif (sun and hills, rings or a
 * skyline) picked from the title's id, so every title has its own stable look without image files.
 */
@Composable
fun PosterArt(title: DemoTitle, modifier: Modifier = Modifier) {
    val seed = title.id.fold(17) { acc, c -> acc * 31 + c.code } and Int.MAX_VALUE
    val palette = Palettes[seed % Palettes.size]
    Canvas(modifier) {
        drawRect(Brush.linearGradient(listOf(palette.first, palette.second), start = Offset.Zero, end = Offset(size.width, size.height)))
        when ((seed / Palettes.size) % 3) {
            0 -> sunAndHills(palette.accent)
            1 -> rings(palette.accent)
            else -> skyline(palette.accent)
        }
    }
}

private class Palette(val first: Color, val second: Color, val accent: Color)

private val Palettes = listOf(
    Palette(Color(0xFF1E3C72), Color(0xFF2A5298), Color(0xFFFFC371)),
    Palette(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B)),
    Palette(Color(0xFF0F2027), Color(0xFF2C5364), Color(0xFF7FE7D3)),
    Palette(Color(0xFF8E2DE2), Color(0xFF4A00E0), Color(0xFFFF9ECF)),
    Palette(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFFFFF3B0)),
    Palette(Color(0xFFB24592), Color(0xFFF15F79), Color(0xFFFFE29F)),
    Palette(Color(0xFF232526), Color(0xFF414345), Color(0xFFFF7A59)),
    Palette(Color(0xFF4568DC), Color(0xFFB06AB3), Color(0xFFFFD6A5)),
    Palette(Color(0xFF7B4397), Color(0xFFDC2430), Color(0xFFFFC857)),
)

private fun DrawScope.sunAndHills(accent: Color) {
    val w = size.width
    val h = size.height
    val sun = Offset(w * 0.7f, h * 0.38f)
    drawCircle(Brush.radialGradient(listOf(accent, accent.copy(alpha = 0f)), center = sun, radius = h * 0.55f), radius = h * 0.55f, center = sun)
    drawCircle(accent, radius = h * 0.16f, center = sun)
    val far = Path().apply {
        moveTo(0f, h * 0.72f)
        cubicTo(w * 0.25f, h * 0.52f, w * 0.45f, h * 0.8f, w * 0.7f, h * 0.6f)
        cubicTo(w * 0.85f, h * 0.5f, w * 0.95f, h * 0.62f, w, h * 0.58f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(far, Color.Black.copy(alpha = 0.28f))
    val near = Path().apply {
        moveTo(0f, h * 0.86f)
        cubicTo(w * 0.3f, h * 0.7f, w * 0.6f, h * 0.95f, w, h * 0.78f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(near, Color.Black.copy(alpha = 0.45f))
}

private fun DrawScope.rings(accent: Color) {
    val center = Offset(size.width * 0.72f, size.height * 0.45f)
    val unit = size.minDimension
    for (i in 1..6) {
        drawCircle(accent.copy(alpha = 0.55f - i * 0.07f), radius = unit * 0.1f * i, center = center, style = Stroke(width = unit * 0.025f))
    }
    drawCircle(accent, radius = unit * 0.06f, center = center)
    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), startY = size.height * 0.5f))
}

private fun DrawScope.skyline(accent: Color) {
    val w = size.width
    val h = size.height
    drawCircle(accent.copy(alpha = 0.85f), radius = h * 0.12f, center = Offset(w * 0.22f, h * 0.28f))
    val heights = listOf(0.42f, 0.6f, 0.35f, 0.7f, 0.5f, 0.38f, 0.64f, 0.46f, 0.3f, 0.55f)
    val bar = w / heights.size
    heights.forEachIndexed { i, fraction ->
        val top = h * (1f - fraction)
        drawRect(Color.Black.copy(alpha = 0.35f + (i % 3) * 0.08f), topLeft = Offset(i * bar, top), size = Size(bar * 0.9f, h - top))
        if (i % 2 == 0) {
            drawRect(accent.copy(alpha = 0.7f), topLeft = Offset(i * bar + bar * 0.3f, top + h * 0.06f), size = Size(bar * 0.18f, h * 0.03f))
        }
    }
}
