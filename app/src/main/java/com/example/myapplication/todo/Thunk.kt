package com.example.myapplication.todo

import arrow.core.Either
import arrow.core.left
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.asynchrony.dispatchAction
import com.example.myapplication.asynchrony.slice
import com.example.myapplication.redux.select
import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.Error
import com.fintonic.domain.commons.redux.types.CombineState
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.commons.redux.types.Store
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.seconds


interface ThunkScreen : ThunkBooks, ThunkPodcast, ThunkToolbar

interface T<S : State> {
    fun load()
    fun increase()
    fun decrease()

    val state: StateFlow<S>
}

interface ThunkHome : ThunkToolbar, ThunkBottom {
    //    fun podcast()
//    fun books()
//    fun videos()
//    fun blogs()
//    fun load()
    operator fun HomeAction.invoke()

    val books: StateFlow<HomeState>
}

interface ThunkBooks {
    fun load()
    fun increase()
    fun decrease()

    val books: StateFlow<BooksState>
}

interface ThunkPodcast {
    fun loadPodcast()
    fun increasePodcast()
    fun decreasePodcast()

    val podcast: StateFlow<PodcastsState>
}

//interface ThunkToolbar {
//    fun onBack()
//    fun onClose()
//    fun loadToolbar()
//
//    val toolbar: StateFlow<ToolbarState>
//}

interface NavigationAction : Action
interface TrackingAction : Action

sealed interface ToolbarAction {
    object OnBack : ToolbarAction, NavigationAction
    object OnClose : ToolbarAction, NavigationAction
    object Load : ToolbarAction, Action
}

interface ThunkToolbar {
    fun action(action: ToolbarAction)

    val toolbar: StateFlow<ToolbarState>
}

sealed interface BottomAction {
    data class OnItemClick(val item: BottomItem) : BottomAction, NavigationAction
    object Load : BottomAction, Action
}

interface ThunkBottom {
    fun action(action: BottomAction)

    val bottom: StateFlow<BottomState>
}

sealed interface DomainError : Error {
    object Default : DomainError
}

interface PodcastRepository {
    suspend fun getAll(): Either<DomainError, Podcasts>
}

fun interface ToolbarNavigator {
    suspend operator fun invoke(navigationAction: NavigationAction)
}

fun interface BottomNavigator {
    suspend operator fun invoke(navigationAction: NavigationAction)
}


fun ThunkToolbar(
    withScope: WithScope,
    store: Store<CombineState>,
    navigator: ToolbarNavigator
): ThunkToolbar =
    object : ThunkToolbar,
        WithScope by withScope,
        Store<CombineState> by store {

        //        override fun loadToolbar() =
//            dispatchAction {
//                slice<ToolbarState> {
//                    copy(title = "Hello")
//                }
//            }
//
//        override fun onBack() =
//            dispatchAction {
//                Main {
//                    navController.popBackStack()
//                }
//            }
//
//        override fun onClose() =
//            dispatchAction {
//
//                slice<ToolbarState> {
//                    copy(title = "Hello")
//                }
//            }

        override fun action(action: ToolbarAction): Unit =
            dispatchAction {
                when (action) {
                    is ToolbarAction.Load -> slice<ToolbarState> {
                        copy(title = "Hello")
                    }

                    is ToolbarAction.OnBack -> navigator(action)
                    is ToolbarAction.OnClose -> navigator(action)
                }
            }

        override val toolbar: StateFlow<ToolbarState> by store
    }

//fun ThunkToolbar2(
//    withScope: WithScope = WithScope(),
//    store: Store<CombineState>
//): ThunkToolbar =
//    object : ThunkToolbar,
//        WithScope by withScope,
//        Store<CombineState> by store {
//
//        override fun loadToolbar() =
//            dispatchAction {
//                slice<ToolbarState> {
//                    copy(title = "Hello2")
//                }
//            }
//
//        override fun onBack() =
//            dispatchAction {
//                slice<ToolbarState> {
//                    copy(title = "back")
//                }
//            }
//
//        override fun onClose() =
//            dispatchAction {
//                slice<ToolbarState> {
//                    copy(title = "Hello2")
//                }
//            }
//
//        override val toolbar: StateFlow<ToolbarState> by store
//    }

sealed interface HomeAction {
    object Podcast : HomeAction, NavigationAction
    object Books : HomeAction, NavigationAction
    object Videos : HomeAction, NavigationAction, TrackingAction
    object Blogs : HomeAction, NavigationAction
    object Load : HomeAction
    data class Bottom(val item: BottomItem) : HomeAction, NavigationAction
}

fun interface HomeNavigator {
    suspend operator fun invoke(action: NavigationAction)
}

fun interface HomeTracker {
    suspend operator fun invoke(action: TrackingAction)
}

