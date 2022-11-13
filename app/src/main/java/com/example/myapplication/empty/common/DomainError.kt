package com.example.myapplication.empty.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right

sealed interface DomainError {
    object Default : DomainError
}

sealed interface NetworkError {
    object Default : NetworkError
}

sealed interface DBError {
    object Default : DBError
}

fun <A, B> A?.toEither(error: B): Either<B, A> =
    this?.right() ?: error.left()