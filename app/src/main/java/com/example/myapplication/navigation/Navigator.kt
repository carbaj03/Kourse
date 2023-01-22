package com.example.myapplication.navigation

import android.util.Log
import com.example.myapplication.with

fun interface Navigator {
    fun Screen.navigate(options: Options)

    data class Options(
        val addToStack: Boolean,
        val clearAll: Boolean,
        val unique: Boolean,
    )
}

context(Navigator)
fun Screen.navigate(
    addToStack: Boolean = true,
    clearAll: Boolean = false,
    unique: Boolean = true,
): Unit =
    navigate(Navigator.Options(addToStack, clearAll, unique))

context(Reducer, Navigator)
inline fun <reified S : Screen> navigate(f: S.() -> S): Unit =
    when (val screen = state.value.screen) {
        is S -> f(screen).navigate()
        else -> Unit
    }

context(Reducer)
fun back() {
    val dashboardMergeable = Mergeable<Dashboard> { stacked ->
        when (stacked.currentTab) {
            is Tab.One -> if (currentTab is Tab.One) this else copy(
                tab1 = stacked.currentTab,
                tab2 = stacked.tab2,
                tab3 = stacked.tab3,
                tab4 = stacked.tab4,
                counter = stacked.counter
            )
            is Tab.Two -> if (currentTab is Tab.Two) this else copy(
                tab1 = stacked.tab1,
                tab2 = stacked.currentTab,
                tab3 = stacked.tab3,
                tab4 = stacked.tab4,
                counter = stacked.counter
            )
            is Tab.Three -> if (currentTab is Tab.Three) this else copy(
                tab1 = stacked.tab1,
                tab2 = stacked.tab2,
                tab3 = stacked.currentTab,
                tab4 = stacked.tab4,
                counter = stacked.counter
            )
            is Tab.Four -> if (currentTab is Tab.Four) this else copy(
                tab1 = stacked.tab1,
                tab2 = stacked.tab2,
                tab3 = stacked.tab3,
                tab4 = stacked.currentTab,
                counter = stacked.counter
            )
        }
    }

    if (state.value.stack.screens.isEmpty())
        return state.value.finish()

    state.value.stack.screens
        .minus(state.value.stack.screens.last())
        .let { newStack ->
            newStack.lastOrNull()?.let { screen ->
                reducer {
                    copy(
                        stack = BackStack(newStack),
                        screen = with(dashboardMergeable) {
                            reduceStack<Dashboard>() ?: screen
                        }
                    )
                }
            } ?: state.value.finish()
        }
}

context(Reducer)
fun screenToMerge(): List<Screen> {
    if (state.value.stack.screens.isEmpty()) return emptyList()
    return state.value.stack.screens
        .getOrNull(state.value.stack.screens.size - 2)
        ?.let { listOf(it, state.value.screen) }
        ?: emptyList()

}

context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> filterMergeable(): List<A>? =
    screenToMerge()
        .filterIsInstance<A>()
        .let { if (it.size <= 1) return null else it }


context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> reduceStack(): A? =
    filterMergeable()?.reduce { a, b -> a + b }