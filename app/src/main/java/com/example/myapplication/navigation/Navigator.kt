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
        is S -> f(screen).navigate()
        else -> Unit
    }

context(Reducer)
fun back() {
    val dashboardMergeable = Mergeable<Dashboard> { other ->
        when (other.currentTab) {
            is Tab.One -> if (currentTab is Tab.One) this else copy(
                tab1 = other.currentTab,
                tab2 = other.tab2,
                tab3 = other.tab3,
                tab4 = other.tab4,
            )
            is Tab.Two -> if (currentTab is Tab.Two) this else copy(
                tab1 = other.tab1,
                tab2 = other.currentTab,
                tab3 = other.tab3,
                tab4 = other.tab4,
            )
            is Tab.Three -> if (currentTab is Tab.Three) this else copy(
                tab1 = other.tab1,
                tab2 = other.tab2,
                tab3 = other.currentTab,
                tab4 = other.tab4,
            )
            is Tab.Four -> if (currentTab is Tab.Four) this else copy(
                tab1 = other.tab1,
                tab2 = other.tab2,
                tab3 = other.tab3,
                tab4 = other.currentTab,
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