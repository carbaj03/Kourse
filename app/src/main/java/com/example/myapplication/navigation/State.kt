package com.example.myapplication.navigation

import android.util.Log
import com.example.myapplication.navigation.Tab.*
import com.example.myapplication.with
import io.ktor.util.reflect.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map


interface State

data class App(
    val screen: Screen,
    val stack: BackStack = BackStack(emptyList()),
    val finish: () -> Unit = {},
    val counter: Int = 0,
) : State


@JvmInline
value class BackStack(
    val screens: List<Screen>,
)

context(Reducer)
fun updateStake(): List<Screen> {
    if (state.stack.screens.isEmpty()) return emptyList()
    val a = state.stack.screens.getOrNull(state.stack.screens.size - 2)
    val b = state.screen
    return a?.let { listOf(a, b) } ?: emptyList()
}

context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> updateStack(): List<A>? =
    updateStake().filterIsInstance<A>().let { if (it.size <= 1) return null else it }


context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> updateStakeWith(): A? =
    updateStack()?.reduce { a, b -> a + b }


//inline fun <reified S : Screen> App.navigateFromStack(default: S): App =
//    (stack.screens.firstOrNull { it is S } as S?)
//        ?.let { copy(screen = it, stack = BackStack(stack.screens.filter { it !is S })) }
//        ?: copy(screen = default)


sealed interface Screen{
    val route : String
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
) : Screen

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

sealed interface Tab {
    data class Tab1(
        val counter: Int,
        val setCounter: (Int) -> Unit,
        val back: () -> Unit,
    ) : Tab

    data class Tab2(
        val counter: Int,
        val setCounter: (Int) -> Unit,
        val detail: () -> Unit,
        val back: () -> Unit,
    ) : Tab

    data class Tab3(
        val screen: Tab3Content,
    ) : Tab
}

sealed interface Tab3Content

data class Tab3Screen1(
    val next: () -> Unit
) : Tab3Content

data class Tab3Screen2(
    val next: () -> Unit,
    val back: () -> Unit,
) : Tab3Content

context(Reducer, Navigator)
fun Tab3Screen1(): Tab3Screen1 =
    Tab3Screen1(
        next = {
            navigate<Dashboard> {
                copy(currentTab = currentTab<Tab3> { copy(screen = Tab3Screen2()) })
            }
        }
    )

context(Reducer)
fun Tab3Screen2(): Tab3Screen2 =
    Tab3Screen2(
        back = { back() },
        next = {},
    )

context(Reducer, Navigator)
fun Tab1(): Tab1 =
    Tab1(
        counter = 0,
        setCounter = { newCounter ->
            reducer<Dashboard> {
                copy(
                    currentTab = currentTab<Tab1> {
                        copy(counter = newCounter)
                    }
                )
            }
        },
        back = { back() }
    )

context(Reducer, Navigator)
fun Tab2(): Tab2 =
    Tab2(
        counter = 0,
        setCounter = { reducer { copy(counter = it) } },
        detail = { Screen1Detail().navigate() },
        back = {}
    )

context(Reducer, Navigator)
fun Tab3(): Tab3 =
    Tab3(
        screen = Tab3Screen1(),
    )


interface TabContext<A : Tab> {
    val tab: A
}

inline fun <reified S : Tab> Tab.assert(
    crossinline f: TabContext<S>.() -> Unit
) {
    object : TabContext<S> {
        override val tab: S
            get() {
                assert(this@assert.instanceOf(S::class)) { "${this@assert::class} is not ${S::class}" }
                return this@assert as S
            }
    }.let { f(it) }
}

inline operator fun <reified A : Tab> Tab.invoke(f: A.() -> A): Tab =
    when (this) {
        is A -> f(this)
        else -> this
    }

data class Dashboard(
    val tab1: Tab1,
    val tab2: Tab2,
    val tab3: Tab3,
    val currentTab: Tab,
    val onTabSelected: (Tab) -> Unit,
) : Screen{
    override val route: String get() =  when(currentTab){
        is Tab1 -> "DashboardTab1"
        is Tab2 -> "DashboardTab2"
        is Tab3 -> when(currentTab.screen){
            is Tab3Screen1 -> "Tab3Screen1"
            is Tab3Screen2 -> "Tab3Screen2"
        }
    }
}


