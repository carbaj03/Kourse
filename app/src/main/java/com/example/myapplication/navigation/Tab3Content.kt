package com.example.myapplication.navigation

import kotlinx.coroutines.launch

sealed interface Tab3Content{
    companion object{
        fun empty() =
            Tab3Screen1({})
    }
}

data class Tab3Screen1(
    val next: () -> Unit
) : Tab3Content

data class Tab3Screen2(
    val next: () -> Unit,
    val back: () -> Unit,
    val load: () -> Unit,
    override val stop: () -> Unit,
) : Tab3Content , Stoppable

context(Reducer, Navigator, SideEffect)
fun Tab3Screen1(): Tab3Screen1 =
    Tab3Screen1(
        next = {
            navigate<Dashboard> {
                copy(currentTab = currentTab<Tab.Three> { copy(screen = withScope("Tab3Screen2") { with(Counter()) { Tab3Screen2() } }) })
            }
        }
    )

context(Reducer, SideEffect, Counter)
fun Tab3Screen2(): Tab3Screen2 =
    Tab3Screen2(
        back = { back() },
        next = {},
        load = {
            launch {
                count.collect {
                    reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set(route, it) }) }
                }
            }
        },
        stop = {
            cancel()
        }
    )