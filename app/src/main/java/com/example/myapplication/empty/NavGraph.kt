package com.example.myapplication.empty

import arrow.optics.optics
import com.example.myapplication.todo.Book

sealed class NavGraph(
    open val new: Boolean = true,
) {
    object Back : NavGraph()
}

@optics
sealed class AppNavGraph(
    new: Boolean = true,
) : NavGraph(new) {
    
    object Login : AppNavGraph()
    
    companion object
}

@optics
sealed class UserNavGraph(
    new: Boolean = true,
) : NavGraph(new) {
    object Podcasts : UserNavGraph()
    object Blogs : UserNavGraph()
    data class Books(override val new: Boolean = true) : UserNavGraph(new)
    object Main : UserNavGraph()
    data class BookDetail(val book: Book) : UserNavGraph()
    
    companion object
}