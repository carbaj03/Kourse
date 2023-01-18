package com.example.myapplication

import io.ktor.util.reflect.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigatorShould {
    @Test
    fun `make the all flow`() = runTest {

        var isFinish = false
        val store = Store(finish = { isFinish = true})

        with(store) {
            state.assertScreen<Start> {
                Splash().navigate(false)
            }
            state.assertScreen<Splash> {
                screen.next()
            }
            state.assertScreen<Home> {
                screen.login()
            }
            state.assertScreen<Login> {
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
                    tab.setCounter(21)
                    Assert.assertEquals(21, tab.counter)
                }
                screen.onTabSelected(screen.tab3)
                assert<Tab.Three> {
                    tab.screen.assert<Tab3Screen1> {
                        next()
                    }
                    tab.screen.assert<Tab3Screen2> {
                        back()
                    }
                    tab.screen.assert<Tab3Screen1>()
                }
                back()
                assert<Tab.Two>{
                    Assert.assertEquals(21, tab.counter)
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