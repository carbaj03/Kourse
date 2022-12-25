package com.example.myapplication.empty.login

import com.example.myapplication.empty.AppAction
import com.example.myapplication.empty.AuthService
import com.example.myapplication.empty.NavGraph
import com.example.myapplication.empty.UserNavGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginAction {
    data class ChangeName(val name: String) : LoginAction
    data class ChangePassword(val password: String) : LoginAction
    object LogIn : LoginAction
    object SignIn : LoginAction
}

data class LoginState(
    val name: String,
    val password: String,
    val error: String? = null,
)

interface LoginThunk {
    fun dispatch(action: LoginAction)
    val state: StateFlow<LoginState>
}

context(AuthService, CoroutineScope)
class LoginThunkAndroid(
    val nav: (NavGraph) -> Unit,
    val appAction: (AppAction) -> Unit,
) : LoginThunk {
    val s = MutableStateFlow(LoginState("", ""))
    
    override fun dispatch(action: LoginAction) {
        when (action) {
            is LoginAction.ChangeName -> s.value = s.value.copy(name = action.name)
            is LoginAction.ChangePassword -> s.value = s.value.copy(password = action.password)
            is LoginAction.LogIn -> {
                launch {
                    login(s.value.name, s.value.password).fold(
                        {
                            s.value = s.value.copy(error = "Invalid user")
                            delay(2000)
                            s.value = s.value.copy(error = null)
                        },
                        {
                            appAction(AppAction.User(it))
                        }
                    )
                }
            }
            is LoginAction.SignIn -> {
//                nav(NavGraph.Main(createUserGraph()))
            }
        }
    }
    
    override val state: StateFlow<LoginState> = s
}
