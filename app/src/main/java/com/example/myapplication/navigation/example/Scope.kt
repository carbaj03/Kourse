package com.example.myapplication.navigation.scope

import com.example.myapplication.with
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow


interface Screen

interface Reducer {
    val state: MutableStateFlow<Screen>
}


data class SearchScreen(
    val title: String,
    val query: String,
    val onQueryChanged: () -> Unit,
    val onSearch: () -> Unit,
    val results: List<String>,
    val onResultSelected: () -> Unit
) : Screen


data class DetailScreen(
    val title: String,
    val info: String,
    val load: () -> Unit,
) : Screen

context(CoroutineScope, Reducer)
fun SearchScreen(): SearchScreen =
    SearchScreen(
        title = "",
        query = "",
        onSearch = {
            launch {
                delay(1000)
                state.value = DetailScreen()
            }
        },
        results = listOf(),
        onResultSelected = {},
        onQueryChanged = {}
    )

context(CoroutineScope, Reducer)
fun DetailScreen(): DetailScreen =
    DetailScreen(
        title = "",
        info = "",
        load = {
            launch {
                delay(1000)
                state.value = (state.value as DetailScreen).copy(title = "title", info = "info")
            }
        }
    )

suspend fun main() {
    val scope = CoroutineScope(Job())

    val reducer = object : Reducer {
        val screen: MutableStateFlow<Screen> = MutableStateFlow(with(scope) { SearchScreen() })

        override val state: MutableStateFlow<Screen> = screen
    }

    with(reducer, scope) {
        (screen.value as SearchScreen).onSearch()
        delay(1020)
        (screen.value as DetailScreen).load()
        delay(1020)
        println(state.value.toString())
    }

    awaitCancellation()
}