fun ThunkHome(
    withScope: WithScope = WithScope(),
    store: Store<CombineState>,
    homeNavigator: HomeNavigator,
    bottomNavigator: BottomNavigator,
    tracker: HomeTracker
): ThunkHome =
    object : ThunkHome,
        WithScope by withScope,
        Store<CombineState> by store {

        override fun HomeAction.invoke(): Unit =
            dispatchAction {
                when (val action = this@invoke) {
                    is HomeAction.Blogs -> homeNavigator(action)
                    is HomeAction.Books -> homeNavigator(action)
                    is HomeAction.Podcast -> homeNavigator(action)
                    is HomeAction.Videos -> {
                        tracker(action)
                        homeNavigator(action)
                    }

                    is HomeAction.Load -> {}
                    is HomeAction.Bottom -> homeNavigator(action)
                }
            }

        override fun action(action: ToolbarAction): Unit =
            dispatchAction {
                when (action) {
                    is ToolbarAction.Load -> slice<ToolbarState> {
                        copy(title = "Hello")
                    }

                    is ToolbarAction.OnBack -> {}
                    is ToolbarAction.OnClose -> {}
                }
            }

        override fun action(action: BottomAction): Unit =
            dispatchAction {
                when (action) {
                    BottomAction.Load -> slice<BottomState> {
                        copy(list = emptyList())
                    }

                    is BottomAction.OnItemClick -> {
                        bottomNavigator(action)
                        slice<BottomState> {
                            copy(selected = action.item)
                        }
                    }
                }
            }

        override val bottom: StateFlow<BottomState> by store
        override val books: StateFlow<HomeState> by store
        override val toolbar: StateFlow<ToolbarState> by store
    }

fun ThunkBooks(
    withScope: WithScope = WithScope(),
    store: Store<CombineState>
): ThunkBooks =
    object : ThunkBooks,
        WithScope by withScope,
        Store<CombineState> by store {

        override fun load(): Unit =
            dispatchAction {
                repeat(10) {
                    slice<BooksState> { copy(books = books) }
                    delay(2.seconds)
                }
            }

        override fun increase(): Unit =
            dispatchAction {
                slice<BooksState> { copy(isLoading = true) }
                delay(1000)
                slice<BooksState> {
                    copy(
                        books = Books(books.value.plus(Book(BookId(books.value.size), "book${books.value.size}"))),
                        isLoading = false
                    )
                }
            }

        override fun decrease(): Unit =
            dispatchAction {
                slice<PodcastsState> { copy(isLoading = true) }
                delay(1000)
                slice<PodcastsState> {
                    copy(
                        podcast = Podcasts(
                            podcast.value.plus(
                                Podcast(
                                    id = PodcastId(podcast.value.size),
                                    title = "podcast${podcast.value.size}",
                                    url = ""
                                )
                            )
                        ),
                        isLoading = false
                    )
                }
            }

        override val books: StateFlow<BooksState> by store
    }


context(WithScope)
inline operator fun <reified T : State> Store<CombineState>.getValue(
    thisObj: Any?,
    property: KProperty<*>
): StateFlow<T> =
    state
        .map { it.select<T>() ?: throw Exception("Add the slice") }
        .distinctUntilChanged()
        .stateIn(
            scope = this@WithScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = state.value.select() ?: throw Exception("Add the slice")
        )

fun ThunkPodcast(
    withScope: WithScope,
    store: Store<CombineState>,
    repository: PodcastRepository,
): ThunkPodcast =
    object : ThunkPodcast,
        WithScope by withScope,
        Store<CombineState> by store {

        override fun loadPodcast(): Unit =
            dispatchAction {
                repository.getAll().bind()
//                repeat(10) {
//                    slice<PodcastsState> { copy(podcast = podcast) }
//                    delay(2.seconds)
//                }
            }

        override fun increasePodcast(): Unit =
            dispatchAction {
                slice<BooksState> { copy(isLoading = true) }
                delay(1000)
                slice<BooksState> {
                    copy(
                        books = Books(books.value.plus(Book(BookId(books.value.size), "book${books.value.size}"))),
                        isLoading = false
                    )
                }
            }

        override fun decreasePodcast(): Unit =
            dispatchAction {
                slice<PodcastsState> { copy(isLoading = true) }
                delay(1000)
                slice<PodcastsState> {
                    copy(
                        podcast = Podcasts(
                            podcast.value.plus(
                                Podcast(
                                    id = PodcastId(podcast.value.size),
                                    title = "podcast${podcast.value.size}",
                                    url = ""
                                )
                            )
                        ),
                        isLoading = false
                    )
                }
            }

        override val podcast: StateFlow<PodcastsState> by store
    }

sealed interface ApiError : Error {
    object Default : ApiError
}

sealed interface LocalError :  Error {
    object Default : LocalError

}

fun fromApi(): Either<ApiError, String> = ApiError.Default.left()
fun fromLocal(): Either<LocalError, String> = LocalError.Default.left()