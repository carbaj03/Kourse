package com.fintonic.domain.utils.asynchrony

import com.example.myapplication.asynchrony.Event
import com.example.myapplication.asynchrony.ThunkEvent
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.redux.Slice
import com.example.myapplication.redux.configureStore
import com.example.myapplication.redux.createStore
import com.example.myapplication.redux.types.Action
import com.fintonic.domain.commons.redux.types.CombineState
import com.example.myapplication.redux.types.Reducer
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.commons.redux.types.Store
import com.example.myapplication.redux.types.createEventMiddleware
import com.example.myapplication.redux.types.createNavMiddleware
import com.example.myapplication.redux.types.createTimeTravelMiddleware

interface ThunkScreenT<S : State> : Store<S>, WithScope, WithActions {
    companion object
}

interface ThunkScreenCombine : Store<CombineState>, WithScope, WithActions {
    companion object
}

interface WithActions {
    val actions: MutableList<Action>
}

fun WithActions(): WithActions =
    object : WithActions {
        override val actions: MutableList<Action> = mutableListOf()
    }

inline fun <reified S : State, reified N : Screen, reified E : Event> ThunkScreenT(
    vararg reducer: Reducer<S>,
    eventTracker: Array<ThunkEvent<E>>,
    initialState: S,
    navigator: ThunkNavigator<N>,
    withScope: WithScope,
    withActions: WithActions = WithActions(),
): ThunkScreenT<S> =
    object : ThunkScreenT<S>,
        WithScope by withScope,
        WithActions by withActions,
        Store<S> by createStore(
            reducer = reducer,
            initialState = initialState,
            middleware = arrayOf(
                createTimeTravelMiddleware(withActions.actions),
                eventTracker.fold().run { createEventMiddleware<S, E>(withScope) },
                navigator.run { createNavMiddleware(withScope) }
            ),
        ) {}


inline fun <reified N : Screen, reified E : Event> ThunkScreenCombine(
    vararg slice: Slice<*> = arrayOf(),
    eventTracker: Array<ThunkEvent<E>>,
    navigator: ThunkNavigator<N>,
    withScope: WithScope,
    withActions: WithActions = WithActions()
): ThunkScreenT<CombineState> =
    object : ThunkScreenT<CombineState>,
        WithScope by withScope,
        WithActions by withActions,
        Store<CombineState> by configureStore(
            *slice,
            middleware = arrayOf(
                createTimeTravelMiddleware(withActions.actions),
                eventTracker.fold().run { createEventMiddleware<CombineState, E>(withScope) },
                navigator.run { createNavMiddleware(withScope) }
            )
        ) {}
