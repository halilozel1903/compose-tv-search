package io.github.halilozel1903.tvsearch.core

/** A D-pad direction. */
public enum class Direction { Up, Down, Left, Right }

/**
 * The focused key of a keyboard.
 *
 * [preferredColumn] is the grid column focus came from. Moving up and down keeps it, so passing
 * through a wide key (like a two column space bar) brings you back to the column you started in.
 */
public data class KeyboardCursor(
    public val row: Int = 0,
    public val index: Int = 0,
    public val preferredColumn: Int = 0,
) {
    /** The key position of this cursor. */
    public val position: KeyPosition get() = KeyPosition(row, index)
}

/** The outcome of [KeyboardNavigator.move]. */
public sealed interface KeyboardMove {
    /** Focus moves to [cursor]. */
    public data class Moved(public val cursor: KeyboardCursor) : KeyboardMove

    /** Focus is at the keyboard's edge in [direction] and should leave the keyboard. */
    public data class Edge(public val direction: Direction) : KeyboardMove
}

/**
 * D-pad navigation over a [KeyboardLayout].
 *
 * - Left and right go to the neighbour key; at the end of a row they report an [KeyboardMove.Edge]
 *   (or wrap around with [wrapHorizontally]).
 * - Up and down go to the key covering the cursor's preferred column in the next row; past the
 *   first or last row they report an [KeyboardMove.Edge].
 */
public class KeyboardNavigator(
    public val layout: KeyboardLayout,
    public val wrapHorizontally: Boolean = false,
) {
    /** A cursor at [position], with its preferred column at the key's start. */
    public fun cursorAt(position: KeyPosition): KeyboardCursor {
        val row = position.row.coerceIn(0, layout.rowCount - 1)
        val index = position.index.coerceIn(0, layout.rows[row].lastIndex)
        return KeyboardCursor(row, index, layout.startColumn(row, index))
    }

    /** [cursor] moved inside the layout, for example after the layout changed. */
    public fun clamp(cursor: KeyboardCursor): KeyboardCursor {
        val row = cursor.row.coerceIn(0, layout.rowCount - 1)
        val index = cursor.index.coerceIn(0, layout.rows[row].lastIndex)
        val preferred = cursor.preferredColumn.coerceIn(0, layout.columnCount - 1)
        return if (row == cursor.row && index == cursor.index && preferred == cursor.preferredColumn) {
            cursor
        } else {
            KeyboardCursor(row, index, preferred)
        }
    }

    /** Where focus goes from [cursor] when [direction] is pressed. */
    public fun move(cursor: KeyboardCursor, direction: Direction): KeyboardMove {
        val from = clamp(cursor)
        val keys = layout.rows[from.row]
        return when (direction) {
            Direction.Left -> when {
                from.index > 0 -> moved(from.row, from.index - 1)
                wrapHorizontally && keys.size > 1 -> moved(from.row, keys.lastIndex)
                else -> KeyboardMove.Edge(direction)
            }
            Direction.Right -> when {
                from.index < keys.lastIndex -> moved(from.row, from.index + 1)
                wrapHorizontally && keys.size > 1 -> moved(from.row, 0)
                else -> KeyboardMove.Edge(direction)
            }
            Direction.Up -> if (from.row == 0) {
                KeyboardMove.Edge(direction)
            } else {
                vertical(from, from.row - 1)
            }
            Direction.Down -> if (from.row == layout.rowCount - 1) {
                KeyboardMove.Edge(direction)
            } else {
                vertical(from, from.row + 1)
            }
        }
    }

    private fun moved(row: Int, index: Int): KeyboardMove =
        KeyboardMove.Moved(KeyboardCursor(row, index, layout.startColumn(row, index)))

    private fun vertical(from: KeyboardCursor, row: Int): KeyboardMove {
        val index = layout.indexAtColumn(row, from.preferredColumn)
        return KeyboardMove.Moved(KeyboardCursor(row, index, from.preferredColumn))
    }
}
