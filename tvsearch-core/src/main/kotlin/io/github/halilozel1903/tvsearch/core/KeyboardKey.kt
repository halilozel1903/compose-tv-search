package io.github.halilozel1903.tvsearch.core

/**
 * One key of the on-screen keyboard.
 *
 * Every key covers [span] columns of the keyboard grid, so action keys can be wider than letters.
 */
public sealed class KeyboardKey {
    /** How many grid columns the key covers, at least 1. */
    public abstract val span: Int

    /** Types [char]. */
    public data class Character(public val char: Char, override val span: Int = 1) : KeyboardKey() {
        init {
            require(span >= 1) { "span must be at least 1, was $span" }
        }
    }

    /** Types a space, never at the start of the query and never twice in a row. */
    public data class Space(override val span: Int = 2) : KeyboardKey() {
        init {
            require(span >= 1) { "span must be at least 1, was $span" }
        }
    }

    /** Deletes the last character. */
    public data class Backspace(override val span: Int = 2) : KeyboardKey() {
        init {
            require(span >= 1) { "span must be at least 1, was $span" }
        }
    }

    /** Clears the whole query. */
    public data class Clear(override val span: Int = 2) : KeyboardKey() {
        init {
            require(span >= 1) { "span must be at least 1, was $span" }
        }
    }
}
