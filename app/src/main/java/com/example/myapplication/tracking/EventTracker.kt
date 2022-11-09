package com.example.myapplication.tracking

import com.example.myapplication.asynchrony.Event

fun interface EventTracker<E : Event> {
    suspend operator fun invoke(event: E)
}

context(EventTracker<E>)
        suspend fun <E : Event> E.track() =
    invoke(this)

context(EventTracker<E>) suspend inline operator fun <E : Event> E.invoke() =
    invoke(this)