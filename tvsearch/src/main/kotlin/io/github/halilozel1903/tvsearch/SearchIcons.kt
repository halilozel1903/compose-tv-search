package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The icons the search components draw. They are drawn with paths, so no icon library is needed. */
public enum class SearchIcon { Search, Mic, Backspace, Space, History, Clear }

/** Draws [icon] in [tint]. */
@Composable
public fun SearchIconImage(icon: SearchIcon, tint: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val stroke = Stroke(width = this.size.minDimension * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            SearchIcon.Search -> magnifier(tint, stroke)
            SearchIcon.Mic -> mic(tint, stroke)
            SearchIcon.Backspace -> backspace(tint, stroke)
            SearchIcon.Space -> space(tint, stroke)
            SearchIcon.History -> history(tint, stroke)
            SearchIcon.Clear -> cross(tint, stroke)
        }
    }
}

private fun DrawScope.magnifier(tint: Color, stroke: Stroke) {
    val w = size.width
    drawCircle(tint, radius = w * 0.28f, center = Offset(w * 0.42f, w * 0.42f), style = stroke)
    drawLine(tint, Offset(w * 0.63f, w * 0.63f), Offset(w * 0.86f, w * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.mic(tint: Color, stroke: Stroke) {
    val w = size.width
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.36f, w * 0.08f),
        size = Size(w * 0.28f, w * 0.5f),
        cornerRadius = CornerRadius(w * 0.14f),
    )
    drawArc(
        tint,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.22f, w * 0.18f),
        size = Size(w * 0.56f, w * 0.56f),
        style = stroke,
    )
    drawLine(tint, Offset(w * 0.5f, w * 0.74f), Offset(w * 0.5f, w * 0.9f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.36f, w * 0.9f), Offset(w * 0.64f, w * 0.9f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.backspace(tint: Color, stroke: Stroke) {
    val w = size.width
    val outline = Path().apply {
        moveTo(w * 0.3f, w * 0.22f)
        lineTo(w * 0.9f, w * 0.22f)
        lineTo(w * 0.9f, w * 0.78f)
        lineTo(w * 0.3f, w * 0.78f)
        lineTo(w * 0.08f, w * 0.5f)
        close()
    }
    drawPath(outline, tint, style = stroke)
    drawLine(tint, Offset(w * 0.46f, w * 0.38f), Offset(w * 0.7f, w * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.7f, w * 0.38f), Offset(w * 0.46f, w * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.space(tint: Color, stroke: Stroke) {
    val w = size.width
    val path = Path().apply {
        moveTo(w * 0.1f, w * 0.42f)
        lineTo(w * 0.1f, w * 0.66f)
        lineTo(w * 0.9f, w * 0.66f)
        lineTo(w * 0.9f, w * 0.42f)
    }
    drawPath(path, tint, style = stroke)
}

private fun DrawScope.history(tint: Color, stroke: Stroke) {
    val w = size.width
    drawArc(
        tint,
        startAngle = -150f,
        sweepAngle = 300f,
        useCenter = false,
        topLeft = Offset(w * 0.14f, w * 0.14f),
        size = Size(w * 0.72f, w * 0.72f),
        style = stroke,
    )
    val head = Path().apply {
        moveTo(w * 0.08f, w * 0.2f)
        lineTo(w * 0.2f, w * 0.38f)
        lineTo(w * 0.36f, w * 0.26f)
    }
    drawPath(head, tint, style = stroke)
    drawLine(tint, Offset(w * 0.5f, w * 0.32f), Offset(w * 0.5f, w * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.5f, w * 0.52f), Offset(w * 0.64f, w * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.cross(tint: Color, stroke: Stroke) {
    val w = size.width
    drawLine(tint, Offset(w * 0.24f, w * 0.24f), Offset(w * 0.76f, w * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.76f, w * 0.24f), Offset(w * 0.24f, w * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
