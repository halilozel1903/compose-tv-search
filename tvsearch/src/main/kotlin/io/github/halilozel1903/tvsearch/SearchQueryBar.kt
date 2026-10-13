package io.github.halilozel1903.tvsearch

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * The query field above the keyboard. It is not focusable: on TV the keyboard types into it.
 * Shows [placeholder] while [query] is empty, and a blinking caret after the text. Long queries
 * keep their end visible.
 */
@Composable
public fun SearchQueryBar(
    query: String,
    modifier: Modifier = Modifier,
    placeholder: String = TvSearchLabels().placeholder,
    showCaret: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(TvSearchDefaults.QueryBarHeight)
            .clip(RoundedCornerShape(TvSearchDefaults.QueryBarHeight / 2))
            .background(colors.onSurface.copy(alpha = 0.08f))
            .padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchIconImage(SearchIcon.Search, tint = colors.onSurfaceVariant, size = 22.dp)
        Spacer(Modifier.width(10.dp))
        if (query.isEmpty()) {
            if (showCaret) Caret()
            Text(
                text = placeholder,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = query,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis,
            )
            if (showCaret) Caret()
        }
    }
}

@Composable
private fun Caret() {
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 530), RepeatMode.Reverse),
        label = "caret alpha",
    )
    Box(
        Modifier
            .padding(horizontal = 2.dp)
            .size(width = 2.dp, height = 22.dp)
            // Visible in the second half of each blink, and for good when animations are switched off.
            .alpha(if (alpha <= 0.5f) 1f else 0f)
            .background(MaterialTheme.colorScheme.primary),
    )
}

/**
 * A round, focusable microphone button that starts voice search. Pair it with
 * [rememberVoiceSearchLauncher]: `VoiceSearchButton(onClick = { launcher.launch() })`.
 */
@Composable
public fun VoiceSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = TvSearchLabels().voiceSearch,
    size: Dp = TvSearchDefaults.QueryBarHeight,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription },
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.onSurface.copy(alpha = 0.12f),
            contentColor = colors.onSurface,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
            pressedContainerColor = colors.primary.copy(alpha = 0.85f),
            pressedContentColor = colors.onPrimary,
            disabledContainerColor = colors.onSurface.copy(alpha = 0.05f),
            disabledContentColor = colors.onSurface.copy(alpha = 0.4f),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
    ) {
        SearchIconImage(
            icon = SearchIcon.Mic,
            tint = LocalContentColor.current,
            modifier = Modifier.align(Alignment.Center),
            size = size * 0.5f,
        )
    }
}

/** The voice button and the query bar side by side, as [TvSearchScreen] shows them. */
@Composable
public fun SearchQueryHeader(
    state: TvSearchState,
    modifier: Modifier = Modifier,
    voiceSearch: VoiceSearchLauncher? = null,
    labels: TvSearchLabels = TvSearchLabels(),
    voiceButtonModifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (voiceSearch != null) {
            VoiceSearchButton(
                onClick = { voiceSearch.launch() },
                modifier = voiceButtonModifier.onFocusChanged {
                    if (it.hasFocus) state.focusArea = SearchFocusArea.VoiceButton
                },
                contentDescription = labels.voiceSearch,
            )
            Spacer(Modifier.width(12.dp))
        }
        SearchQueryBar(
            query = state.query,
            modifier = Modifier.weight(1f),
            placeholder = labels.placeholder,
            showCaret = state.focusArea == SearchFocusArea.Keyboard,
        )
    }
}

