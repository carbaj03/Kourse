package com.example.myapplication.redux.types

import com.fintonic.domain.commons.redux.types.Dispatcher
import com.fintonic.domain.commons.redux.types.State

interface MiddlewareAPI<S : State> {
    val dispatch: Dispatcher
    val state : S
}

fun interface Middleware<S : State> {
    operator fun invoke(api: MiddlewareAPI<S>, next: Dispatcher, action: Action): Action
}