package com.example.myapplication.empty

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface LoginAction {
    data class ChangeName(val name: String) : LoginAction
    object Send : LoginAction
}

data class LoginState(val name: String)

interface LoginThunk {
    fun dispatch(action: LoginAction)
    val state: StateFlow<LoginState>
}

class LoginThunkAndroid(
    val nav: (NavGraph) -> Unit
) : LoginThunk {
    val s = MutableStateFlow(LoginState(""))

    override fun dispatch(action: LoginAction) {
        when (action) {
            is LoginAction.ChangeName -> s.value = s.value.copy(name = action.name)
            is LoginAction.Send -> nav(NavGraph.Main)
        }
    }

    override val state: StateFlow<LoginState> = s
}
