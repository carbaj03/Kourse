package com.example.myapplication.navigation

interface WithTab<A> {
    val currentTab: Tab
    val onTabSelected: (Tab) -> Unit

    fun with(current: Tab): A
}