fun interface Mergeable<A : Screen> {
    infix operator fun A.plus(other: A): A
}

context(Navigator, Reducer)
fun Dashboard(): Dashboard {
    val tab1 = Tab1()
    val tab2 = Tab2()
    val tab3 = Tab3()
    return Dashboard(
        currentTab = tab1,
        onTabSelected = {
            navigate<Dashboard> {
                when (currentTab) {
                    is Tab1 -> if (it is Tab1) this else copy(tab1 = currentTab, currentTab = it)
                    is Tab2 -> if (it is Tab2) this else copy(tab2 = currentTab, currentTab = it)
                    is Tab3 -> if (it is Tab3) this else copy(tab3 = currentTab, currentTab = it)
                }
            }
        },
        tab1 = tab1,
        tab2 = tab2,
        tab3 = tab3,
    )
}

context(Navigator, Reducer)
fun Splash(): Splash =
    Splash(
        next = { Home().navigate(false) },
    )

context(Navigator, Reducer)
fun Home(): Home =
    Home(
        login = { Login().navigate() },
        signUp = { SignUp().navigate() }
    )

context(Navigator, Reducer)
fun Login(): Login =
    Login(
        next = { LoginPassword().navigate() },
        back = { back() },
        name = "name",
        onChange = {
            reducer<Login> { copy(name = it) }
        }
    )

context(Navigator, Reducer)
fun SignUp(): SignUp =
    SignUp(
        next = { SignupPassword() },
        back = {},
        name = "name"
    )

context(Navigator, Reducer)
fun LoginPassword(): Password =
    Password.Login(
        password = "",
        next = { Dashboard().navigate(clearAll = true) },
        back = { back() },
    )

context(Navigator, Reducer)
fun SignupPassword(): Password =
    Password.Signup(
        password = "",
        next = { Dashboard().navigate() },
        back = { back() },
    )

data class Screen1Detail(
    val close: () -> Unit,
    val setCounter: (Int) -> Unit,
    override val route: String = "Screen1Detail"
) : Screen

context(Reducer, Navigator)
fun Screen1Detail(): Screen1Detail =
    Screen1Detail(
        close = { back() },
        setCounter = { reducer { copy(counter = it) } }
    )

fun main() {
    val state = MutableStateFlow(App(screen = Start))
    val navigator = Navigator {
        state.value = state.value.copy(screen = this)
    }
    val provider = object : Reducer {
        override fun App.reduce() {
            state.value = this
        }

        override val state: App
            get() = state.value

    }

    with(navigator, provider) {
        state.value.screen.let {
            if (it is Start) Splash().navigate()
        }

        state<Splash> {
            next()
        }

        state<Home> {
            login()
        }
        state<Login> {
            next()
        }
        state<Password> {
            back()
        }
    }
}

inline operator fun <reified S : Screen> StateFlow<App>.invoke(f: S.() -> Unit): Unit =
    when (val screen = value.screen) {
        is S -> f(screen)
        else -> Unit
    }

suspend inline fun <reified A : Screen> StateFlow<App>.assertScreen(
    crossinline f: suspend ScreenContext<A>.() -> Unit
) {
    object : ScreenContext<A> {
        override val screen: A
            get() = value.screen.let {
                assert(it.instanceOf(A::class)) { "${it::class} is not ${A::class}" }
                return it as A
            }

        override val screenFlow: Flow<A>
            get() = map {
                it.screen.let {
                    assert(it.instanceOf(A::class)) { "${it::class} is not ${A::class}" }
                    it as A
                }
            }
    }.let { f(it) }
}

inline fun <reified S : Screen> Screen.assert(f: S.() -> Unit) {
    assert(this.instanceOf(S::class)) { "${this::class} is not ${S::class}" }
    f(this as S)
}

interface ScreenContext<A : Screen> {
    val screen: A
    val screenFlow: Flow<A>
}