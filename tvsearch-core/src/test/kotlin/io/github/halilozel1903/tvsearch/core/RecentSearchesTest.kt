package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RecentSearchesTest {

    @Test
    fun recordsMostRecentFirst() {
        val recent = RecentSearches().record("harbor").record("mystery")
        assertEquals(listOf("mystery", "harbor"), recent.items)
    }

    @Test
    fun duplicatesMoveToTheFrontWithTheirNewSpelling() {
        val recent = RecentSearches(listOf("mystery", "Harbor", "comets")).record("  harbor ")
        assertEquals(listOf("harbor", "mystery", "comets"), recent.items)
    }

    @Test
    fun blankSearchesAreIgnored() {
        val recent = RecentSearches(listOf("a"))
        assertSame(recent, recent.record("   "))
        assertEquals(listOf("a", "b"), RecentSearches(listOf("a", " ", "b")).items)
    }

    @Test
    fun keepsAtMostCapacity() {
        var recent = RecentSearches(capacity = 3)
        for (query in listOf("one", "two", "three", "four")) recent = recent.record(query)
        assertEquals(listOf("four", "three", "two"), recent.items)
        assertEquals(listOf("four"), recent.withCapacity(1).items)
        assertTrue(RecentSearches(capacity = 0).record("x").isEmpty())
    }

    @Test
    fun removeAndClear() {
        val recent = RecentSearches(listOf("mystery", "harbor"))
        assertEquals(listOf("harbor"), recent.remove("MYSTERY").items)
        assertTrue(recent.clear().isEmpty())
        assertEquals(8, recent.clear().capacity)
    }

    @Test
    fun matchingUsesWordPrefixes() {
        val recent = RecentSearches(listOf("paper comets", "harbor lights", "comet tail"))
        assertEquals(listOf("paper comets", "comet tail"), recent.matching("com"))
        assertEquals(listOf("harbor lights"), recent.matching("light har"))
        assertEquals(recent.items, recent.matching(""))
    }
}
