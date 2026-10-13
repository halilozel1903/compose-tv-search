package io.github.halilozel1903.tvsearch.core

import java.text.Normalizer
import java.util.Locale

/** Text helpers for matching queries: cleaning, case and accent folding, and splitting into words. */
public object QueryText {
    private val whitespace = Regex("\\s+")
    private val combiningMarks = Regex("\\p{Mn}+")
    private val separators = Regex("[^\\p{L}\\p{N}]+")

    /** [query] trimmed, with runs of whitespace collapsed to one space. */
    public fun clean(query: String): String = query.trim().replace(whitespace, " ")

    /**
     * [text] in lower case without accents, for comparing: `"Café Ünal"` becomes `"cafe unal"`.
     * The Turkish dotless `ı` folds to `i` as well.
     */
    public fun fold(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return decomposed.replace(combiningMarks, "").lowercase(Locale.ROOT).replace('ı', 'i')
    }

    /** The folded words of [text], split at anything that is not a letter or a digit. */
    public fun words(text: String): List<String> = fold(text).split(separators).filter { it.isNotEmpty() }

    /** `true` when [a] and [b] are the same search, ignoring case, accents and extra spaces. */
    public fun sameSearch(a: String, b: String): Boolean = fold(clean(a)) == fold(clean(b))
}
