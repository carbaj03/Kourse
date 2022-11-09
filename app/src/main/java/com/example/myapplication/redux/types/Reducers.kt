package com.example.myapplication.redux.types

import com.example.myapplication.redux.SliceName
import com.fintonic.domain.commons.redux.types.CombineState
import com.fintonic.domain.commons.redux.types.State


fun interface Reducer<S : State> {
    operator fun invoke(state: S, action: Action): S
}

fun interface ReducerType<S : State, A : Action> {
    operator fun invoke(state: S, action: A): S
}

@Suppress("UNCHECKED_CAST")
fun <S : State, A : Action> reducerType(reducer: S.(A) -> S): Reducer<S> =
    Reducer { state, action ->
        try {
            reducer(state, action as A)
        } catch (e: ClassCastException) {
            state
        }
    }

@Suppress("UNCHECKED_CAST")
fun <S : State, A : Action> reducerTypeSlice(reducer: S.(A) -> S): Reducer<State> =
    Reducer { state, action ->
        try {
            reducer(state as S, action as A)
        } catch (e: ClassCastException) {
            state
        }
    }

typealias ReducersMapObject = Map<SliceName, Reducer<State>>

fun combineReducers(reducers: ReducersMapObject): Reducer<CombineState> {
    val finalReducerKeys: Set<SliceName> = reducers.keys

    return Reducer { state, action ->
        var hasChanged = false
        val nextState: CombineState = object : CombineState {
            override val states: MutableMap<SliceName, State> = hashMapOf()
        }
        finalReducerKeys.forEach { key ->
            val reducer: Reducer<State> = reducers[key]!!
            val previousStateForKey: State? = state.states[key]
            val nextStateForKey: State? = previousStateForKey?.let { reducer(it, action) }

            nextStateForKey?.let { nextState.states[key] = nextStateForKey }
            hasChanged = hasChanged || nextStateForKey != previousStateForKey
        }
        hasChanged = hasChanged || finalReducerKeys.size != state.states.keys.size
        (if (hasChanged) nextState else state)
    }
}