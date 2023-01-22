package com.example.myapplication.navigation

import arrow.core.nonFatalOrThrow
import kotlin.experimental.ExperimentalTypeInference


class RaiseException(val raised: Error) : IllegalStateException() {
    companion object {
        private const val serialVersionUID = -9202173006928992231L
    }
}

@OptIn(ExperimentalTypeInference::class)
inline fun <A> raised(
    error: (error: Throwable) -> Unit = {},
    recover: (raised: Error) -> Unit = {},
    transform: (value: A) -> Unit = {},
    @BuilderInference program: Raise<Error>.() -> A,
): Unit {
    val raise = DefaultRaise()
    return try {
        transform(program(raise))
    } catch (e: RaiseException) {
        recover(e.raised)
    } catch (e: Throwable) {
        error(e.nonFatalOrThrow())
    }
}

class DefaultRaise : Raise<Error> {
    override fun raise(error: Error): Nothing = throw RaiseException(error)
}