package com.fintonic.domain.utils.asynchrony

import com.example.myapplication.redux.types.Action


interface Screen : Action {
    val route: String
}

fun interface ThunkNavigator<S : Screen> {
    suspend operator fun S.invoke()
}

context(ThunkNavigator<S>)
        suspend inline fun <S : Screen> S.navigate() {
    invoke()
}

class ThunkNavigatorMock<S : Screen>(
    val screens: MutableList<S> = mutableListOf()
) : ThunkNavigator<S> {
    override suspend fun S.invoke() {
        screens.add(this)
    }
}