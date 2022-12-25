package com.example.myapplication.asynchrony

import com.example.myapplication.tracking.EventTracker
import com.example.myapplication.redux.types.Action

interface EventScreen : Action

fun interface ThunkEvent<E : EventScreen> {
    suspend operator fun E.invoke()
}

context(ThunkEvent<E>)
suspend inline fun <E : EventScreen> E.track() {
    invoke()
}

operator fun <E : EventScreen, T : EventScreen> EventTracker<T>.invoke(
    f: suspend E.() -> T
): ThunkEvent<E> =
    ThunkEvent { f() }


operator fun <E : EventScreen> ThunkEvent<E>.plus(other: ThunkEvent<E>): Array<ThunkEvent<E>> =
    arrayOf(this, other)