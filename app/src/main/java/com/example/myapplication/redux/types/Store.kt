package com.fintonic.domain.commons.redux.types

import com.example.myapplication.redux.SliceName
import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.Reducer
import kotlinx.coroutines.flow.StateFlow


typealias Dispatcher = Dispatch<Action>

fun interface Dispatch<A : Action> {
    operator fun invoke(action: A): A
}

interface State

interface CombineState : State {
    val states: MutableMap<SliceName, State>
}

interface Store<S : State> {
    val dispatch: Dispatcher
    val state: StateFlow<S>
}

context(Dispatcher)
fun Action.dispatch() =
    invoke(this)

context(Store<S>)
fun <S : State> Action.dispatch() =
    dispatch(this)

fun interface StoreEnhancer<S : State> {
    operator fun invoke(next: StoreEnhancerStoreCreator<S>): StoreEnhancerStoreCreator<S>
}

fun interface StoreEnhancerStoreCreator<S : State> {
    operator fun invoke(
        reducer: Reducer<S>,
        preloadedState: S
    ): Store<S>
}