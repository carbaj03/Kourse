package com.example.myapplication

import com.example.myapplication.navigation.Dashboard
import com.example.myapplication.navigation.Screen
import com.example.myapplication.navigation.Tab
import io.ktor.util.reflect.*

context(ScreenContext<Dashboard>)
inline fun <reified S : Tab> assert(
    crossinline f: TabContext<S>.() -> Unit = {}
) {
    object : TabContext<S> {
        override val tab: S
            get() {
                assert(screen.currentTab.instanceOf(S::class)) { "${screen.currentTab::class} is not ${S::class}" }
                return screen.currentTab as S
            }
    }.let { f(it) }
}


inline fun <reified S : Screen> Screen.assert(f: S.() -> Unit) {
    assert(this.instanceOf(S::class)) { "${this::class} is not ${S::class}" }
    f(this as S)
}

interface TabContext<A : Tab> {
    val tab: A
}