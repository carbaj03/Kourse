package com.example.myapplication.empty

import arrow.core.Either
import arrow.optics.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppState(
    val fromScreen: List<NavGraph> = emptyList(),
    val currentScreen: NavGraph? = null,
    val user: String? = null,
)

sealed interface AppAction {
    data class Navigate(val navGraph: NavGraph) : AppAction
}

interface AppThunk {
    fun dispatch(action: AppAction)
    val state: StateFlow<AppState>
}

class AppAndroid : AppThunk {
    val s = MutableStateFlow(AppState())

    override fun dispatch(action: AppAction) {
        when (action) {
            is AppAction.Navigate -> {
                if (action.navGraph is NavGraph.Back) {
                    Either.catch {
                        s.value.fromScreen.dropLast(1).let {
                            s.value = s.value.copy(fromScreen = it, currentScreen = it.last())
                        }
                    }
                } else {
                    s.value = s.value.copy(fromScreen = s.value.fromScreen.plus(NavGraph.books.modify(action.navGraph) { it.copy(new = false) }), currentScreen = action.navGraph)
                }
            }
        }
    }

    override val state: StateFlow<AppState> = s
}