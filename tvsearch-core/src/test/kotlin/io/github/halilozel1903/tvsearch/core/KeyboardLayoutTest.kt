package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KeyboardLayoutTest {

    @Test
    fun alphanumericHasAllLettersDigitsAndActions() {
        val layout = KeyboardLayout.Alphanumeric
        assertEquals(7, layout.rowCount)
        assertEquals(6, layout.columnCount)
        val chars = layout.rows.flatten().filterIsInstance<KeyboardKey.Character>().map { it.char }.toSet()
        assertEquals(('a'..'z').toSet() + ('0'..'9').toSet(), chars)
        assertEquals(KeyPosition(6, 0), layout.positionOf<KeyboardKey.Space>())
        assertEquals(KeyPosition(6, 1), layout.positionOf<KeyboardKey.Backspace>())
        assertEquals(KeyPosition(6, 2), layout.positionOf<KeyboardKey.Clear>())
    }

    @Test
    fun everyBuiltInLayoutHasRowsOfEqualWidth() {
        for (layout in listOf(KeyboardLayout.Alphanumeric, KeyboardLayout.Alphabetic, KeyboardLayout.Qwerty)) {
            for (row in layout.rows) assertEquals(layout.columnCount, row.sumOf { it.span }, "$layout")
        }
    }

    @Test
    fun columnsAccountForWideKeys() {
        val layout = KeyboardLayout.Alphanumeric
        assertEquals(0, layout.startColumn(6, 0))
        assertEquals(2, layout.startColumn(6, 1))
        assertEquals(4, layout.startColumn(6, 2))
        assertEquals(0, layout.indexAtColumn(6, 1))
        assertEquals(1, layout.indexAtColumn(6, 3))
        assertEquals(2, layout.indexAtColumn(6, 5))
    }

    @Test
    fun indexAtColumnPastAShortRowIsTheLastKey() {
        val layout = KeyboardLayout.of("abcd", "ef")
        assertEquals(1, layout.indexAtColumn(1, 3))
    }

    @Test
    fun findsCharactersIgnoringCase() {
        val layout = KeyboardLayout.Alphanumeric
        assertEquals(KeyPosition(3, 1), layout.positionOf('t'))
        assertEquals(KeyPosition(3, 1), layout.positionOf('T'))
        assertEquals(KeyPosition(4, 2), layout.positionOf('1'))
        assertNull(layout.positionOf('#'))
    }

    @Test
    fun ofAddsTheActionRow() {
        val layout = KeyboardLayout.of("ab", "cd", actions = listOf(KeyboardKey.Backspace(2)))
        assertEquals(3, layout.rowCount)
        assertEquals(KeyboardKey.Backspace(2), layout.key(2, 0))
        assertEquals(KeyboardKey.Character('c'), layout.key(KeyPosition(1, 0)))
    }

    @Test
    fun rejectsEmptyLayoutsAndKeys() {
        assertFailsWith<IllegalArgumentException> { KeyboardLayout(emptyList()) }
        assertFailsWith<IllegalArgumentException> { KeyboardLayout(listOf(emptyList())) }
        assertFailsWith<IllegalArgumentException> { KeyboardKey.Space(0) }
        assertFailsWith<IllegalArgumentException> { KeyboardKey.Character('a', span = 0) }
    }

    @Test
    fun layoutsCompareByKeys() {
        assertEquals(KeyboardLayout.of("ab"), KeyboardLayout(listOf(listOf(KeyboardKey.Character('a'), KeyboardKey.Character('b')))))
    }
}
