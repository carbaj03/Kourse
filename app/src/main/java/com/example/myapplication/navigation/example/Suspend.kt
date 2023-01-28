package com.example.myapplication.navigation.suspend

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow


interface Screen

interface Reducer {
    val state: MutableStateFlow<Screen>
}

data class SearchScreen(
    val title: String,
    val query: String,
    val onQueryChanged: suspend () -> Unit,
    val onSearch: suspend () -> Unit,
    val results: List<String>,
    val onResultSelected: () -> Unit
) : Screen

data class DetailScreen(
    val title: String,
    val info: String,
    val load: suspend () -> Unit,
) : Screen

context(Reducer)
fun SearchScreen(): SearchScreen = SearchScreen(
    title = "",
    query = "",
    onSearch = {
        delay(1000)
        state.value = DetailScreen()
    },
    results = listOf(),
    onResultSelected = {},
    onQueryChanged = {}
)

context(Reducer)
fun DetailScreen(): DetailScreen =
    DetailScreen(
        title = "",
        info = "",
        load = {
            delay(1000)
            state.value = (state.value as DetailScreen).copy(title = "title", info = "info")
        }
    )

suspend fun main() {
    val scope = CoroutineScope(Job())

    val reducer = object : Reducer {
        val screen: MutableStateFlow<Screen> = MutableStateFlow(with(scope) { SearchScreen() })

        override val state: MutableStateFlow<Screen> = screen
    }

    with(reducer) {
        scope.launch {
            (screen.value as SearchScreen).onSearch()
            (screen.value as DetailScreen).load()
            println(state.value.toString())
        }
    }

    awaitCancellation()
}
