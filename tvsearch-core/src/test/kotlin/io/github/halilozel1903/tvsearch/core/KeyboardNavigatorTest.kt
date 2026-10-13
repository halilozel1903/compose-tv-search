package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyboardNavigatorTest {
    private val layout = KeyboardLayout.Alphanumeric
    private val navigator = KeyboardNavigator(layout)

    private fun KeyboardMove.cursor(): KeyboardCursor = (this as KeyboardMove.Moved).cursor

    @Test
    fun leftAndRightMoveInsideARow() {
        val start = navigator.cursorAt(KeyPosition(0, 2))
        assertEquals(KeyboardCursor(0, 3, 3), navigator.move(start, Direction.Right).cursor())
        assertEquals(KeyboardCursor(0, 1, 1), navigator.move(start, Direction.Left).cursor())
    }

    @Test
    fun rowEndsAreEdges() {
        assertEquals(KeyboardMove.Edge(Direction.Left), navigator.move(navigator.cursorAt(KeyPosition(2, 0)), Direction.Left))
        assertEquals(KeyboardMove.Edge(Direction.Right), navigator.move(navigator.cursorAt(KeyPosition(2, 5)), Direction.Right))
        assertEquals(KeyboardMove.Edge(Direction.Up), navigator.move(navigator.cursorAt(KeyPosition(0, 3)), Direction.Up))
        assertEquals(KeyboardMove.Edge(Direction.Down), navigator.move(navigator.cursorAt(KeyPosition(6, 1)), Direction.Down))
    }

    @Test
    fun wrapsWhenAsked() {
        val wrapping = KeyboardNavigator(layout, wrapHorizontally = true)
        assertEquals(KeyboardCursor(1, 5, 5), wrapping.move(wrapping.cursorAt(KeyPosition(1, 0)), Direction.Left).cursor())
        assertEquals(KeyboardCursor(1, 0, 0), wrapping.move(wrapping.cursorAt(KeyPosition(1, 5)), Direction.Right).cursor())
        assertEquals(KeyboardMove.Edge(Direction.Up), wrapping.move(wrapping.cursorAt(KeyPosition(0, 0)), Direction.Up))
    }

    @Test
    fun downIntoAWideKeyAndBackKeepsTheColumn() {
        // 'x' is row 3, column 5. Down twice reaches '0', down again the wide Clear key.
        var cursor = navigator.cursorAt(layout.positionOf('x')!!)
        cursor = navigator.move(cursor, Direction.Down).cursor()
        cursor = navigator.move(cursor, Direction.Down).cursor()
        assertEquals(KeyboardKey.Character('0'), layout.key(cursor.position))
        cursor = navigator.move(cursor, Direction.Down).cursor()
        assertEquals(KeyboardCursor(6, 2, 5), cursor)
        assertEquals(KeyboardKey.Clear(2), layout.key(cursor.position))
        cursor = navigator.move(cursor, Direction.Up).cursor()
        assertEquals(KeyboardKey.Character('0'), layout.key(cursor.position))
    }

    @Test
    fun startingOnTheRightHalfOfAWideKeyStillReturnsThere() {
        // '8' sits in column 3, the right half of Backspace (columns 2 and 3).
        var cursor = navigator.cursorAt(layout.positionOf('8')!!)
        cursor = navigator.move(cursor, Direction.Down).cursor()
        assertEquals(KeyboardKey.Backspace(2), layout.key(cursor.position))
        cursor = navigator.move(cursor, Direction.Up).cursor()
        assertEquals(KeyboardKey.Character('8'), layout.key(cursor.position))
    }

    @Test
    fun sidewaysOnAWideKeyResetsThePreferredColumn() {
        val space = navigator.cursorAt(layout.positionOf<KeyboardKey.Space>()!!)
        val backspace = navigator.move(space, Direction.Right).cursor()
        assertEquals(KeyboardCursor(6, 1, 2), backspace)
        assertEquals(KeyboardKey.Character('7'), layout.key(navigator.move(backspace, Direction.Up).cursor().position))
    }

    @Test
    fun clampsCursorsFromAnotherLayout() {
        val small = KeyboardNavigator(KeyboardLayout.of("ab", "c"))
        assertEquals(KeyboardCursor(1, 0, 1), small.clamp(KeyboardCursor(5, 7, 9)))
        val inside = KeyboardCursor(0, 1, 1)
        assertEquals(inside, small.clamp(inside))
    }

    @Test
    fun shortRowsFallBackToTheirLastKey() {
        val ragged = KeyboardNavigator(KeyboardLayout.of("abcdef", "gh"))
        val down = ragged.move(ragged.cursorAt(KeyPosition(0, 4)), Direction.Down).cursor()
        assertEquals(KeyboardCursor(1, 1, 4), down)
        assertEquals(KeyboardCursor(0, 4, 4), ragged.move(down, Direction.Up).cursor())
    }
}
