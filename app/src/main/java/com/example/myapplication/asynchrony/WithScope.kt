package com.example.myapplication.asynchrony

import arrow.core.Either
import arrow.core.continuations.EffectScope
import arrow.core.continuations.either
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext


fun WithScope(job: Job = SupervisorJob()): WithScope =
    object : WithScope {
        override val coroutineContext: CoroutineContext = Dispatchers.Main.immediate + job
        override val io: CoroutineContext = Dispatchers.IO
        override val default: CoroutineContext = Dispatchers.Default
        override val jobs: MutableMap<String, () -> Unit> = mutableMapOf()
    }

suspend inline fun <A, B, C> Either<A, B>.fold(
    crossinline ifLeft: suspend (A) -> C,
    crossinline ifRight: suspend (B) -> C
): C =
    when (this) {
        is Either.Right -> ifRight(value)
        is Either.Left -> ifLeft(value)
    }

interface WithScope : CoroutineScope {
    val io: CoroutineContext
    val default: CoroutineContext
    val jobs: MutableMap<String, () -> Unit>

    suspend fun <T> Default(block: suspend CoroutineScope.() -> T): T =
        withContext(context = default, block = block)

    suspend fun <T> IO(block: suspend CoroutineScope.() -> T): T =
        withContext(context = io, block = block)

    suspend fun <T> Main(block: suspend CoroutineScope.() -> T): T =
        withContext(context = coroutineContext, block = block)


    fun launchIo(
        block: suspend CoroutineScope.() -> Unit
    ): Job =
        launch(context = io, block = block)

    fun launchMain(
        block: suspend CoroutineScope.() -> Unit
    ): Job =
        launch(context = coroutineContext, block = block)


    fun <A, B> launchIo(
        f: suspend () -> Either<A, B>,
        error: suspend (A) -> Unit = {},
        success: suspend (B) -> Unit = {},
    ): Job =
        launchMain {
            IO { f() }.fold(error, success)
        }

    fun <A, B> launchIo(
        f: suspend () -> Either<A, B>,
        error: suspend (A) -> Unit = {},
        success: suspend (B) -> Unit = {},
        before: suspend () -> Unit,
        after: suspend () -> Unit,
    ): Job =
        launchMain {
            before()
            IO { f() }.fold(
                ifLeft = { error(it); after() },
                ifRight = { success(it); after() },
            )
        }

    fun <A> launchIoUnSafe(
        f: suspend () -> A,
        success: (A) -> Unit = {}
    ): Job =
        launchMain {
            success(IO { f() })
        }

    fun <A, B> flowIO(
        f: suspend () -> Flow<Either<A, B>>,
        error: suspend (A) -> Unit = {},
        success: suspend (B) -> Unit = {}
    ): Job =
        launchMain {
            IO { f() }.collect {
                it.fold(error, success)
            }
        }

    fun <A, B> eitherIo(
        onSuccess: (A) -> Unit = {},
        onError: suspend (B) -> Unit = {},
        f: suspend EffectScope<B>.() -> A,
    ): Job =
        launchMain {
            either { IO { f(this@either) } }.fold(onError, onSuccess)
        }

    fun <A, E> eitherMain(
        onSuccess: (A) -> Unit = {},
        onError: (E) -> Unit = {},
        f: suspend EffectScope<E>.() -> A,
    ): Job =
        launchMain {
            either { f(this) }.fold(onError, onSuccess)
        }

    fun <T> asyncIo(
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> =
        async(io, block = block)

    fun cancel() {
        coroutineContext.cancelChildren()
    }

    fun cancel(key: String) {
        jobs[key]?.invoke()
    }
}