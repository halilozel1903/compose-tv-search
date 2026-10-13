package io.github.halilozel1903.tvsearch.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QueryTextTest {

    @Test
    fun cleansWhitespace() {
        assertEquals("paper comets", QueryText.clean("  paper \t  comets  "))
    }

    @Test
    fun foldsCaseAndAccents() {
        assertEquals("cafe unal", QueryText.fold("Café Ünal"))
        assertEquals("istanbul isik", QueryText.fold("İstanbul Işık"))
    }

    @Test
    fun splitsIntoWords() {
        assertEquals(listOf("the", "glass", "harbor", "2"), QueryText.words("The Glass-Harbor: 2"))
    }

    @Test
    fun sameSearch() {
        assertTrue(QueryText.sameSearch(" Lighthouse ", "lighthouse"))
        assertFalse(QueryText.sameSearch("light", "lighthouse"))
    }

    @Test
    fun configTurnsShortQueriesIntoEmptyOnes() {
        val config = SearchConfig(minQueryLength = 2)
        assertEquals("", config.effectiveQuery(" a "))
        assertEquals("ab", config.effectiveQuery(" ab "))
    }
}
