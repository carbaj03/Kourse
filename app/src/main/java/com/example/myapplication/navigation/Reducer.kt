package com.example.myapplication.navigation

import kotlinx.coroutines.flow.StateFlow

interface Reducer {
    fun App.reduce()
    val state: StateFlow<App>
}

context(Reducer)
@JvmName("reducerApp")
inline fun reducer(f: App.() -> App): Unit =
    f(state.value).reduce()

context(Reducer)
@JvmName("reducerScreen")
inline fun <reified S : Screen> reducer(f: S.() -> S): Unit =
    when (val screen = state.value.screen) {
        is S -> state.value.copy(screen = f(screen)).reduce()
        else -> Unit
    }

context(Reducer)
inline fun <reified A : Screen, B> reducerM(f: B.() -> Unit): Unit {
    when (val screen = state.value.screen) {
        is A -> {
            when (screen) {
                is Dashboard -> TODO()
                is Home -> TODO()
                is Login -> {
                    object : Mutable<Login, LoginMutable> {
                        override fun Login.mutate(): LoginMutable =
                            LoginMutable(back, next, name, onChange)

                        override fun LoginMutable.revert(): Login =
                            Login(back, next, name, onChange)

                    }.run {
                        val b = screen.mutate()
                        f(b as B)
                        state.value.copy(screen = b.revert()).reduce()
                    }
                }
                is Password.Login -> TODO()
                is Password.Signup -> TODO()
                is SignUp -> TODO()
                is Splash -> TODO()
                Start -> TODO()
            }

        }
        else -> Unit
    }
}

interface Mutable<A, B> {
    fun A.mutate(): B
    fun B.revert(): A
}

context(Reducer)
inline fun <reified A : Tab, reified B> reducer(f: A.() -> A): Unit where B : Screen, B : WithTab<B> =
    reducer<B> { with(current = currentTab<A> { f() }) }

context(Reducer)
@JvmName("reducerSubTab")
inline fun <reified B, reified C, reified A> reducer(f: A.() -> A): Unit where B : Screen, B : WithTab<B>, C : WithSubTab<C>, C : Tab, A : SubTab =
    reducer<B> { with(current = currentTab<C> { with(current = currentSubTab<A> { f() }) }) }


context(Reducer)
inline fun <reified S : Screen> state(f: S.() -> Unit): Unit =
    when (val screen = state.value.screen) {
        is S -> f(screen)
        else -> Unit
    }

context(Reducer)
@JvmName("stateSubTab")
inline fun <reified B, reified C, reified A> state(f: A.() -> A): Unit where B : Screen, B : WithTab<B>, C : WithSubTab<C>, C : Tab, A : SubTab =
    state<B> { with(current = currentTab<C> { with(current = currentSubTab<A> { f() }) }) }

context(Reducer)
@JvmName("stateTab")
inline fun <reified B, reified C> state(f: B.() -> Unit): Unit where B : Screen, B : WithTab<B>, C : Tab =
    state<B> { with(current = currentTab<C> { f(); this }) }
