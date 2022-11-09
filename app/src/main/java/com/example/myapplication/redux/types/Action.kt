package com.example.myapplication.redux.types

import com.fintonic.domain.commons.redux.types.CombineState
import com.fintonic.domain.commons.redux.types.Dispatcher
import com.fintonic.domain.commons.redux.types.State

interface Action

fun interface AsyncAction<S : State> : Action {
    operator fun invoke(
        state: S,
        dispatcher: Dispatcher,
    )
}

fun interface AsyncAction1<S : State> : Action {
    operator fun invoke(
        state: () -> S,
        dispatcher: Dispatcher,
    )
}

fun interface ActionState<S : State> : Action {
    operator fun invoke(s : S): S
}