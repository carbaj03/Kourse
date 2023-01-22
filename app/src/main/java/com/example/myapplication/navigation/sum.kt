package com.example.myapplication.navigation.raise

import com.example.myapplication.navigation.Error
import com.example.myapplication.navigation.Raise
import com.example.myapplication.navigation.raised
import kotlin.random.Random

context(Raise<Error>)
fun Int.sum(num: Int): Int = if (Random.nextBoolean()) raise(Error.SumError) else this + num

context(Raise<Error>)
fun Int.multiply(num: Int): Int = if (Random.nextBoolean()) raise(Error.MultiplyError) else this * num

context(Raise<Error>)
fun Int.divide(num: Int): Int = if (Random.nextBoolean()) raise(Error.DivideError) else this / num

fun main() {
    repeat(10) {
        raised(
            recover = ::println,
            transform = ::println,
        ) {
            2.sum(2)
                .multiply(3)
                .divide(10)
                .sum(5)
        }
    }
}
