package com.example.myapplication.redux

import com.example.myapplication.redux.types.*
import com.fintonic.domain.commons.redux.combineReducers
import com.fintonic.domain.commons.redux.types.*

fun configureStore(
    vararg slice: Slice<*>,
    middleware: Array<Middleware<CombineState>> = emptyArray(),
): Store<CombineState> =
    com.fintonic.domain.commons.redux.createStore(
        reducer = combineReducers(
            (slice.associate { it.reducer }.asSequence() + slice.associate { it.extraReducer }.asSequence())
                .distinct()
                .groupBy({ it.key }, { it.value })
                .mapValues { combineReducers(it.value) }
        ),
        preloadedState = object : CombineState {
            override val states: MutableMap<SliceName, State> = slice.associate { it.initialState }.toMutableMap()
        },
        enhancer = applyMiddleware(createThunkMiddleware(), *middleware)
    )

inline fun <reified S : State> createStore(
    vararg reducer: Reducer<S>,
    initialState: S,
    middleware: Array<Middleware<S>> = emptyArray(),
): Store<S> =
    com.fintonic.domain.commons.redux.createStore(
        reducer = combineReducers(*reducer),
        preloadedState = initialState,
        enhancer = applyMiddleware(createThunkMiddleware(), *middleware)
    )


@JvmInline
value class SliceName(val value: String)

inline fun <reified S : State> sliceName(): SliceName =
    SliceName(S::class.simpleName!!)

interface Slice<S : State> {
    val name: SliceName
    val initialState: Pair<SliceName, S>
    val reducer: Pair<SliceName, Reducer<State>>
    val extraReducer: Pair<SliceName, Reducer<State>>
}

inline fun <reified S : State> createSliceMutable(
    nameDefault: SliceName = sliceName<S>(),
    initialState: S,
): Slice<S> =
    object : Slice<S> {
        override val name: SliceName =
            nameDefault
        override val reducer: Pair<SliceName, Reducer<State>> =
            nameDefault to reducerTypeSlice<S, ActionState<S>> { action -> action(this) }
        override val initialState: Pair<SliceName, S> =
            nameDefault to initialState
        override val extraReducer: Pair<SliceName, Reducer<State>> =
            nameDefault to reducerTypeSlice<S, Action> { this }
    }


inline fun <reified S : State, reified A : Action> createSlice(
    initialState: S,
    nameDefault: SliceName = sliceName<S>(),
    crossinline extra: S.(Action) -> S = { _ -> this },
    crossinline reducer: S.(A) -> S,
): Slice<S> =
    object : Slice<S> {
        override val name: SliceName =
            nameDefault
        override val reducer: Pair<SliceName, Reducer<State>> =
            nameDefault to reducerTypeSlice<S, A> { action -> reducer(action) }
        override val initialState: Pair<SliceName, S> =
            nameDefault to initialState
        override val extraReducer: Pair<SliceName, Reducer<State>> =
            nameDefault to reducerTypeSlice<S, A> { action -> extra(action) }
    }

//@Suppress("UNCHECKED_CAST")
//fun <S : State> Slice<S>.select(state: CombineState): S? =
//    state.states[name] as S?

@Suppress("UNCHECKED_CAST")
inline fun <reified S : State> CombineState.select(): S? =
    states[sliceName<S>()] as S?

inline fun <reified S : State> createActionSlice(crossinline f: (S, Dispatcher) -> Unit): AsyncAction<CombineState> =
    AsyncAction { states, dispatcher -> states.select<S>()?.let { f(it, dispatcher) } }

fun <S : State> createAction(f: (S, Dispatcher) -> Unit): AsyncAction<S> =
    AsyncAction { state, dispatcher -> f(state, dispatcher) }

fun <S : State> createAction1(f: (() -> S, Dispatcher) -> Unit): AsyncAction1<S> =
    AsyncAction1 { state, dispatcher -> f(state, dispatcher) }