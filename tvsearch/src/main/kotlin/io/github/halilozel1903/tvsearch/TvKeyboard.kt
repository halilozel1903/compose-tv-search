package io.github.halilozel1903.tvsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.halilozel1903.tvsearch.core.Direction
import io.github.halilozel1903.tvsearch.core.KeyboardCursor
import io.github.halilozel1903.tvsearch.core.KeyboardKey
import io.github.halilozel1903.tvsearch.core.KeyboardLayout
import io.github.halilozel1903.tvsearch.core.KeyboardMove
import io.github.halilozel1903.tvsearch.core.KeyboardNavigator

/**
 * A D-pad friendly on-screen keyboard grid.
 *
 * - Arrow keys move between keys with [KeyboardNavigator]: up and down keep the column, also
 *   through wide keys. At the keyboard's edge the arrow key is left to the platform, so focus moves
 *   on to the results, the suggestions or the voice button.
 * - Coming back to the keyboard focuses the key that was focused last ([TvSearchState.keyboardCursor]).
 * - Center types the key into [state]; holding center on backspace clears the query.
 *
 * @param state the screen's [TvSearchState].
 * @param layout the keys; see [KeyboardLayout.Alphanumeric], [KeyboardLayout.Qwerty] and [KeyboardLayout.Alphabetic].
 * @param onKeyPress called for every key press; the default types into [state].
 * @param uppercase draw letter keys in upper case (they still type lower case letters).
 * @param wrapHorizontally left on the first key goes to the last key of the row and the other way round.
 */
@Composable
public fun TvKeyboard(
    state: TvSearchState,
    modifier: Modifier = Modifier,
    layout: KeyboardLayout = KeyboardLayout.Alphanumeric,
    onKeyPress: (KeyboardKey) -> Unit = { state.type(it) },
    uppercase: Boolean = false,
    wrapHorizontally: Boolean = false,
    labels: TvSearchLabels = TvSearchLabels(),
    colors: TvKeyboardColors = TvSearchDefaults.keyboardColors(),
    keyWidth: Dp = TvSearchDefaults.KeyWidth,
    keyHeight: Dp = TvSearchDefaults.KeyHeight,
    keySpacing: Dp = TvSearchDefaults.KeySpacing,
) {
    val navigator = remember(layout, wrapHorizontally) { KeyboardNavigator(layout, wrapHorizontally) }
    val requesters = remember(layout) { layout.rows.map { row -> row.map { FocusRequester() } } }
    val cursor = navigator.clamp(state.keyboardCursor)
    val restoreTarget = requesters[cursor.row][cursor.index]

    val pending = state.pendingFocus
    LaunchedEffect(pending, layout) {
        val target = when (pending) {
            SearchFocusTarget.Keyboard -> cursor
            is SearchFocusTarget.Key -> layout.positionOf(pending.char)?.let { navigator.cursorAt(it) }
            else -> null
        } ?: return@LaunchedEffect
        state.keyboardCursor = target
        // The keys may need a frame before they can take focus.
        repeat(10) {
            withFrameNanos { }
            if (runCatching { requesters[target.row][target.index].requestFocus() }.isSuccess) {
                state.consumePendingFocus(pending!!)
                return@LaunchedEffect
            }
        }
    }

    Column(
        modifier = modifier
            .onFocusChanged { if (it.hasFocus) state.focusArea = SearchFocusArea.Keyboard }
            .focusRestorer(restoreTarget)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val direction = when (event.key) {
                    Key.DirectionUp -> Direction.Up
                    Key.DirectionDown -> Direction.Down
                    Key.DirectionLeft -> Direction.Left
                    Key.DirectionRight -> Direction.Right
                    else -> return@onPreviewKeyEvent false
                }
                when (val move = navigator.move(navigator.clamp(state.keyboardCursor), direction)) {
                    is KeyboardMove.Moved -> {
                        state.keyboardCursor = move.cursor
                        runCatching { requesters[move.cursor.row][move.cursor.index].requestFocus() }
                        true
                    }
                    // Leave the keyboard: the platform finds the next focusable in that direction.
                    is KeyboardMove.Edge -> false
                }
            },
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        layout.rows.forEachIndexed { row, keys ->
            Row(horizontalArrangement = Arrangement.spacedBy(keySpacing)) {
                keys.forEachIndexed { index, key ->
                    KeyboardKeyButton(
                        key = key,
                        width = keyWidth * key.span + keySpacing * (key.span - 1),
                        height = keyHeight,
                        uppercase = uppercase,
                        labels = labels,
                        colors = colors,
                        onClick = { onKeyPress(key) },
                        onLongClick = if (key is KeyboardKey.Backspace) {
                            { onKeyPress(KeyboardKey.Clear()) }
                        } else {
                            null
                        },
                        modifier = Modifier
                            .focusRequester(requesters[row][index])
                            .onFocusChanged { focus ->
                                if (focus.isFocused) {
                                    val current = state.keyboardCursor
                                    // Keep the preferred column when the navigator moved here.
                                    if (current.row != row || current.index != index) {
                                        state.keyboardCursor = KeyboardCursor(row, index, layout.startColumn(row, index))
                                    }
                                }
                            },
                    )
                }
            }
        }
    }
}

/**
 * One key of [TvKeyboard]: a rounded tile that scales up and inverts its colors when focused.
 * Use it to build your own keyboard rows.
 */
@Composable
public fun KeyboardKeyButton(
    key: KeyboardKey,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    uppercase: Boolean = false,
    labels: TvSearchLabels = TvSearchLabels(),
    colors: TvKeyboardColors = TvSearchDefaults.keyboardColors(),
) {
    val isAction = key !is KeyboardKey.Character
    val description = when (key) {
        is KeyboardKey.Character -> key.char.toString()
        is KeyboardKey.Space -> labels.space
        is KeyboardKey.Backspace -> labels.backspace
        is KeyboardKey.Clear -> labels.clear
    }
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .size(width, height)
            .semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(shape = TvSearchDefaults.KeyShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isAction) colors.actionKey else colors.key,
            contentColor = if (isAction) colors.onActionKey else colors.onKey,
            focusedContainerColor = colors.focusedKey,
            focusedContentColor = colors.onFocusedKey,
            pressedContainerColor = colors.focusedKey.copy(alpha = 0.85f),
            pressedContentColor = colors.onFocusedKey,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = TvSearchDefaults.KeyFocusedScale),
    ) {
        val tint = LocalContentColor.current
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            when (key) {
                is KeyboardKey.Character -> Text(
                    text = if (uppercase) key.char.uppercase() else key.char.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = tint,
                    maxLines = 1,
                )
                is KeyboardKey.Space -> SearchIconImage(SearchIcon.Space, tint, size = TvSearchDefaults.KeyHeight * 0.5f)
                is KeyboardKey.Backspace -> SearchIconImage(SearchIcon.Backspace, tint, size = TvSearchDefaults.KeyHeight * 0.55f)
                is KeyboardKey.Clear -> Text(
                    text = labels.clear,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    maxLines = 1,
                )
            }
        }
    }
}
