package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import com.example.myapplication.navigation.Tab.*
import com.example.myapplication.with
import io.ktor.util.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map


interface State

data class App(
    val screen: Screen,
    val stack: BackStack = BackStack(emptyList()),
    val finish: () -> Unit,
    val counter: Int = 0,
) : State


@JvmInline
value class BackStack(
    val screens: List<Screen>,
)

context(Reducer)
fun screenToMerge(): List<Screen> {
    if (state.value.stack.screens.isEmpty()) return emptyList()
    return state.value.stack.screens
        .getOrNull(state.value.stack.screens.size - 2)
        ?.let { listOf(it, state.value.screen) }
        ?: emptyList()

}

context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> filterMergeable(): List<A>? =
    screenToMerge()
        .filterIsInstance<A>()
        .let { if (it.size <= 1) return null else it }


context(Reducer, Mergeable<A>)
inline fun <reified A : Screen> reduceStack(): A? =
    filterMergeable()?.reduce { a, b -> a + b }


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
    data class One(
        val counter: Int,
        val setCounter: (Int) -> Unit,
        val back: () -> Unit,
    ) : Tab

    data class Two(
        val toSearch: String,
        val search: () -> Unit,
        val changeSearch: (String) -> Unit,
        val suggestions: List<String>,
        val selectSuggestion: (String) -> Unit,
        val results: List<String>,
        val selectResult: () -> Unit,
        val back: () -> Unit,
    ) : Tab

    data class Three(
        val screen: Tab3Content,
    ) : Tab

    data class Four(
        val subTab: SubTab,
        val tab1: SubTab.One,
        val tab2: SubTab.Two,
        val tab3: SubTab.Three,
        val onSelected: (SubTab) -> Unit
    ) : Tab
}

sealed interface SubTab {
    data class One(
        val counter: Int,
        val setCounter: (Int) -> Unit,
    ) : SubTab

    data class Two(
        val counter: Int,
        val setCounter: (Int) -> Unit,
    ) : SubTab

