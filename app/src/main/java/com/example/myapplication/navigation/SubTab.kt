package com.example.myapplication.navigation

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface SubTab {
    data class One(
        val counter: Int,
        val setCounter: (Int) -> Unit,
        override val stop: () -> Unit,
    ) : SubTab, Stoppable{
        companion object{
            fun empty() =
                One(0, {}, {})
        }
    }

    data class Two(
        val counter: Int,
        val setCounter: (Int) -> Unit,
    ) : SubTab{
        companion object{
            fun empty() =
                Two(0, {})
        }
    }

    data class Three(
        val counter: Int,
        val setCounter: (Int) -> Unit,
    ) : SubTab{
        companion object{
            fun empty() =
                Three(0, {})
        }
    }
}


context(Reducer, SideEffect)
fun SubTabOne(): SubTab.One =
    SubTab.One(
        counter = 0,
        setCounter = {
            launch {
                reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set("Dash", it) }) }
                delay(2000)
                reducer<Dashboard, Tab.Four, SubTab.One> {
                    copy(counter = it)
                }
            }
        },
        stop = {
            cancel()
        }
    )

context(Reducer)
fun SubTabTwo(): SubTab.Two =
    SubTab.Two(
        counter = 0,
        setCounter = { reducer<Dashboard, Tab.Four, SubTab.Two> { copy(counter = it) } }
    )

context(Reducer)
fun SubTabThree() = SubTab.Three(
    counter = 0,
    setCounter = { reducer<Dashboard, Tab.Four, SubTab.Three> { copy(counter = it) } }
)

inline operator fun <reified A : SubTab> SubTab.invoke(f: A.() -> A): SubTab =
    when (this) {
        is A -> f(this)
        else -> this.also { Log.e("Missed", this.toString()) }
    }