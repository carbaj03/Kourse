package com.example.myapplication.navigation

interface Reducer {
    fun App.reduce()
    val state: App
}

//context(Reducer)
//inline fun <reified S : Screen> screenFromStack(): S? =
//    state.stack.screens.filterIsInstance<S>().firstOrNull()

context(Reducer)
@JvmName("reducerState")
inline fun reducer(f: App.() -> App): Unit =
    f(state).reduce()

context(Reducer)
inline fun <reified S : Screen> reducer(f: S.() -> S): Unit =
    when (val screen = state.screen) {
        is S -> state.copy(screen = f(screen)).reduce()
        else -> Unit
    }