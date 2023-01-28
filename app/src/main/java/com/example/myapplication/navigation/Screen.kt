package com.example.myapplication.navigation

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface Screen {
    val route: String
}

data object Start : Screen {
    override val route: String = "Start"
}

data class Splash(
    val next: () -> Unit,
    override val route: String = "Splash",
) : Screen

data class Home(
    val login: () -> Unit,
    val signUp: () -> Unit,
    override val route: String = "Home",
) : Screen

data class Login(
    val back: () -> Unit,
    val next: () -> Unit,
    val name: String,
    val onChange: (String) -> Unit,
    override val route: String = "Login",
    override val stop: () -> Unit,
) : Screen, Stoppable

data class LoginMutable(
    var back: () -> Unit,
    var next: () -> Unit,
    var name: String,
    var onChange: (String) -> Unit,
)

data class SignUp(
    val back: () -> Unit,
    val next: () -> Unit,
    val name: String,
    override val route: String = "SignUp",
) : Screen

sealed interface Password : Screen {
    val back: () -> Unit
    val next: () -> Unit
    val password: String

    data class Login(
        override val back: () -> Unit,
        override val next: () -> Unit,
        override val password: String,
        override val route: String = "PasswordLogin",
    ) : Password

    data class Signup(
        override val back: () -> Unit,
        override val next: () -> Unit,
        override val password: String,
        override val route: String = "PasswordSignup",
    ) : Password
}

data class Detail(
    val title: String,
    val back: () -> Unit,
    override val route: String = "Detail",
    override val stop: () -> Unit
) : Screen, Stoppable


context(Navigator, Reducer, SearchRepository, SideEffect)
fun Start() {
    Splash().navigate(addToStack = false)
}

context(Navigator, Reducer, SearchRepository, SideEffect)
fun Splash(): Splash =
    Splash(
        next = {
            launch {
                delay(1000)
                Home().navigate(addToStack = false)
            }
        },
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun Home(): Home =
    Home(
        login = { Login().navigate() },
        signUp = { SignUp().navigate() }
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun Login(): Login =
    Login(
        next = {
            state<Login> { if (name == "name") LoginPassword().navigate() }
        },
        back = { back() },
        name = "",
        onChange = {
            reducer<Login> { copy(name = it) }
//            reducerM<Login, LoginMutable> { name = it }
        },
        stop = {
            cancel()
        }
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun SignUp(): SignUp =
    SignUp(
        next = { SignupPassword() },
        back = {},
        name = "name"
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun LoginPassword(): Password =
    Password.Login(
        password = "",
        next = { with(Counter()) { Dashboard() }.navigate(clearAll = true) },
        back = { back() },
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun SignupPassword(): Password =
    Password.Signup(
        password = "",
        next = { with(Counter()) { Dashboard().navigate() } },
        back = { back() },
    )

context(Reducer)
fun Detail(): Detail =
    Detail(
        back = { back() },
        title = "asdf",
        stop = {}
    )