package com.fintonic.domain.commons.redux

import com.example.myapplication.redux.types.Reducer
import com.fintonic.domain.commons.redux.types.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


fun <S : State> createStore(
    reducer: Reducer<S>,
    preloadedState: S,
    enhancer: StoreEnhancer<S>? = null,
): Store<S> {
    if (enhancer != null) {
        return enhancer { r, initialState -> createStore(r, initialState, null) }(
            reducer,
            preloadedState
        )
    }

    val currentState: MutableStateFlow<S> = MutableStateFlow(preloadedState)

    val dispatch: Dispatcher = Dispatch { action ->
        currentState.value = reducer(currentState.value, action)
        action
    }

    return object : Store<S> {
        override val dispatch: Dispatcher = dispatch
        override val state: StateFlow<S> = currentState
    }
}
