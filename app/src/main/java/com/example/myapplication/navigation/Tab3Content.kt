package com.example.myapplication.navigation

sealed interface Tab3Content

data class Tab3Screen1(
    val next: () -> Unit
) : Tab3Content

data class Tab3Screen2(
    val next: () -> Unit,
    val back: () -> Unit,
) : Tab3Content

context(Reducer, Navigator)
fun Tab3Screen1(): Tab3Screen1 =
    Tab3Screen1(
        next = {
            navigate<Dashboard> {
                copy(currentTab = currentTab<Tab.Three> { copy(screen = Tab3Screen2()) })
            }
        }
    )

context(Reducer)
fun Tab3Screen2(): Tab3Screen2 =
    Tab3Screen2(
        back = { back() },
        next = {},
    )