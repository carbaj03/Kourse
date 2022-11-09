package com.fintonic.domain.utils.asynchrony

import arrow.core.Either
import arrow.core.handleError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

interface Poller {

    val coroutineContextPoller: CoroutineContext
    val ioPoller: CoroutineContext
    val pollings: MutableList<Job>

    fun cancelPolling() {
        pollings.onEach { it.cancel() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T, G> CoroutineScope.polling(
        delayTime: Long = 5000,
        f: suspend () -> Either<T, G>,
        success: suspend (G) -> Unit = {},
        error: suspend (T) -> Unit = {}
    ): Job =
        launch {
            channelFlow<G> {
                while (!isClosedForSend) {
                    delay(delayTime)
                    withContext(ioPoller) { f() }
                            .map { withContext(coroutineContextPoller) { success(it) } }
                            .handleError { withContext(coroutineContextPoller) { error(it) } }
                }
            }.flowOn(ioPoller).collect()
        }.also {
            pollings.add(it)
        }
}