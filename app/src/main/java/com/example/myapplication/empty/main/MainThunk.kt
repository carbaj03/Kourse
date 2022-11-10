package com.example.myapplication.empty.main

import arrow.core.continuations.either
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.empty.Repository
import com.example.myapplication.empty.book.BooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MainState(
    val title: String = "",
    val items: List<String> = emptyList(),
    val tabs: List<Tab> = Tab.values().toList(),
    val selectedTab: Tab = Tab.Home
)

enum class Tab(val title: String) {
    Home("home"),
    Podcast("podcast"),
    Blogs("blogs"),
    Books("books")
}

sealed interface MainAction {
    object Load : MainAction
    data class ChangeTab(val tab: Tab) : MainAction
    object Account : MainAction
}

interface MainThunk {
    fun dispatch(action: MainAction)
    val state: StateFlow<MainState>
}

class MainThunkAndroid(
    initialState: MainState,
    repository: Repository,
    val booksRepository: BooksRepository,
    withScope: WithScope,
) : MainThunk, Repository by repository, WithScope by withScope {
    val s = MutableStateFlow(initialState)

    override fun dispatch(action: MainAction) {
        when (action) {
            MainAction.Load -> {
                launchMain {
                    either {
                        val books = asyncIo { booksRepository.all().bind() }
                        val podcast = asyncIo { podcast().bind() }
                        val blogs = asyncIo { blogs().bind() }

                        val items = books.await().value.map { it.title }.plus(podcast.await().value.map { it.title }.plus(blogs.await().value.map { it.author.name }))

//                        books().zip(podcast(), blogs()) { books, podcast, blogs ->
//                            books.value.map { it.title }.plus(podcast.value.map { it.title }.plus(blogs.value.map { it.author }))
//                        }

                        s.value = s.value.copy(
                            title = "App",
                            items = items
                        )
                    }
                }
            }

            is MainAction.ChangeTab -> {
                s.value = s.value.copy(
                    selectedTab = action.tab
                )
            }

            MainAction.Account -> {
            }
        }
    }

    override val state: StateFlow<MainState> = s
}