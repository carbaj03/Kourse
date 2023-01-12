package com.example.myapplication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


interface Logger {
    fun error(msg: String)
}

data class CoroutineLogger(
    val logger: Logger
) : AbstractCoroutineContextElement(CoroutineLogger) {

    companion object Key : CoroutineContext.Key<CoroutineLogger>
}

fun CoroutineScope.log(msg: String) {
    coroutineContext[CoroutineLogger]?.logger?.error(msg)
}

suspend fun log(msg: String) {
    coroutineContext[CoroutineLogger]?.logger?.error(msg)
}

suspend fun myUseCase() {
    log("myUseCase")
}

suspend fun main() {
    val logger = object : Logger {
        override fun error(msg: String) {
            println(msg)
        }
    }

    val ctx = CoroutineScope(CoroutineLogger(logger))

    ctx.launch {
        log("Implicit logger")
        myUseCase()
    }

    awaitCancellation()
}



interface Repository {
    suspend fun doSomething()
}

val repository = object : Repository {
    override suspend fun doSomething() {
        log("repository")
    }
}
