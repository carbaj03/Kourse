package com.example.myapplication.navigation

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


context(Reducer)
fun SubTabOne() = SubTab.One(
        counter = 0,
        setCounter = {
            reducer<Dashboard, Tab.Four, SubTab.One> { copy(counter = it) }
            reducer<Dashboard> { copy(counter = counter + it) }
        }
    )

context(Reducer)
fun SubTabTwo() = SubTab.Two(
    counter = 0,
    setCounter = { reducer<Dashboard, Tab.Four, SubTab.Two> { copy(counter = it) } }
)

context(Reducer)
fun SubTabThree() = SubTab.Three(
    counter = 0,
    setCounter = { reducer<Dashboard, Tab.Four, SubTab.Three> { copy(counter = it) } }
)

inline operator fun <reified A : SubTab> SubTab.invoke(f: A.() -> A): SubTab =
    when (this) {
        is A -> f(this)
        else -> this
    }