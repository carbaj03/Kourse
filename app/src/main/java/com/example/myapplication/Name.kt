package com.example.myapplication

import kotlinx.coroutines.*

suspend fun main() {
    val ctx = CoroutineScope(Job() + CoroutineName("CustomName"))

    ctx.launch {
        println(coroutineContext[CoroutineName]?.name) //CustomName
    }

    val ctxWithoutName = CoroutineScope(Job())

    ctxWithoutName.launch {
        println(coroutineContext[CoroutineName]?.name) // null
    }

    awaitCancellation()
}