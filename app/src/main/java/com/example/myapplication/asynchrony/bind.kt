package com.example.myapplication.asynchrony

import arrow.core.Either
import arrow.core.continuations.Raise
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

context(Raise<E>)
suspend inline fun <E, A> bind(
    crossinline f: suspend () -> Either<E, A>
): A =
    f().bind()

context(Raise<E>)
suspend inline fun <E, A> bindNull(
    e: E,
    crossinline f: suspend () -> A?
): A =
    f().bindNull(e)

context(Raise<E>)
suspend fun <E, A> A?.bindNull(e: E): A =
    this ?: raise(e)

context(WithScope, Raise<E>)
fun <A, E> bindAsync(
    f: suspend () -> Either<E, A>
): Deferred<A> =
    async(io) { f().bind() }