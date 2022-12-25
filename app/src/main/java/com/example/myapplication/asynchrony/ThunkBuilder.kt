package com.example.myapplication.asynchrony

import com.example.myapplication.redux.Slice
import com.example.myapplication.redux.createSlice
import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.ActionState
import com.example.myapplication.redux.types.Reducer
import com.example.myapplication.redux.types.reducerType
import com.example.myapplication.tracking.EventTracker
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.utils.asynchrony.*
import kotlin.experimental.ExperimentalTypeInference

interface ReducerBuilder<S : State> {
    val reducer: MutableList<Reducer<S>>
}

interface SliceBuilder {
    val slices: MutableList<Slice<*>>
}

interface TrackerBuilder<E : EventScreen> {
    val eventTracker: MutableList<ThunkEvent<E>>
}


data class ThunkBuilder<S : State, N : Screen, E : EventScreen>(
    val initialState: S,
    val navigator: ThunkNavigator<N> = ThunkNavigatorMock(),
    val withScope: WithScope = WithScope(),
    override val reducer: MutableList<Reducer<S>> = mutableListOf(),
    override val eventTracker: MutableList<ThunkEvent<E>> = mutableListOf(),
) : ReducerBuilder<S>, TrackerBuilder<E>

data class ThunkBuilderSlice<N : Screen, E : EventScreen>(
    val navigator: ThunkNavigator<N> = ThunkNavigatorMock(),
    val withScope: WithScope = WithScope(),
    override val slices: MutableList<Slice<*>> = mutableListOf(),
    override val eventTracker: MutableList<ThunkEvent<E>> = mutableListOf(),
) : SliceBuilder, TrackerBuilder<E>

inline fun <reified S : State, reified N : Screen, reified E : EventScreen> ThunkScreenT(
    initialState: S,
    navigator: ThunkNavigator<N>,
    withScope: WithScope,
    f: ThunkBuilder<S, N, E>.() -> Unit,
): ThunkScreenT<S> =
    ThunkBuilder<S, N, E>(initialState = initialState)
        .also { it.addReducer { action: ActionState<S> -> action(this) } }
        .also(f)
        .run {
            ThunkScreenT(
                initialState = initialState,
                reducer = reducer.toTypedArray(),
                eventTracker = eventTracker.toTypedArray(),
                navigator = navigator,
                withScope = withScope,
            )
        }

inline operator fun <reified S : State, reified N : Screen, reified E : EventScreen> ThunkScreen.Companion.invoke(
    initialState: S,
    navigator: ThunkNavigator<N>,
    withScope: WithScope,
    f: ThunkBuilder<S, N, E>.() -> Unit,
): ThunkScreen<S, N, E> =
    ThunkBuilder<S, N, E>(initialState = initialState)
        .also { it.addReducer { action: ActionState<S> -> action(this) } }
        .also(f)
        .run {
            ThunkScreen(
                initialState = initialState,
                reducer = reducer.toTypedArray(),
                eventTracker = eventTracker.toTypedArray(),
                navigator = navigator,
                withScope = withScope,
            )
        }

inline fun <reified S : State, reified A : Action> ReducerBuilder<S>.addReducer(noinline f: S.(A) -> S) {
    reducer.add(reducerType(f))
}

inline fun <reified S : State, reified A : Action> SliceBuilder.addSlice(initialState: S, crossinline f: S.(A) -> S) {
    slices.add(createSlice(initialState = initialState, reducer = { a: A -> f(this, a) }))
}

inline fun <reified S : State> SliceBuilder.addSlice(slice: Slice<S>) {
    slices.add(slice)
}

@OptIn(ExperimentalTypeInference::class)
fun <ES : EventScreen, E : EventScreen> TrackerBuilder<ES>.addTracker(
    tracker: EventTracker<E>,
    @BuilderInference f: suspend ES.() -> E?,
) {
    eventTracker.add(ThunkEvent { f(this)?.let { tracker(it) } })
}

fun <E : EventScreen> TrackerBuilder<E>.addTracker(f: ThunkEvent<E>) {
    eventTracker.add(f)
}