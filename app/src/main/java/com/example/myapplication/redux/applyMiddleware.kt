package com.example.myapplication.redux

import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.Middleware
import com.example.myapplication.redux.types.MiddlewareAPI
import com.fintonic.domain.commons.redux.types.*
import kotlinx.coroutines.flow.StateFlow

fun <S : State> applyMiddleware(
    vararg middlewares: Middleware<S>
): StoreEnhancer<S> =
    StoreEnhancer { createStore ->
        StoreEnhancerStoreCreator { reducer, initialState ->
            val store = createStore(reducer, initialState)
            var dispatch: Dispatch<Action> = Dispatch {
                throw Throwable(
                    "Dispatching while constructing your middleware is not allowed. " +
                            "Other middleware would not be applied to this dispatch."
                )
            }
            val middlewareAPI: MiddlewareAPI<S> = object : MiddlewareAPI<S> {
                override val dispatch: Dispatch<Action> = Dispatch { dispatch(it) }
                override val state: S get() = store.state.value
            }

            dispatch = middlewares.foldRight(store.dispatch) { middleware, dispatcher ->
                Dispatch { middleware(middlewareAPI, dispatcher, it) }
            }
            object : Store<S> {
                override val dispatch: Dispatch<Action> = dispatch
                override val state: StateFlow<S> = store.state
            }
        }
    }