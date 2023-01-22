package com.example.myapplication.navigation

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    ) : Tab

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


context(Reducer, Navigator)
fun Tab1(): Tab.One =
    Tab.One(
        counter = 0,
        setCounter = { newCounter ->
            reducer<Tab.One, Dashboard> { copy(counter = newCounter) }
        },
        back = { back() }
    )

context(Reducer, Navigator, SearchRepository, SideEffect)
fun Tab2(): Tab.Two =
    Tab.Two(
        toSearch = "",
        changeSearch = { search ->
            reducer<Tab.Two, Dashboard> { copy(toSearch = search) }
            launch {
                raised {
                    reducer<Tab.Two, Dashboard> { copy(suggestions = getSuggestions(search)) }
                }
            }
        },
        search = {
            launch {
                raised {
                    reducer<Tab.Two, Dashboard> { copy(results = getResults(toSearch)) }
                }
            }
        },
        results = listOf(),
        selectResult = {
            launch {
                raised {
                    state<Dashboard, Tab.Two> { getResults(tab2.toSearch) }
                }
            }
        },
        suggestions = listOf(),
        selectSuggestion = { reducer<Tab.Two, Dashboard> { copy(toSearch = it) } },
        back = {},
        isLoading = true,
        load = {
            launch {
                delay(2000)
                reducer<Tab.Two, Dashboard> { copy(isLoading = false) }
            }
        }
    )

context(Reducer, Navigator)
fun Tab3(): Tab.Three =
    Tab.Three(
        screen = Tab3Screen1(),
    )

context(Reducer, Navigator)
fun Tab4(): Tab.Four {
    val one = SubTabOne()
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
    )
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
