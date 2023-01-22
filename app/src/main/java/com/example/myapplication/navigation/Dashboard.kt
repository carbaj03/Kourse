package com.example.myapplication.navigation

import com.example.myapplication.navigation.Tab.*

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

    override fun with(current: Tab): Dashboard =
        copy(currentTab = current)
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
