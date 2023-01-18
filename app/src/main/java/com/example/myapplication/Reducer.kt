package com.example.myapplication

import kotlinx.coroutines.flow.StateFlow

interface Reducer {
    fun App.reduce()
    val state: StateFlow<App>
}

context(Reducer)
@JvmName("reducerState")
inline fun reducer(f: App.() -> App): Unit =
    f(state.value).reduce()

context(Reducer)
inline fun <reified S : Screen> reducer(f: S.() -> S): Unit =
    when (val screen = state.value.screen) {
        is S -> state.value.copy(screen = f(screen)).reduce()
        else -> Unit
    }