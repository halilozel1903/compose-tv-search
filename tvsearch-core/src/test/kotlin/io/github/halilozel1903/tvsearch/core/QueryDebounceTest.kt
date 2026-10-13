package io.github.halilozel1903.tvsearch.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueryDebounceTest {
    private val config = SearchConfig(debounceMillis = 300)

    @Test
    fun emitsOnlyAfterTypingPauses() = runTest {
        val input = MutableStateFlow("")
        val emitted = mutableListOf<String>()
        val job = launch { input.debounceQuery(config).toList(emitted) }
        runCurrent()
        for (text in listOf("l", "li", "lig", "ligh", "light")) {
            input.value = text
            advanceTimeBy(100)
        }
        assertEquals(listOf(""), emitted)
        advanceTimeBy(250)
        assertEquals(listOf("", "light"), emitted)
        job.cancel()
    }

    @Test
    fun clearingIsImmediate() = runTest {
        val input = MutableStateFlow("")
        val emitted = mutableListOf<String>()
        val job = launch { input.debounceQuery(config).toList(emitted) }
        runCurrent()
        input.value = "harbor"
        advanceTimeBy(400)
        input.value = ""
        runCurrent()
        assertEquals(listOf("", "harbor", ""), emitted)
        job.cancel()
    }

    @Test
    fun theSameQueryIsNotSearchedTwice() = runTest {
        val input = MutableStateFlow("")
        val emitted = mutableListOf<String>()
        val job = launch { input.debounceQuery(config).toList(emitted) }
        runCurrent()
        input.value = "paper"
        advanceTimeBy(400)
        input.value = "paper "
        advanceTimeBy(50)
        input.value = "paperx"
        advanceTimeBy(50)
        input.value = "paper"
        advanceUntilIdle()
        assertEquals(listOf("", "paper"), emitted)
        job.cancel()
    }

    @Test
    fun tooShortQueriesCountAsEmpty() = runTest {
        val input = MutableStateFlow("")
        val emitted = mutableListOf<String>()
        val job = launch { input.debounceQuery(SearchConfig(debounceMillis = 300, minQueryLength = 3)).toList(emitted) }
        runCurrent()
        input.value = "ab"
        advanceTimeBy(400)
        input.value = "abc"
        advanceTimeBy(400)
        assertEquals(listOf("", "abc"), emitted)
        job.cancel()
    }

    @Test
    fun clockDebouncerWaitsForThePause() {
        val debouncer = QueryDebouncer(config)
        debouncer.input("li", nowMillis = 0)
        debouncer.input("light", nowMillis = 200)
        assertTrue(debouncer.isPending)
        assertNull(debouncer.poll(400))
        assertEquals("light", debouncer.poll(500))
        assertFalse(debouncer.isPending)
        assertEquals("light", debouncer.current)
        assertNull(debouncer.poll(900))
    }

    @Test
    fun clockDebouncerClearsAndFlushesRightAway() {
        val debouncer = QueryDebouncer(config)
        debouncer.input("harbor", nowMillis = 0)
        assertEquals("harbor", debouncer.flush())
        debouncer.input("", nowMillis = 10)
        assertEquals("", debouncer.poll(10))
        debouncer.input("  ", nowMillis = 20)
        assertFalse(debouncer.isPending)
    }

    @Test
    fun clockDebouncerIgnoresInputThatReturnsToTheCurrentQuery() {
        val debouncer = QueryDebouncer(config)
        debouncer.input("paper", nowMillis = 0)
        debouncer.poll(300)
        debouncer.input("paperx", nowMillis = 400)
        debouncer.input("paper", nowMillis = 450)
        assertFalse(debouncer.isPending)
        assertNull(debouncer.poll(1_000))
    }
}
