package com.example.myapplication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface Store : Navigator, Reducer

fun Store(finish: () -> Unit): Store {
    val state: MutableStateFlow<App> = MutableStateFlow(App(screen = Start, finish = finish))

    val reducer = object : Reducer {
        override fun App.reduce() {
            state.value = this
        }

        override val state: StateFlow<App> =
            state
    }

    val navigator = Navigator { option ->
        when (option.addToStack) {
            true -> when (option.clearAll) {
                true -> listOf(this)
                false -> when (option.unique) {
                    true -> state.value.stack.screens
                        .filterNot { it.route == state.value.screen.route }
                        .plus(state.value.screen)
                        .filterNot { it.route == route }
                        .plus(this)
                    false -> state.value.stack.screens
                        .filterNot { it.route == state.value.screen.route }
                        .plus(state.value.screen)
                        .plus(this)
                }
            }
            false -> when (option.clearAll) {
                true -> emptyList()
                false -> state.value.stack.screens
            }
        }.let {
            state.value = state.value.copy(
                screen = this,
                stack = BackStack(it)
            )
        }
    }

    return object : Store, Navigator by navigator, Reducer by reducer {}
}