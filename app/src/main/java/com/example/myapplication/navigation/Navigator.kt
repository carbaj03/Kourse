package com.example.myapplication.navigation

import android.util.Log
import com.example.myapplication.with

fun interface Navigator {
    fun Screen.navigate(options: Options)

    data class Options(
        val addToStack: Boolean,
        val clearAll: Boolean,
        val unique: Boolean,
        val key: String,
    )
}

context(Navigator)
fun Screen.navigate(
    addToStack: Boolean = true,
    clearAll: Boolean = false,
    unique: Boolean = true,
    key: String = javaClass.name,
): Unit =
    navigate(Navigator.Options(addToStack, clearAll, unique, key))

context(Reducer, Navigator)
inline fun <reified S : Screen> navigate(key: String, f: S.() -> S): Unit =
    when (val screen = state.screen) {
        is S -> f(screen).navigate(key = key)
        else -> Unit
    }

context(Reducer)
fun back() {
    val mergeable = Mergeable<Dashboard> { other ->
        when (other.currentTab) {
            is Tab.Tab1 -> if (currentTab is Tab.Tab1) this else copy(
                tab1 = other.currentTab,
                tab2 = other.tab2,
                tab3 = other.tab3,
            )
            is Tab.Tab2 -> if (currentTab is Tab.Tab2) this else copy(
                tab1 = other.tab1,
                tab2 = other.currentTab,
                tab3 = other.tab3,
            )
            is Tab.Tab3 -> if (currentTab is Tab.Tab3) this else copy(
                tab1 = other.tab1,
                tab2 = other.tab2,
                tab3 = other.currentTab,
            )
        }
    }
    val mergeable2 = Mergeable<Screen1Detail> { other ->
        copy()
    }

    if (state.stack.screens.isEmpty()) return state.finish()
    state.stack.screens
        .minus(state.stack.screens.keys.last())
        .let { newStack ->
            newStack[newStack.keys.lastOrNull()]
                ?.let { screen ->
                    reducer {
                        copy(
                            stack = BackStack(newStack),
                            screen = with(mergeable, mergeable2) {
                                updateStakeWith<Screen1Detail>() ?: updateStakeWith<Dashboard>() ?: screen
                            }
                        )
                    }
                }
                ?: state.finish()
        }
    state.stack.screens.forEach {
        Log.e("stack", it.toString())
    }
}
