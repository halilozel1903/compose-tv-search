package io.github.halilozel1903.tvsearch.core

/** The row and the index of a key inside that row. */
public data class KeyPosition(public val row: Int, public val index: Int)

/**
 * The keys of an on-screen keyboard, row by row.
 *
 * Rows can have different widths and keys can cover several columns ([KeyboardKey.span]).
 * Use one of the built-in layouts ([Alphanumeric], [Qwerty], [Alphabetic]) or build your own with
 * [of] or the constructor.
 */
public class KeyboardLayout(rows: List<List<KeyboardKey>>) {

    /** The keys, top row first. */
    public val rows: List<List<KeyboardKey>> = rows.map { it.toList() }

    init {
        require(this.rows.isNotEmpty()) { "A keyboard needs at least one row" }
        require(this.rows.none { it.isEmpty() }) { "Keyboard rows must not be empty" }
    }

    /** The number of rows. */
    public val rowCount: Int get() = rows.size

    /** The width of the widest row, in grid columns. */
    public val columnCount: Int = this.rows.maxOf { row -> row.sumOf { it.span } }

    /** The key at [row] and [index]. */
    public fun key(row: Int, index: Int): KeyboardKey = rows[row][index]

    /** The key at [position]. */
    public fun key(position: KeyPosition): KeyboardKey = key(position.row, position.index)

    /** The grid column where the key at [row] and [index] starts. */
    public fun startColumn(row: Int, index: Int): Int {
        var column = 0
        for (i in 0 until index) column += rows[row][i].span
        return column
    }

    /** The index of the key in [row] that covers grid [column]; the last key when the row is shorter. */
    public fun indexAtColumn(row: Int, column: Int): Int {
        var start = 0
        rows[row].forEachIndexed { index, key ->
            if (column < start + key.span) return index
            start += key.span
        }
        return rows[row].lastIndex
    }

    /** Where [key] is, or `null` when the layout has no such key. */
    public fun positionOf(key: KeyboardKey): KeyPosition? {
        rows.forEachIndexed { row, keys ->
            val index = keys.indexOf(key)
            if (index >= 0) return KeyPosition(row, index)
        }
        return null
    }

    /** Where the key typing [char] is (ignoring case), or `null`. */
    public fun positionOf(char: Char): KeyPosition? {
        val wanted = char.lowercaseChar()
        rows.forEachIndexed { row, keys ->
            val index = keys.indexOfFirst { it is KeyboardKey.Character && it.char.lowercaseChar() == wanted }
            if (index >= 0) return KeyPosition(row, index)
        }
        return null
    }

    /** Where the first key of type [T] is (for example `positionOf<KeyboardKey.Backspace>()`), or `null`. */
    public inline fun <reified T : KeyboardKey> positionOf(): KeyPosition? {
        rows.forEachIndexed { row, keys ->
            val index = keys.indexOfFirst { it is T }
            if (index >= 0) return KeyPosition(row, index)
        }
        return null
    }

    override fun equals(other: Any?): Boolean = other is KeyboardLayout && other.rows == rows

    override fun hashCode(): Int = rows.hashCode()

    override fun toString(): String = "KeyboardLayout(${rows.size} rows, $columnCount columns)"

    public companion object {
        /**
         * A layout from strings, one per row, where every character becomes a one column key,
         * followed by a last row of [actions].
         */
        public fun of(vararg rows: String, actions: List<KeyboardKey> = emptyList()): KeyboardLayout {
            val keys = rows.map { row -> row.map { KeyboardKey.Character(it) } }
            return KeyboardLayout(if (actions.isEmpty()) keys else keys + listOf(actions))
        }

        /**
         * a to z and 0 to 9 in a 6 x 6 grid, then space, backspace and clear. The compact layout
         * most TV apps use; the default.
         */
        public val Alphanumeric: KeyboardLayout = of(
            "abcdef",
            "ghijkl",
            "mnopqr",
            "stuvwx",
            "yz1234",
            "567890",
            actions = listOf(KeyboardKey.Space(2), KeyboardKey.Backspace(2), KeyboardKey.Clear(2)),
        )

        /** Letters only, seven per row, with an apostrophe and a hyphen. */
        public val Alphabetic: KeyboardLayout = of(
            "abcdefg",
            "hijklmn",
            "opqrstu",
            "vwxyz'-",
            actions = listOf(KeyboardKey.Space(3), KeyboardKey.Backspace(2), KeyboardKey.Clear(2)),
        )

        /** A ten column QWERTY layout with a digit row. */
        public val Qwerty: KeyboardLayout = of(
            "1234567890",
            "qwertyuiop",
            "asdfghjkl'",
            "zxcvbnm-.&",
            actions = listOf(KeyboardKey.Space(4), KeyboardKey.Backspace(3), KeyboardKey.Clear(3)),
        )
    }
}
