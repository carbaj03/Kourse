package com.example.myapplication

import com.example.myapplication.navigation.*
import com.example.myapplication.navigation.Navigator
import com.example.myapplication.navigation.Reducer
import com.example.myapplication.navigation.Splash
import com.example.myapplication.navigation.Start
import io.ktor.util.reflect.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigatorShould {
    @Test
    fun `make the all flow`() = runTest {
        val state = MutableStateFlow(App(Start))
        val navigator = Navigator {
            state.value = when (it) {
                true -> state.value.copy(screen = this, stack = BackStack(state.value.stack.screens.plus(state.value.screen)))
                false -> state.value.copy(screen = this)
            }
        }
        val provider = object : Reducer {
            override fun App.reduce() {
                state.value = this
            }

            override val state: App
                get() = state.value
        }

        with(navigator, provider) {
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
                screen.onChange("nameChanged")
            }
            state.assertScreen<Login> {
                screen.next()
            }
            state.assertScreen<Password> {
                screen.back()
            }
            state.assertScreen<Login> {
                Assert.assertEquals("nameChanged", screen.name)
                screen.next()
            }
            state.assertScreen<Password> {
                screen.next()
            }
            state.assertScreen<Dashboard> {
                screen.currentTab.assert<Tab.Tab1>{
                    Assert.assertEquals(0, tab.counter)
                }
                screen.currentTab.assert<Tab.Tab1>{
                    tab.setCounter(21)
                }
                screen.currentTab.assert<Tab.Tab1>{
                    Assert.assertEquals(21, tab.counter)
                }
                screen.onTabSelected(Tab.Tab2.id)
                screen.currentTab.assert<Tab.Tab2>{
                    Assert.assertEquals(21, tab.counter)
                }

                screen.currentTab.assert<Tab.Tab2>{
                    Assert.assertEquals(21, tab.counter)
                }
            }
        }
    }
}