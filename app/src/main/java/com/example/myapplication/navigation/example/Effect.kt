package com.example.myapplication.navigation.effect

import com.example.myapplication.with
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow


interface Effect : CoroutineScope {
    fun close() = coroutineContext.cancelChildren()
    val active get() = isActive
    fun a() {
        coroutineContext.job.invokeOnCompletion { println("Complete") }
    }
}

interface Reducer {
    val state: MutableStateFlow<App>
}

interface Screen

data class App(
    val current: Screen,
    val stack: List<Screen>
)

data class SearchScreen(
    val title: String,
    val query: String,
    val onQueryChanged: (String) -> Unit,
    val onSearch: () -> Unit,
    val results: List<String>,
    val onResultSelected: () -> Unit
) : Screen

data class DetailScreen(
    val title: String,
    val info: String,
    val load: () -> Unit,
    val back: () -> Unit,
) : Screen


var counter = 0
suspend fun repo(): List<String> {
    delay(2000)
    counter += 1
    return listOf("sadf:${counter}")
}

context(Effect, Reducer)
fun SearchScreen(): SearchScreen =
    SearchScreen(
        title = "",
        query = "",
        onQueryChanged = {

        },
        onSearch = {
            println(active)
            launch {
                repo().let {
                    println(it.toString())
                    state.value = state.value.copy(current = (state.value.current as SearchScreen).copy(results = it))
                }
            }
        },
        results = listOf(),
        onResultSelected = {
            close()
            val scope = object : Effect, CoroutineScope by CoroutineScope(Job()) {}
            state.value = state.value.copy(current = scope.run { DetailScreen() }, stack = state.value.stack.plus(state.value.current))
        },
    )

context(Effect, Reducer)
fun DetailScreen(): DetailScreen =
    DetailScreen(
        title = "",
        info = "",
        load = {
            launch {
                delay(3000)
                state.value = state.value.copy(current = (state.value.current as DetailScreen).copy(title = "title", info = "info"))
            }
        },
        back = {
            close()
            state.value = state.value.copy(current = state.value.stack.last(), stack = listOf())
        }
    )

suspend fun main() {
    val scope = object : Effect, CoroutineScope by CoroutineScope(SupervisorJob()) {}

    val reducer = object : Reducer {
        val screen: MutableStateFlow<App> = MutableStateFlow(with(scope) { App(current = SearchScreen(), stack = emptyList()) })

        override val state: MutableStateFlow<App> = screen
    }

    with(reducer) {
        (screen.value.current as SearchScreen).onSearch()
        (screen.value.current as SearchScreen).onResultSelected()
        (screen.value.current as DetailScreen).load()
        delay(2030)
        (screen.value.current as DetailScreen).back()
        println(state.value.toString())
        (screen.value.current as SearchScreen).onSearch()
        delay(2030)
        println(state.value.toString())
    }

}