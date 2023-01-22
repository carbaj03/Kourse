package com.example.myapplication.navigation

interface WithSubTab<A> {
    val currentSubTab: SubTab
    val onSubTabSelected: (SubTab) -> Unit

    fun with(current: SubTab): A
}
