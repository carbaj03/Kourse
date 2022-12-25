package com.example.myapplication.tracking

import com.example.myapplication.asynchrony.EventScreen


fun interface EventTracker<E : EventScreen> {
    suspend operator fun invoke(event: E)
}

context(EventTracker<E>)
suspend fun <E : EventScreen> E.track() =
    invoke(this)

context(EventTracker<E>)
suspend inline operator fun <E : EventScreen> E.invoke() =
    invoke(this)