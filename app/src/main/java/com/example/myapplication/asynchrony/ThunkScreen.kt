package com.fintonic.domain.utils.asynchrony

import com.example.myapplication.asynchrony.Event
import com.example.myapplication.asynchrony.ThunkEvent
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.redux.createStore
import com.example.myapplication.redux.types.Reducer
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.commons.redux.types.Store

interface ThunkScreen<S : State, N : Screen, E : Event> :
    WithScope,
    Store<S>,
    ThunkEvent<E>,
    ThunkNavigator<N> {

    companion object {
        inline operator fun <reified S : State, reified N : Screen, reified E : Event> invoke(
            vararg reducer: Reducer<S>,
            initialState: S,
            navigator: ThunkNavigator<N>,
            eventTracker: Array<ThunkEvent<E>>,
            withScope: WithScope = WithScope(),
//            extra: Array<Reducer<S>> = emptyArray(),
        ): ThunkScreen<S, N, E> =
            object : ThunkScreen<S, N, E>,
                WithScope by withScope,
                Store<S> by createStore(
                    reducer = reducer,
                    initialState = initialState,
                ),
                ThunkEvent<E> by eventTracker.fold(),
                ThunkNavigator<N> by navigator {}
    }
}


fun <E : Event> Array<ThunkEvent<E>>.fold(): ThunkEvent<E> =
    ThunkEvent {
        fold(this) { event, reducer ->
            with(reducer) { event() }
            event
        }
    }
