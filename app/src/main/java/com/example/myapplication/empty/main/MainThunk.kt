package com.example.myapplication.empty.main

import arrow.core.continuations.either
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.empty.book.BooksRepository
import com.example.myapplication.empty.podcast.PodcastRepository
import com.example.myapplication.empty.video.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MainState(
    val title: String = "",
    val items: List<String> = emptyList(),
    val tabs: List<Tab> = Tab.values().toList(),
    val selectedTab: Tab = Tab.Home,
    val drawer: Boolean
)

enum class Tab(val title: String) {
    Home("home"),
    Podcast("Podcast"),
    Blogs("Blogs"),
    Books("Books"),
    Videos("Videos"),
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
    val bookRepository: BooksRepository,
    val videoRepository: VideoRepository,
    val podcastRepository: PodcastRepository,
    withScope: WithScope,
) : MainThunk, WithScope by withScope {
    val s = MutableStateFlow(initialState)
    
    override fun dispatch(action: MainAction) {
        when (action) {
            MainAction.Load -> {
                launchMain {
                    either {
                        val books = asyncIo { bookRepository.all().bind() }
                        val podcast = asyncIo { podcastRepository.all().bind() }
                        val videos = asyncIo { videoRepository.all().bind() }
                        
                        val items = books.await().value.map { it.title }.plus(podcast.await().value.map { it.title }).plus(videos.await().value.map { it.url })

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
                s.value = s.value.copy(drawer = true)
            }
        }
    }
    
    override val state: StateFlow<MainState> = s
}