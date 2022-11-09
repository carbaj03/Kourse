package com.fintonic.domain.commons.redux

import com.example.myapplication.redux.Slice
import com.example.myapplication.redux.configureStore
import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.AsyncAction
import com.example.myapplication.redux.types.Middleware
import com.example.myapplication.redux.types.Reducer
import com.fintonic.domain.commons.redux.types.*
import kotlinx.coroutines.flow.StateFlow

fun <S : State> createTestMiddleware(actions: MutableList<Action>): Middleware<S> =
    Middleware { _, next, action ->
        if (action !is AsyncAction<*>)
            actions.add(action)
        next(action)
    }

interface MockStore<S : State> : Store<S> {
    val actions: List<Action>
}

fun mockStore(slice: Slice<*>): MockStore<CombineState> =
    object : MockStore<CombineState> {
        private var temp = mutableListOf<Action>()

        private val store = configureStore(slice, middleware = arrayOf(createTestMiddleware(temp)))

        override val dispatch: Dispatcher = store.dispatch

        override val state: StateFlow<CombineState> = store.state

        override val actions: List<Action> = temp
    }

inline fun <reified S : State> mockStore(reducer: Reducer<S>, s: S): MockStore<S> =
    object : MockStore<S> {
        private var temp = mutableListOf<Action>()
        private val store = com.example.myapplication.redux.createStore(
            reducer,
            initialState = s,
            middleware = arrayOf(createTestMiddleware(temp))
        )

        override val dispatch: Dispatcher = store.dispatch

        override val state: StateFlow<S> = store.state

        override val actions: List<Action> = temp
    }