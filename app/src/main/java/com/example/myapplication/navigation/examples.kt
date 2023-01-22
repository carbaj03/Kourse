package com.example.myapplication.navigation

import arrow.optics.optics


@optics
sealed class NetworkResult {
    companion object
}

@optics
data class Success(val content: String) : NetworkResult() {
    companion object
}

@optics
sealed class NetworkError : NetworkResult() {
    companion object
}

@optics
data class HttpError(val message: String) : NetworkError() {
    companion object
}

object TimeoutError : NetworkError()


fun main() {
    val networkResult: NetworkResult = HttpError("boom!")
    val f: (String) -> String = String::toUpperCase
    NetworkResult.networkError.httpError.message.modify(networkResult, f)
}