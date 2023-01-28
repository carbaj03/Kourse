package com.example.myapplication.navigation

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed interface Tab {
    data class One(
        val counter: Int,
        val setCounter: (Int) -> Unit,
        val back: () -> Unit,
        val load: () -> Unit,
        override val stop: () -> Unit,
    ) : Tab, Stoppable {
        companion object {
            fun empty() =
                One(0, {}, {}, {}, {})
        }
    }

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
        val load: () -> Unit,
        override val stop: () -> Unit,
    ) : Tab, Stoppable {
        companion object {
            context(Reducer)
            inline operator fun <A> invoke(f: Two.() -> A): A = f(get())

            fun empty() = Two("", {}, {}, listOf(), {}, listOf(), {}, {}, false, {}, {})
        }
    }

    data class Three(
        val screen: Tab3Content,
        val load: () -> Unit,
        override val stop: () -> Unit,
    ) : Tab, Stoppable {
        companion object {
            fun empty() =
                Three(Tab3Content.empty(), {}, {})
        }
    }

    data class Four(
        val tab1: SubTab.One,
        val tab2: SubTab.Two,
        val tab3: SubTab.Three,
        val load: () -> Unit,
        override val currentSubTab: SubTab,
        override val onSubTabSelected: (SubTab) -> Unit,
        override val stop: () -> Unit,
    ) : Tab, WithSubTab<Four>, Stoppable {
        override fun with(current: SubTab): Four =
            copy(currentSubTab = current)

        companion object {
            fun empty() =
                Four(SubTab.One.empty(), SubTab.Two.empty(), SubTab.Three.empty(), {}, SubTab.One.empty(), {}, {})
        }
    }
}


context(TabReducer<Dashboard, Tab.One>, Navigator, SideEffect, Counter)
fun Tab1(): Tab.One =
    Tab.One(
        counter = 0,
        setCounter = { newCounter ->
            reducerTab { copy(counter = newCounter) }
        },
        back = { back() },
        load = {
            launch {
                count.collect {
                    reducerScreen { copy(counters = counters.toMutableMap().apply { set(route, it) }) }
                }
            }
        },
        stop = {
            cancel()
        }
    )

context(TabReducer<Dashboard, Tab.Two>, Navigator, SearchRepository, SideEffect, Counter)
fun Tab2(): Tab.Two {
    var job: Job? = null
    return Tab.Two(
        toSearch = "",
        changeSearch = { search ->
            job?.cancel()
            reducerTab { copy(toSearch = search) }
            job = launch {
                raised {
                    val s = getSuggestions(search)
                    reducerTab { copy(suggestions = s) }
//                    reducer<Tab.Two, Dashboard> { copy(suggestions = getSuggestions(search)) }
                }
            }
        },
        search = {
            launch {
                raised {
                    val r = getResults(tab.value.toSearch)
                    reducerTab { copy(results = r) }
//                    reducer<Tab.Two, Dashboard> { copy(results = r) }
                }
            }
        },
        results = listOf(),
        selectResult = { Detail().navigate() },
        suggestions = listOf(),
        selectSuggestion = { reducerTab { copy(toSearch = it) } },
        back = {},
        isLoading = true,
        load = {
            launch {
                count.collect {
                    reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set(route, it) }) }
                }
            }
            launch {
                delay(2000)
                reducerTab { copy(isLoading = false) }
            }
        },
        stop = {
            cancel()
        }
    )
}

context(Reducer)
fun <A> get(): A =
    when (val s = state.value.screen) {
        is Dashboard ->
            when (val tab = s.currentTab) {
                is Tab.Four -> TODO()
                is Tab.One -> tab as A
                is Tab.Three -> TODO()
                is Tab.Two -> tab as A
            }

        is Detail -> TODO()
        is Home -> TODO()
        is Login -> TODO()
        is Password.Login -> TODO()
        is Password.Signup -> TODO()
        is SignUp -> TODO()
        is Splash -> TODO()
        Start -> TODO()
    }

context(Reducer, Navigator, Counter, SideEffect)
fun Tab3(): Tab.Three =
    Tab.Three(
        screen = Tab3Screen1(),
        load = {
            launch {
                count.collect {
                    reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set(route, it) }) }
                }
            }
        },
        stop = {
            cancel()
        }
    )

context(Reducer, Navigator, SideEffect, Counter)
fun Tab4(): Tab.Four {
    val one = withScope("SubTabOne") { SubTabOne() }
    val two = SubTabTwo()
    val three = SubTabThree()

    return Tab.Four(
        onSubTabSelected = {
            reducer<Tab.Four, Dashboard> {
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
        load = {
            launch {
                count.collect {
                    reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set(route, it) }) }
                }
            }
        },
        stop = {
            cancel()
        }
    )
}


inline operator fun <reified A : Tab> Tab.invoke(f: A.() -> A): Tab =
    when (this) {
        is A -> f(this)
        else -> throw Exception(this.toString())
    }

inline fun <reified A : Tab, B> Tab.pointer(f: A.() -> B): B =
    when (this) {
        is A -> f(this)
        else -> throw Exception(this.toString())
    }
