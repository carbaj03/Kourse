package com.fintonic.domain.commons.redux

import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.Reducer
import com.example.myapplication.redux.types.ReducerType
import com.fintonic.domain.commons.redux.types.State

fun <S : State> combineReducers(
    vararg reducers: Reducer<S>
): Reducer<S> =
    Reducer { state, action ->
        reducers.fold(state) { s, reducer -> reducer(s, action) }
    }

fun <S : State> combineReducers(
    reducers: List<Reducer<S>>
): Reducer<S> =
    Reducer { state, action ->
        reducers.fold(state) { s, reducer -> reducer(s, action) }
    }

fun <S : State, A : Action> combineReducers(
    vararg reducers: ReducerType<S, A>
): ReducerType<S, A> =
    ReducerType { state, action ->
        reducers.fold(state) { s, reducer -> reducer(s, action) }
    }

fun <S : State, A : Action> combineReducers(
    reducers: List<ReducerType<S, A>>
): ReducerType<S, A> =
    ReducerType { state, action ->
        reducers.fold(state) { s, reducer -> reducer(s, action) }
    }