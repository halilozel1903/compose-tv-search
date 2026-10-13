package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals

class QueryEditorTest {

    private fun type(vararg keys: KeyboardKey, start: String = "", max: Int = Int.MAX_VALUE): String =
        keys.fold(start) { query, key -> QueryEditor.apply(query, key, max) }

    private fun chars(text: String): Array<KeyboardKey> = text.map { KeyboardKey.Character(it) }.toTypedArray()

    @Test
    fun typesCharacters() {
        assertEquals("light", type(*chars("light")))
    }

    @Test
    fun spaceIsNeverLeadingOrDoubled() {
        assertEquals("", type(KeyboardKey.Space()))
        assertEquals("paper ", type(KeyboardKey.Space(), start = "paper"))
        assertEquals("paper ", type(KeyboardKey.Space(), KeyboardKey.Space(), start = "paper"))
    }

    @Test
    fun backspaceAndClear() {
        assertEquals("ligh", type(KeyboardKey.Backspace(), start = "light"))
        assertEquals("", type(KeyboardKey.Backspace(), start = ""))
        assertEquals("", type(KeyboardKey.Clear(), start = "light"))
    }

    @Test
    fun backspaceRemovesAWholeEmoji() {
        assertEquals("tv", QueryEditor.backspace("tv📺"))
    }

    @Test
    fun stopsAtTheMaximumLength() {
        assertEquals("abc", type(*chars("abcdef"), max = 3))
        assertEquals("abc", type(KeyboardKey.Space(), start = "abc", max = 3))
        assertEquals("ab", type(KeyboardKey.Backspace(), start = "abc", max = 3))
    }
}
