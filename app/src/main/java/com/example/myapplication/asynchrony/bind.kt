package com.example.myapplication.asynchrony

import arrow.core.Either
import arrow.core.continuations.EffectScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

context(EffectScope<E>)
        suspend inline fun <E, A> bind(
    crossinline f: suspend () -> Either<E, A>
): A =
    f().bind()

context(EffectScope<E>)
        suspend inline fun <E, A> bindNull(
    e: E,
    crossinline f: suspend () -> A?
): A =
    f().bindNull(e)

context(EffectScope<E>)
        suspend fun <E, A> A?.bindNull(e: E): A =
    this ?: shift(e)

context(WithScope, EffectScope<E>)
fun <A, E> bindAsync(
    f: suspend () -> Either<E, A>
): Deferred<A> =
    async(io) { f().bind() }