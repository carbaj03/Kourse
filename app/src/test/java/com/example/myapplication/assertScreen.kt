package com.example.myapplication

import com.example.myapplication.navigation.App
import com.example.myapplication.navigation.Screen
import io.ktor.util.reflect.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

suspend inline fun <reified A : Screen> StateFlow<App>.assertScreen(
    crossinline f: suspend ScreenContext<A>.() -> Unit
) {
    object : ScreenContext<A> {
        override val screen: A
            get() = value.screen.let {
                assert(it.instanceOf(A::class)) { "${it::class} is not ${A::class}" }
                return it as A
            }

        override val screenFlow: Flow<A>
            get() = map {
                it.screen.let {
                    assert(it.instanceOf(A::class)) { "${it::class} is not ${A::class}" }
                    it as A
                }
            }
    }.let { f(it) }
}

interface ScreenContext<A : Screen> {
    val screen: A
    val screenFlow: Flow<A>
}