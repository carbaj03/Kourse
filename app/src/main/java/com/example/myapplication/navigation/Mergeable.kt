package com.example.myapplication.navigation

fun interface Mergeable<A : Screen> {
    infix operator fun A.plus(other: A): A
}

fun interface OnStop<A : Screen> {
      fun A.stop(): Unit
}