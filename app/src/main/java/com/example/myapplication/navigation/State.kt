package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import arrow.optics.optics
import com.example.myapplication.navigation.Tab.*
import com.example.myapplication.with
import io.ktor.util.reflect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


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
) : Screen {

    context(Reducer)
    fun reduce(
        back: () -> Unit = this.back,
        next: () -> Unit = this.next,
        name: String = this.name,
        onChange: (String) -> Unit = this.onChange
    ) =
        reducer<Login> { copy(back = back, next = next, name = name, onChange = onChange, route = route) }
}

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
        val isLoading: Boolean,
        val load: () -> Unit
    ) : Tab {
        companion object {
            context(Reducer)
            inline fun <reified A> reducer(f: Two.() -> Two): Unit where A : Screen, A : WithTab<A> =
                reducer<Two, A> { f() }
        }
    }

    data class Three(
        val screen: Tab3Content,
    ) : Tab

    data class Four(
        val tab1: SubTab.One,
        val tab2: SubTab.Two,
        val tab3: SubTab.Three,
        override val currentSubTab: SubTab,
        override val onSubTabSelected: (SubTab) -> Unit
    ) : Tab, WithSubTab<Four> {
        override fun with(current: SubTab): Four =
            copy(currentSubTab = current)
    }
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

sealed interface Error {
    data object Default : Error
}

fun interface Raise<E> {
    fun raise(e: E): Nothing
}


class SearchRepository {
    context(Raise<Error>)
    suspend fun getSuggestions(hint: String): List<String> {
        delay(1000)
        return listOf("carbajo").filter { it.contains(hint) }
    }

    context(Raise<Error>)
    suspend fun getResults(search: String): List<String> {
        delay(1000)
        return listOf("Carbajo Achievements", "Carbajo the great developer").filter { it.contains(search, true) }
    }
}

context(Raise<Error>)
fun Int.sum() : Int = this + 2

context(Raise<Error>)
fun Int.multiply(num : Int) : Int = this * num

context(Raise<Error>)
fun Int.divide() : Int = this / 2

context(Raise<Error>)
fun String.count() : Int = this.length


val sum : context(Raise<Error>) (Int) -> Int =  { x -> x + 2}



context(Reducer, Navigator)
fun Tab1(): One =
    One(
        counter = 0,
        setCounter = { newCounter ->
            reducer<One, Dashboard> { copy(counter = newCounter) }
        },
        back = { back() }
    )

