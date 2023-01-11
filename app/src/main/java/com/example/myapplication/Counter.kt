package com.example.myapplication

import arrow.core.NonEmptyList
import arrow.core.continuations.Raise
import io.ktor.util.reflect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class Counter(
    val screen: BackStack,
    val events: List<Event>
)

data class BackStack(
    val screens: NonEmptyList<Screen>
)

sealed interface DomainError {
    data object RandomNumber : DomainError
}

sealed interface Event
data class RandomSuccess(val count: Int) : Event
data object RandomFail : Event

sealed interface Screen

data class Start(
    val next: context(SideEffect, Navigator, Reducer, Tracker) () -> Unit
) : Screen

context(RandomRepository)
fun Start() = Start {
    Splash().navigate()
}

data class Splash(
    val next: () -> Unit
) : Screen

context(SideEffect, Navigator, Reducer, Tracker, RandomRepository)
fun Splash(): Splash =
    Splash {
        sideEffect<Splash> {
            delay(1000)
            Main().navigate()
        }
    }

data class Main(
    val display: String,
    val counter: Int,
    val increment: () -> Unit,
    val decrement: () -> Unit,
    val restart: () -> Unit,
    val random: () -> Unit,
    val loading: Boolean = false,
) : Screen

context(Reducer, SideEffect, Tracker, RandomRepository)
fun Main(): Main =
    Main(
        display = "0",
        counter = 0,
        increment = { reduce<Main> { (counter + 1).let { copy(counter = it, display = it.toString()) } } },
        decrement = { reduce<Main> { (counter - 1).let { copy(counter = it, display = it.toString()) } } },
        restart = { reduce<Main> { copy(counter = 0, display = "0") } },
        random = {
            sideEffect<Main>(
                recover = {
                    reduce<Main> { copy(loading = false, display = "Bad number") }
                    RandomFail.track()
                }
            ) {
                reduce<Main> { copy(loading = true) }
                val randomCount = randomNumberUseCase()
                RandomSuccess(randomCount).track()
                reduce<Main> { copy(counter = randomCount, loading = false, display = randomCount.toString()) }
            }
        }
    )

interface RandomRepository {
    context(Raise<DomainError>)
    suspend fun getNext(): Int
}

context(Raise<DomainError>, RandomRepository)
suspend fun randomNumberUseCase(): Int {
    return getNext().also { if (it == 3) raise(DomainError.RandomNumber) }
}

interface WithScreen<S : Screen> {
    val screen: S
}

suspend inline operator fun <reified S : Screen> StateFlow<Counter>.invoke(
    crossinline f: suspend WithScreen<S>.() -> Unit
) {
    object : WithScreen<S> {
        override val screen: S
            get() = value.screen.screens.last().let {
                require(it.instanceOf(S::class)) { "${it::class} is not ${S::class}" }
                return it as S
            }
    }.let { f(it) }
}

suspend inline fun <reified S : Screen> StateFlow<Counter>.assert(
    crossinline f: suspend WithScreen<S>.() -> Unit
) {
    object : WithScreen<S> {
        override val screen: S
            get() = value.screen.screens.last().let {
                assert(it.instanceOf(S::class)) { "${it::class} is not ${S::class}" }
                return it as S
            }
    }.let { f(it) }
}