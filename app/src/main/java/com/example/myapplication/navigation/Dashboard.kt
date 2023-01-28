package com.example.myapplication.navigation

import com.example.myapplication.navigation.Tab.*
import com.example.myapplication.with
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

data class Dashboard(
    val tab1: One,
    val tab2: Two,
    val tab3: Three,
    val tab4: Four,
    val counters: Map<String, Int>,
    val load: () -> Unit,
    override val currentTab: Tab,
    override val onTabSelected: (Tab) -> Unit,
    override val stop: () -> Unit,
) : Screen, WithTab<Dashboard>, Stoppable {
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

    override fun with(current: Tab): Dashboard =
        copy(currentTab = current)

    companion object {
        fun empty() =
            Dashboard(One.empty(), Two.empty(), Three.empty(), Four.empty(), mapOf(), {}, One.empty(), {}, {})
    }
}

context(Reducer, SideEffect)
inline fun <reified B> create(initialScreen: B): ScreenReducer<B> where B : Screen, B : WithTab<B> =
    object : ScreenReducer<B>, Reducer by this@Reducer {
        override val screen: StateFlow<B> = state.map { it.screen }.distinctUntilChanged().filterIsInstance<B>().stateIn(this@SideEffect, SharingStarted.Lazily, initialScreen)
    }

context(Reducer, SideEffect)
inline fun <reified B, reified A : Tab> create(initialScreen: B, initialTab: A): TabReducer<B, A> where B : Screen, B : WithTab<B> =
    object : TabReducer<B, A>, ScreenReducer<B> by create(initialScreen) {
        override val tab: StateFlow<A> = screen.map { it.currentTab }.distinctUntilChanged().filterIsInstance<A>().stateIn(this@SideEffect, SharingStarted.Lazily, initialTab)
    }


context(Navigator, Reducer, SearchRepository, SideEffect, Counter)
fun Dashboard(): Dashboard {
    val tab1 = withScope(name = "Tab1") { with(Counter(), create(Dashboard.empty(), One.empty())) { Tab1() } }
    val tab2 = withScope(name = "Tab2") { with(Counter(), create(Dashboard.empty(), Two.empty())) { Tab2() } }
    val tab3 = withScope(name = "Tab3") { with(Counter()) { Tab3() } }
    val tab4 = withScope(name = "Tab4") { with(Counter()) { Tab4() } }
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
        counters = emptyMap(),
        load = {
            launch {
                count.collect {
                    reducer<Dashboard> { copy(counters = counters.toMutableMap().apply { set("Dash", it) }) }
                }
            }
        },
        stop = {
            cancel()
        }
    )
}


context(SideEffect)
inline fun <A> withScope(name: String = "withScope", f: context(SideEffect)() -> A): A =
    (coroutineContext + SupervisorJob() + CoroutineName(name)).let {
        f(object : SideEffect {
            override val coroutineContext: CoroutineContext = it
        })
    }