context(Reducer, Navigator, SearchRepository, SideEffect)
fun Tab2(): Two {
    var j : Job? = null
    return Two(
        toSearch = "",
        changeSearch = { search ->
            j?.cancel()
            reducer<Two, Dashboard> { copy(toSearch = search) }
            j = launch {
                raised {
                    reducer<Two, Dashboard> { copy(suggestions = getSuggestions(search)) }
                }
//                Two.reducer<Dashboard> { copy(toSearch = search, suggestions = getSuggestions(search)) }
            }
        },
        search = {
            launch {
                raised {
                    reducer<Two, Dashboard> { copy(results = getResults(toSearch)) }
                }
            }
        },
        results = listOf(),
        selectResult = {
            launch {
                raised {
                    state<Dashboard, Two> { getResults(tab2.toSearch) }
                }
            }
        },
        suggestions = listOf(),
        selectSuggestion = { reducer<Two, Dashboard> { copy(toSearch = it) } },
        back = {},
        isLoading = true,
        load = {
            launch {
                delay(2000)
                reducer<Two, Dashboard> { copy(isLoading = false) }
            }
        }
    )
}

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
            reducer<Dashboard, Four, SubTab.One> { copy(counter = it) }
            reducer<Dashboard> { copy(counter = counter + it) }
        }
    )
    val two = SubTab.Two(
        counter = 0,
        setCounter = { reducer<Dashboard, Four, SubTab.Two> { copy(counter = it) } }
    )
    val three = SubTab.Three(
        counter = 0,
        setCounter = { reducer<Dashboard, Four, SubTab.Three> { copy(counter = it) } }
    )
    return Four(
        onSubTabSelected = {
            reducer<Four, Dashboard> {
                when (currentSubTab) {
                    is SubTab.One -> if (it is SubTab.One) this else copy(tab1 = currentSubTab, currentSubTab = it)
                    is SubTab.Two -> if (it is SubTab.Two) this else copy(tab2 = currentSubTab, currentSubTab = it)
                    is SubTab.Three -> if (it is SubTab.Three) this else copy(tab3 = currentSubTab, currentSubTab = it)
                }
            }
        },
        currentSubTab = one,
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


interface WithTab<A> {
    val currentTab: Tab
    val onTabSelected: (Tab) -> Unit

    fun with(current: Tab): A
}

interface WithSubTab<A> {
    val currentSubTab: SubTab
    val onSubTabSelected: (SubTab) -> Unit

    fun with(current: SubTab): A
}

data class Dashboard(
    val tab1: One,
    val tab2: Two,
    val tab3: Three,
    val tab4: Four,
    val counter: Int,
    override val currentTab: Tab,
    override val onTabSelected: (Tab) -> Unit
) : Screen, WithTab<Dashboard> {
    override val route: String
        get() = when (currentTab) {
            is One -> "DashboardTab1"
            is Two -> "DashboardTab2"
            is Three -> when (currentTab.screen) {
                is Tab3Screen1 -> "Tab3Screen1"
                is Tab3Screen2 -> "Tab3Screen2"
            }
            is Four -> when (currentTab.currentSubTab) {
                is SubTab.One -> "SubTab.One"
                is SubTab.Two -> "SubTab.Two"
                is SubTab.Three -> "SubTab.Three"
            }
        }

    override fun with(currentTab: Tab): Dashboard =
        copy(currentTab = currentTab)
}

fun interface Mergeable<A : Screen> {
    infix operator fun A.plus(other: A): A
}


context(Navigator, Reducer, SearchRepository, SideEffect)
fun Start() {
    Splash().navigate(addToStack = false)
}

context(Navigator, Reducer, SearchRepository, SideEffect)
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
        counter = 0,
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
        next = {
            state<Login> { if (name == "name") LoginPassword().navigate() }
        },
        back = { back() },
        name = "",
        onChange = {
            reducer<Login> { copy(name = it) }
//            reducerM<Login, LoginMutable> { name = it }
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
        next = { Dashboard().navigate(clearAll = true) },
        back = { back() },
    )

context(Navigator, Reducer, SearchRepository, SideEffect)
fun SignupPassword(): Password =
    Password.Signup(
        password = "",
        next = { Dashboard().navigate() },
        back = { back() },
    )


fun <T> CoroutineScope.launchMolecule(
    body: @Composable () -> T,
): StateFlow<T> {
    var flow: MutableStateFlow<T>? = null

    launchMolecule(
        emitter = { value ->
            val outputFlow = flow
            if (outputFlow != null) {
                outputFlow.value = value
            } else {
                flow = MutableStateFlow(value)
            }
        },
        body = body,
    )

    return flow!!
}

fun main() {
    val scope = CoroutineScope(Job())
    val repo = SearchRepository()

    val store = with(scope, repo) {
        Store(start = { Start() }, finish = {})
    }
    val networkResult: NetworkResult = HttpError("boom!")
    val f: (String) -> String = String::toUpperCase
    NetworkResult.networkError.httpError.message.modify(networkResult, f)

    with(store) {
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

    raised {
        2.sum().sum().divide().multiply(2).toString().count()
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


@optics
sealed class NetworkResult {
    companion object
}

@optics
data class Success(val content: String) : NetworkResult() {
    companion object
}

@optics
sealed class NetworkError : NetworkResult() {
    companion object
}

@optics
data class HttpError(val message: String) : NetworkError() {
    companion object
}

object TimeoutError : NetworkError()