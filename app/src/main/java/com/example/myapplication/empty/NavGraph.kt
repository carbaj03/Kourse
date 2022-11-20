package com.example.myapplication.empty

import arrow.optics.optics
import com.example.myapplication.todo.Book

@optics
sealed class NavGraph(
    open val new : Boolean = true
) {
    object Podcasts : NavGraph()
    object Blogs : NavGraph()
    data class Books(override val new: Boolean = true) : NavGraph(new)
    data class Main(val userGraph: UserGraph) : NavGraph()
    object Login : NavGraph()
    data class BookDetail(val book: Book) : NavGraph()

    object Back : NavGraph()

    companion object
}