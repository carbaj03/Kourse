package com.example.myapplication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

suspend fun main() {
    val network = object : Network {}
    val db = object : DB {}
    val repository: RandomRepository by lazy {
        with(network, db) { randomRepository() }
    }
    val store = Store(
        start = with(repository) { Start() },
        coroutineScope = CoroutineScope(Job())
    )

    with(store.navigator, store.reducer, store.sideEffect, store.tracker, repository) {
        launch {
            store.state.map { it.events }.distinctUntilChanged().collect {
                println(it)
            }
        }
        store.state<Start> {
            screen.next(store.sideEffect, store.navigator, store.reducer, store.tracker)
        }
        store.state<Splash> {
            screen.next()
        }
        delay(1100)
        store.state<Main> {
            screen.decrement()
            println(screen.display)
            screen.increment()
            println(screen.display)
            screen.increment()
            println(screen.display)
            screen.restart()
            println(screen.display)
            screen.random()
            delay(1050)
            println(screen.display)
        }
    }

    delay(4000)
}
