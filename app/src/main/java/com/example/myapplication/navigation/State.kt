package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import arrow.optics.optics
import com.example.myapplication.navigation.Tab.*
import com.example.myapplication.with
import io.ktor.util.reflect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


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
        delay(1000)
        return listOf("carbajo").filter { it.contains(hint) }
    }

    context(Raise<Error>)
    suspend fun getResults(search: String): List<String> {
        delay(1000)
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