package com.example.myapplication.navigation.result

import arrow.core.flatMap
import kotlin.random.Random


fun Int.sum(num: Int): Result<Int> = kotlin.runCatching { if (Random.nextBoolean()) throw NumberFormatException("SumError") else this + num }

fun Int.multiply(num: Int): Result<Int> = kotlin.runCatching { if (Random.nextBoolean()) throw NumberFormatException("MultiplyError") else this * num }

fun Int.divide(num: Int): Result<Int> = kotlin.runCatching { if (Random.nextBoolean()) throw NumberFormatException("DivideError") else this / num }

fun main() {
    repeat(10) {
        2.sum(3)
            .flatMap { it.multiply(3) }
            .flatMap { it.divide(10) }
            .flatMap { it.sum(5) }
            .map { println(it) }.map { it }
            .recover { println(it.message) }
    }
}