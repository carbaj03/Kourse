package com.fintonic.domain.utils.asynchrony

import com.example.myapplication.asynchrony.EventScreen
import com.example.myapplication.asynchrony.ThunkEvent
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.redux.Slice
import com.example.myapplication.redux.configureStore
import com.fintonic.domain.commons.redux.types.CombineState
import com.fintonic.domain.commons.redux.types.Store

interface ThunkCombineScreen<N : Screen, E : EventScreen> : Store<CombineState>, WithScope, ThunkEvent<E>, ThunkNavigator<N> {
    companion object
}

operator fun <N : Screen, E : EventScreen> ThunkCombineScreen.Companion.invoke(
    vararg reducer: Slice<*>,
    navigator: ThunkNavigator<N>,
    withScope: WithScope = WithScope(),
    eventTracker: ThunkEvent<E>
): ThunkCombineScreen<N, E> =
    object : ThunkCombineScreen<N, E>,
        WithScope by withScope,
        Store<CombineState> by configureStore(*reducer),
        ThunkEvent<E> by eventTracker,
        ThunkNavigator<N> by navigator {}