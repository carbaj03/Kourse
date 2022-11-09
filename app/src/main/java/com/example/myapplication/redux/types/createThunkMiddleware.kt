package com.example.myapplication.redux.types

import com.example.myapplication.asynchrony.Event
import com.example.myapplication.asynchrony.ThunkEvent
import com.example.myapplication.asynchrony.WithScope
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.utils.asynchrony.Screen
import com.fintonic.domain.utils.asynchrony.ThunkNavigator

fun <S : State> createTimeTravelMiddleware(actions: MutableList<Action>): Middleware<S> =
    Middleware { _, next, action ->
        if (action !is AsyncAction<*>) {
            actions.add(action)
        }

        next(action)
    }

@Suppress("UNCHECKED_CAST")
fun <S : State> createThunkMiddleware(): Middleware<S> =
    Middleware { store, next, action ->
        if (action is AsyncAction<*>) {
            (action as AsyncAction<S>)(
                state = store.state,
                dispatcher = store.dispatch
            )
            action
        } else {
            next(action)
        }
    }

context(ThunkEvent<E>) inline fun <S : State, reified E : Event> createEventMiddleware(
    withScope: WithScope
): Middleware<S> =
    Middleware { _, next, action ->
        if (action is E) {
            withScope.launchIo { action() }
        }
        next(action)
    }

context(ThunkNavigator<SC>) inline fun <S : State, reified SC : Screen> createNavMiddleware(
    withScope: WithScope
): Middleware<S> =
    Middleware { _, next, action ->
        if (action is SC) {
            withScope.launchMain { action() }
        }
        next(action)
    }