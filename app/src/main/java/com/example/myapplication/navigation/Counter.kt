package com.example.myapplication.navigation

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class Counter {
    private var counter = 0
    val count: Flow<Int> =
        flow {
            do {
                delay(1000)
                counter += 1
                emit(counter)
            } while (currentCoroutineContext().isActive)
        }
}