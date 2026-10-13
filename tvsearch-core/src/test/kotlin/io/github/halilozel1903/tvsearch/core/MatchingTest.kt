package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchingTest {

    @Test
    fun scoresExactThenPrefixThenWordsThenSubstrings() {
        val exact = SearchMatcher.score("light", "Light")!!
        val prefix = SearchMatcher.score("light", "Light Years Apart")!!
        val word = SearchMatcher.score("light", "The Last Lighthouse")!!
        val inside = SearchMatcher.score("light", "Moonlight Bakery")!!
        assertTrue(exact > prefix, "$exact > $prefix")
        assertTrue(prefix > word, "$prefix > $word")
        assertTrue(word > inside, "$word > $inside")
    }

    @Test
    fun shorterTextsWinInsideAGroup() {
        assertTrue(SearchMatcher.score("harb", "Harbor")!! > SearchMatcher.score("harb", "Harbor Lights at Dawn")!!)
    }

    @Test
    fun everyQueryWordHasToMatch() {
        assertTrue(SearchMatcher.matches("glass har", "The Glass Harbor"))
        assertNull(SearchMatcher.score("glass moon", "The Glass Harbor"))
        assertNull(SearchMatcher.score("li", "Moonlight"))
        assertNull(SearchMatcher.score("", "Anything"))
        assertNull(SearchMatcher.score("!!", "Anything"))
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertTrue(SearchMatcher.matches("cafe", "Café Stories"))
        assertTrue(SearchMatcher.matches("ISIK", "Işık"))
    }

    @Test
    fun filterSortsByScoreAndKeepsOrderOnTies() {
        val titles = listOf("Moonlight Bakery", "The Last Lighthouse", "Paper Comets", "Light Years Apart", "Lighthouse Keepers")
        assertEquals(
            listOf("Light Years Apart", "Lighthouse Keepers", "The Last Lighthouse", "Moonlight Bakery"),
            SearchMatcher.filter("light", titles) { it },
        )
        assertEquals(listOf("Light Years Apart"), SearchMatcher.filter("light", titles, limit = 1) { it })
    }

    @Test
    fun filterByFieldsUsesTheBestField() {
        data class Title(val name: String, val genre: String)
        val items = listOf(Title("Paper Comets", "Mystery"), Title("Mystery Hill", "Drama"), Title("Quiet Orbit", "Drama"))
        assertEquals(
            listOf("Mystery Hill", "Paper Comets"),
            SearchMatcher.filterByFields("myst", items) { listOf(it.name, it.genre) }.map { it.name },
        )
    }

    @Test
    fun suggestionsPreferRecentSearchesAndSkipDuplicates() {
        val suggestions = Suggestions.rank(
            query = "light",
            candidates = listOf("Lighthouse Keepers", "Light Years Apart", "light years apart", "Moonlight Bakery", "Paper Comets", "light"),
            recent = listOf("lighthouse"),
        )
        assertEquals(listOf("lighthouse", "Light Years Apart", "Lighthouse Keepers", "Moonlight Bakery"), suggestions)
    }

    @Test
    fun suggestionsForAnEmptyQueryAreTheRecentSearches() {
        assertEquals(listOf("a", "b"), Suggestions.rank("  ", listOf("x"), recent = listOf("a", "b", "c"), limit = 2))
        assertEquals(emptyList(), Suggestions.rank("light", listOf("Lighthouse"), limit = 0))
    }

    @Test
    fun resultFocusFollowsTheKeyThenTheIndex() {
        val keys = listOf("a", "b", "c")
        assertEquals(2, ResultFocus.restoreIndex(keys, lastKey = "c", lastIndex = 0))
        assertEquals(1, ResultFocus.restoreIndex(keys, lastKey = "gone", lastIndex = 1))
        assertEquals(2, ResultFocus.restoreIndex(keys, lastKey = null, lastIndex = 9))
        assertEquals(0, ResultFocus.restoreIndex(keys, lastKey = null, lastIndex = -4))
        assertEquals(-1, ResultFocus.restoreIndex(emptyList<String>(), lastKey = "a", lastIndex = 0))
    }

    @Test
    fun voiceResultsPickTheMostConfidentUsableEntry() {
        assertEquals("paper comets", VoiceResults.best(listOf(" paper  comets ", "paper comet")))
        assertEquals("paper comet", VoiceResults.best(listOf("paper comets", "paper comet"), floatArrayOf(0.2f, 0.9f)))
        assertEquals("harbor", VoiceResults.best(listOf("", null, "harbor")))
        assertEquals("a", VoiceResults.best(listOf("a", "b"), floatArrayOf(0.5f)))
        assertNull(VoiceResults.best(listOf(" ", null)))
        assertNull(VoiceResults.best(null))
    }
}
