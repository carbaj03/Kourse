package com.example.myapplication.navigation

import com.example.myapplication.with
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


interface State

data class App(
    val screen: Screen,
    val stack: BackStack = BackStack(emptyList()),
    val finish: () -> Unit,
    val counter: Int = 0,
) : State


@JvmInline
value class BackStack(
    val screens: List<Screen>,
)




class SearchRepository {
    context(Raise<Error>)
    suspend fun getSuggestions(hint: String): List<String> {
        delay(2000)
        return listOf("carbajo").filter { it.contains(hint) }
    }

    context(Raise<Error>)
    suspend fun getResults(search: String): List<String> {
        delay(2000)
        return listOf("Carbajo Achievements", "Carbajo the great developer").filter { it.contains(search, true) }
    }
}


fun main() {
    val scope = CoroutineScope(Job())
    val repo = SearchRepository()

    val store = with(scope, repo) {
        Store(start = { Start() }, finish = {})
    }

    with(store) {
        state<Splash> {
            next()
        }
        state<Home> {
            login()
        }
        state<Login> {
            next()
        }
        state<Password> {
            back()
        }
    }
}