    data class Three(
        val counter: Int,
        val setCounter: (Int) -> Unit,
    ) : SubTab
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
                copy(currentTab = currentTab<Three> { copy(screen = Tab3Screen2()) })
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
fun Tab1(): One =
    One(
        counter = 0,
        setCounter = { newCounter ->
            reducer<Dashboard> {
                copy(currentTab = currentTab<One> { copy(counter = newCounter) })
            }
        },
        back = { back() }
    )


class SearchRepository {
    suspend fun getSuggestions(hint: String): List<String> = listOf("carbajo").filter { it.contains(hint) }
    suspend fun getResults(search: String): List<String> = listOf("Carbajo Achievements", "Carbajo the great developer").filter { it.contains(search, true) }
}

context(Reducer, Navigator, SearchRepository, CoroutineScope)
fun Tab2(): Two =
    Two(
        toSearch = "",
        changeSearch = { search ->
            launch {
                reducer<Dashboard> {
                    copy(currentTab = currentTab<Two> { copy(toSearch = search, suggestions = getSuggestions(search)) })
                }
            }
        },
        search = {
            launch {
                reducer<Dashboard> {
                    copy(currentTab = currentTab<Two> { copy(results = getResults(toSearch)) })
                }
            }
        },
        results = listOf(),
        selectResult = {
            launch {
                state<Dashboard> {
                    currentTab<Two, String> { tab2.toSearch }?.let { getResults(it) }
                }
            }
        },
        suggestions = listOf(),
        selectSuggestion = {
            reducer<Dashboard> {
                copy(currentTab = currentTab<Two> { copy(toSearch = it) })
            }
        },
        back = {}
    )

context(Reducer, Navigator)
fun Tab3(): Three =
    Three(
        screen = Tab3Screen1(),
    )

context(Reducer, Navigator)
fun Tab4(): Four {
    val one = SubTab.One(
        counter = 0,
        setCounter = {
            reducer<Dashboard> {
                copy(currentTab = currentTab<Four> { copy(subTab = subTab<SubTab.One> { copy(counter = it) }) })
            }
        }
    )
    val two = SubTab.Two(
        counter = 0,
        setCounter = {
            reducer<Dashboard> {
                copy(currentTab = currentTab<Four> { copy(subTab = subTab<SubTab.Two> { copy(counter = it) }) })
            }
        }
    )
    val three = SubTab.Three(
        counter = 0,
        setCounter = {
            reducer<Dashboard> {
                copy(currentTab = currentTab<Four> { copy(subTab = subTab<SubTab.Three> { copy(counter = it) }) })
            }
        }
    )
    return Four(
        onSelected = {
            reducer<Dashboard> {
                copy(currentTab = currentTab<Four> {
                    when (subTab) {
                        is SubTab.One -> if (it is SubTab.One) this else copy(tab1 = subTab, subTab = it)
                        is SubTab.Two -> if (it is SubTab.Two) this else copy(tab2 = subTab, subTab = it)
                        is SubTab.Three -> if (it is SubTab.Three) this else copy(tab3 = subTab, subTab = it)
                    }
                })
            }
        },
        subTab = one,
        tab1 = one,
        tab2 = two,
        tab3 = three,
    )
}


interface TabContext<A : Tab> {
    val tab: A
}

context(ScreenContext<Dashboard>)
inline fun <reified S : Tab> assert(
    crossinline f: TabContext<S>.() -> Unit = {}
) {
    object : TabContext<S> {
        override val tab: S
            get() {
                assert(screen.currentTab.instanceOf(S::class)) { "${screen.currentTab::class} is not ${S::class}" }
                return screen.currentTab as S
            }
    }.let { f(it) }
}

inline operator fun <reified A : Tab> Tab.invoke(f: A.() -> A): Tab =
    when (this) {
        is A -> f(this)
        else -> this
    }

inline operator fun <reified A : Tab, B> Tab.invoke(f: A.() -> B): B? =
    when (this) {
        is A -> f(this)
        else -> null
    }

inline operator fun <reified A : SubTab> SubTab.invoke(f: A.() -> A): SubTab =
    when (this) {
        is A -> f(this)
        else -> this
    }

data class Dashboard(
    val tab1: One,
    val tab2: Two,
    val tab3: Three,
    val tab4: Four,
    val currentTab: Tab,
    val onTabSelected: (Tab) -> Unit,
) : Screen {
    override val route: String
        get() = when (currentTab) {
            is One -> "DashboardTab1"
            is Two -> "DashboardTab2"
            is Three -> when (currentTab.screen) {
                is Tab3Screen1 -> "Tab3Screen1"
                is Tab3Screen2 -> "Tab3Screen2"
            }
            is Four -> when (currentTab.subTab) {
                is SubTab.One -> "SubTab.One"
                is SubTab.Two -> "SubTab.Two"
                is SubTab.Three -> "SubTab.Three"
            }
        }
}

fun interface Mergeable<A : Screen> {
    infix operator fun A.plus(other: A): A
}


context(Navigator, Reducer, SearchRepository, SideEffect)
fun Start() {
    Splash().navigate(addToStack = false)
}

context(Navigator, Reducer, SearchRepository, CoroutineScope)
fun Dashboard(): Dashboard {
    val tab1 = Tab1()
    val tab2 = Tab2()
    val tab3 = Tab3()
    val tab4 = Tab4()
    return Dashboard(
        currentTab = tab1,
        onTabSelected = {
            navigate<Dashboard> {
                when (currentTab) {
                    is One -> if (it is One) this else copy(tab1 = currentTab, currentTab = it)
                    is Two -> if (it is Two) this else copy(tab2 = currentTab, currentTab = it)
                    is Three -> if (it is Three) this else copy(tab3 = currentTab, currentTab = it)
                    is Four -> if (it is Four) this else copy(tab4 = currentTab, currentTab = it)
                }
            }
        },
        tab1 = tab1,
        tab2 = tab2,
        tab3 = tab3,
        tab4 = tab4,
    )
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
        next = { LoginPassword().navigate() },
        back = { launch { back() } },
        name = "name",
        onChange = { reducer<Login> { copy(name = it) } }
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
        next = { Dashboard().navigate(clearAll = true) },
        back = { launch { back() } },
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
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
        setCounter = {
            reducer { copy(counter = it) }
        }
    )


fun main() {
    val scope = CoroutineScope(Job())
    val repo = SearchRepository()

    val store = with(scope, repo) {
        Store(start = { Start() }, finish = {})
    }

    with(store) {
        state<Splash> {
            scope.launch { next() }
        }
        state<Home> {
            login()
        }
        state<Login> {
            scope.launch { next() }
        }
        state<Password> {
            scope.launch { back() }
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