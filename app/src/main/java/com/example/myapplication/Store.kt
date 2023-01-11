package com.example.myapplication

import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import arrow.core.continuations.fold
import arrow.core.identity
import arrow.core.nonEmptyListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface Store {
    val state: StateFlow<Counter>
    val navigator: Navigator
    val tracker: Tracker
    val reducer: Reducer
    val sideEffect: SideEffect
//    fun init() : context(SideEffect, Navigator, Reducer, Tracker)() -> Unit
}

//interface Store :Navigator , Tracker, Reducer, SideEffect,StateFlow<Counter>

fun interface Navigator {
    fun Screen.navigate()
}

fun interface Tracker {
    fun Event.track()
}

interface Reducer {
    fun Counter.reduce()
    val state: Counter
}

context(Reducer)
inline fun <reified SC : Screen> reduce(crossinline f: SC.() -> SC): Unit =
    when (val screen: Screen? = state.screen.screens.lastOrNull()) {
        is SC -> state.copy(screen = BackStack(state.screen.screens.map { if (it is SC) f(screen) else it }))
        else -> state
    }.reduce()

interface SideEffect : CoroutineScope {
    val state: Counter
}

context(SideEffect)
inline fun <reified SC : Screen> sideEffect(
    noinline recover: suspend (DomainError) -> Unit = {},
    crossinline f: suspend context(SC, Raise<DomainError>) () -> Unit //S, receiver workaround
): Unit {
    when (val screen: Screen? = state.screen.screens.lastOrNull()) {
        is SC -> launch {
            effect { f(screen, this@effect) }.fold(recover, ::identity)
        }
        else -> Unit
    }
}

fun Store(
    start : Start,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
): Store {
    val state: MutableStateFlow<Counter> = MutableStateFlow(
        Counter(screen = BackStack(screens = nonEmptyListOf(start)), events = emptyList())
    )

    val navigator = Navigator {
        state.value = state.value.copy(screen = BackStack( state.value.screen.screens.plus(this)))
    }

    val tracker = Tracker {
        state.value = state.value.copy(events = state.value.events.plus(this))
    }

    val reducer: Reducer = object : Reducer {
        override fun Counter.reduce() {
            state.value = this
        }

        override val state: Counter
            get() = state.value
    }


    val sideEffect: SideEffect = object : SideEffect, CoroutineScope by coroutineScope {
        override val state: Counter
            get() = state.value
    }

    return object : Store {
        override val state: StateFlow<Counter> = state
        override val navigator: Navigator = navigator
        override val reducer: Reducer = reducer
        override val sideEffect: SideEffect = sideEffect
        override val tracker: Tracker = tracker
    }
}
