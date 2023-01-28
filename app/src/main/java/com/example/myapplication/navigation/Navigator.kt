package com.example.myapplication.navigation

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
        is S -> f(screen).navigate(addToStack = true)
        else -> Unit
    }


fun Screen.stop(next: Screen) {
    when (this) {
        is Dashboard -> {
            when (val tab = currentTab) {
                is Tab.Four -> when (val sub = tab.currentSubTab) {
                    is SubTab.One -> {
                        tab.stop()
                        sub.stop()
                    }
                    is SubTab.Three -> tab.stop()
                    is SubTab.Two -> tab.stop()
                }
                is Tab.One -> tab.stop()
                is Tab.Three -> when (tab.screen) {
                    is Tab3Screen1 -> tab.stop()
                    is Tab3Screen2 -> tab.screen.stop()
                }
                is Tab.Two -> tab.stop()
            }
            if(next !is Dashboard) stop()
        }
        is Home -> {}
        is Login -> stop()
        is Password.Login -> {}
        is Password.Signup -> {}
        is SignUp -> {}
        is Splash -> {}
        is Detail -> stop()
        is Start -> {}

    }
}

context(Reducer)
fun back() {
    state.value.screen.stop(state.value.stack.screens.last())

    val dashboardMergeable = Mergeable<Dashboard> { stacked ->
        copy()
//        when (stacked.currentTab) {
//            is Tab.One -> copy(
//                tab1 = stacked.currentTab,
//                tab2 = stacked.tab2,
//                tab3 = stacked.tab3,
//                tab4 = stacked.tab4,
//                counters = stacked.counters
//            )
//            is Tab.Two -> copy(
//                tab1 = stacked.tab1,
//                tab2 = stacked.currentTab,
//                tab3 = stacked.tab3,
//                tab4 = stacked.tab4,
//                counters = stacked.counters
//            )
//            is Tab.Three -> copy(
//                tab1 = stacked.tab1,
//                tab2 = stacked.tab2,
//                tab3 = stacked.currentTab,
//                tab4 = stacked.tab4,
//                counters = stacked.counters
//            )
//            is Tab.Four -> copy(
//                tab1 = stacked.tab1,
//                tab2 = stacked.tab2,
//                tab3 = stacked.tab3,
//                tab4 = stacked.currentTab,
//                counters = stacked.counters
//            )
//        }
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
