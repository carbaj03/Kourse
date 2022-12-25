package com.example.myapplication.empty.home

import com.example.myapplication.empty.NavGraph
import com.example.myapplication.empty.UserNavGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface HomeAction {
    object Books : HomeAction
    object Podcasts : HomeAction
    object Blogs : HomeAction
}

class HomeState

interface HomeThunk {
    fun dispatch(action: HomeAction)
    val state: StateFlow<HomeState>
}

class HomeThunkAndroid(
    val initialState: HomeState,
    val nav: (NavGraph) -> Unit
) : HomeThunk {
    val s = MutableStateFlow(initialState)

    override fun dispatch(action: HomeAction) {
        when (action) {
            HomeAction.Blogs -> nav(UserNavGraph.Blogs)
            HomeAction.Books -> nav(UserNavGraph.Books())
            HomeAction.Podcasts -> nav(UserNavGraph.Podcasts)
        }
    }

    override val state: StateFlow<HomeState> = s
}