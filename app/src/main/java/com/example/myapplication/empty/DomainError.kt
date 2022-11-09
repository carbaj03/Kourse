package com.example.myapplication.empty

import arrow.core.Either
import arrow.core.left
import arrow.core.right

sealed interface DomainError {
    object Default : DomainError
}

fun <A, B> A?.toEither(error: B): Either<B, A> =
    this?.right() ?: error.left()