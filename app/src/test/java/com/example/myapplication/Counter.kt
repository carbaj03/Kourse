package com.example.myapplication

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterShould {
    @Test
    fun `run app`() = runTest {
        val repository = object : RandomRepositoryMock {
            override var error: Boolean = false
        }

        val store = Store(
            start = with(repository) { Start() },
            coroutineScope = this
        )

        with(store.navigator, store.reducer, store.sideEffect, store.tracker, repository) {
            store.state.assert<Start> {
                screen.next(store.sideEffect, store.navigator, store.reducer, store.tracker)
                advanceUntilIdle()
            }
            store.state.assert<Splash> {
                screen.next()
                advanceUntilIdle()
            }
            store.state.assert<Main> {
                screen.increment()
                screen.increment()
                Assert.assertEquals("2", screen.display)
                screen.decrement()
                Assert.assertEquals("1", screen.display)
                screen.restart()
                Assert.assertEquals("0", screen.display)
                error = true
                screen.random()
                advanceTimeBy(100)
                Assert.assertEquals(true, screen.loading)
                advanceTimeBy(1030)
                Assert.assertEquals(false, screen.loading)
                Assert.assertEquals("Bad number", screen.display)
                Assert.assertEquals(0, screen.counter)
                error = false
                screen.random()
                advanceTimeBy(100)
                Assert.assertEquals(true, screen.loading)
                advanceTimeBy(1030)
                Assert.assertEquals(false, screen.loading)
                Assert.assertEquals("1", screen.display)
            }
        }
    }
}