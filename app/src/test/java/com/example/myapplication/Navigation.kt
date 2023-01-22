package com.example.myapplication

import com.example.myapplication.navigation.*
import com.example.myapplication.navigation.Splash
import com.example.myapplication.navigation.Store
import io.ktor.util.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigatorShould {

    @Test
    fun `make the all flow`(): TestResult = runTest {

        var isFinish = false
        val repo = SearchRepository()
        val store = Store(
            start = { with(repo) { Start() } },
            finish = { isFinish = true }
        )

        with(store) {
            state.assertScreen<Splash> {
                screen.next()
                advanceTimeBy(1021)
            }
            state.assertScreen<Home> {
                screen.login()
            }
            state.assertScreen<Login> {
                screen.onChange("name")
                screen.next()
            }
            state.assertScreen<Password> {
                screen.next()
            }
            assertStackJustContains<Dashboard>()
            state.assertScreen<Dashboard> {
                assert<Tab.One> {
                    Assert.assertEquals(0, tab.counter)
                    tab.setCounter(10)
                    Assert.assertEquals(10, tab.counter)
                }
                screen.onTabSelected(screen.tab2)
                assert<Tab.Two> {
                    tab.changeSearch("ca")
                    advanceUntilIdle()
                    Assert.assertEquals("ca", tab.toSearch)
                    tab.selectSuggestion(tab.suggestions.first())
                    advanceUntilIdle()
                    Assert.assertEquals("carbajo", tab.toSearch)
                    tab.search()
                    advanceUntilIdle()
                    Assert.assertEquals(listOf("Carbajo Achievements", "Carbajo the great developer"), tab.results)
                }
                screen.onTabSelected(screen.tab3)
                assert<Tab.Three> {
                    tab.screen.assert<Tab3Screen1> {
                        next()
                    }
                    tab.screen.assert<Tab3Screen2> {
                        launch { back() }
                        advanceUntilIdle()
                    }
                    tab.screen.assert<Tab3Screen1>()
                }
                back()
                assert<Tab.Two> {
                    Assert.assertEquals("carbajo", tab.toSearch)
                }
                back()
                assert<Tab.One> {
                    Assert.assertEquals(10, tab.counter)
                }
                back()
                assert(isFinish)
            }
        }
    }
}


inline fun <reified A : Tab3Content> Tab3Content.assert(f: A.() -> Unit = {}) {
    assert(this.instanceOf(A::class)) { "${this::class} is not ${A::class}" }
    f(this as A)
}

context(Store)
inline fun <reified A> assertStackJustContains() {
    Assert.assertEquals(1, state.value.stack.screens.size)
    assert(state.value.stack.screens.first().instanceOf(A::class)) { "${state.value.stack.screens.first()::class} is not ${A::class}" }
}