package com.example.myapplication.asynchrony

import com.example.myapplication.tracking.EventTracker
import com.example.myapplication.redux.types.Action

interface Event : Action

fun interface ThunkEvent<E : Event> {
    suspend operator fun E.invoke()
}

context(ThunkEvent<E>)
        suspend inline fun <E : Event> E.track() {
    invoke()
}

operator fun <E : Event, T : Event> EventTracker<T>.invoke(
    f: suspend E.() -> T
): ThunkEvent<E> =
    ThunkEvent { f() }


operator fun <E : Event> ThunkEvent<E>.plus(other: ThunkEvent<E>): Array<ThunkEvent<E>> =
    arrayOf(this, other)