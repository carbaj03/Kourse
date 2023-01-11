package com.example.myapplication

import arrow.core.continuations.Raise
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextInt


interface Network {

}

interface DB {

}

context(Network, DB)
fun randomRepository(): RandomRepository =
    object : RandomRepository {
        context(Raise<DomainError>)
        override suspend fun getNext(): Int {
            delay(1000)
            return Random.nextInt(2..4)
        }
    }

interface RandomRepositoryMock : RandomRepository {
    var error: Boolean

    context(Raise<DomainError>)
    override suspend fun getNext(): Int {
        delay(1000)
        return if (error) 3 else 1
    }
}
