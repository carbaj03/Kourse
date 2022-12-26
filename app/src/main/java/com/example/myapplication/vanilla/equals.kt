package com.example.myapplication.vanilla

enum class IsEqual {
    Equal, NotEqual
}

infix fun String.isEqual(other: String): IsEqual = when (this == other) {
    true -> IsEqual.Equal
    false -> IsEqual.NotEqual
}

inline fun <A> isEqual(compare: () -> Boolean, notEqual: A, equal: A): A =
    when (compare()) {
        true -> equal
        false -> notEqual
    }

fun <A> IsEqual.then(notEqual: A, equal: A): A = when (this) {
    IsEqual.Equal -> equal
    IsEqual.NotEqual -> notEqual
}