package com.example.myapplication.empty

import arrow.core.Either
import arrow.optics.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppState(
    val fromScreen: List<NavGraph> = emptyList(),
    val currentScreen: NavGraph? = null,
    val userGraph: UserGraph? = null,
)

sealed interface AppAction {
    data class Navigate(val navGraph: NavGraph) : AppAction
    data class User(val userGraph: UserGraph) : AppAction
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
                when (action.navGraph) {
                    is NavGraph.Back -> {
                        Either.catch {
                            s.value.fromScreen.dropLast(1).let {
                                s.value = s.value.copy(fromScreen = it, currentScreen = it.last())
                            }
                        }
                    }
                    is AppNavGraph -> {
                        s.value = s.value.copy(
                            fromScreen = s.value.fromScreen,
                            currentScreen = action.navGraph
                        )
                    }
                    is UserNavGraph -> {
                        s.value = s.value.copy(
                            fromScreen = s.value.fromScreen.plus(UserNavGraph.books.modify(action.navGraph) { it.copy(new = false) }),
                            currentScreen = action.navGraph
                        )
                    }
                }
            }
            is AppAction.User -> {
                s.value = s.value.copy(userGraph = action.userGraph)
                dispatch(AppAction.Navigate(UserNavGraph.Main))
            }
        }
    }
    
    override val state: StateFlow<AppState> = s
}