package com.example.myapplication

import android.app.Application
import arrow.core.Either
import arrow.core.right
import com.example.myapplication.redux.configureStore
import com.example.myapplication.redux.createSlice
import com.example.myapplication.redux.types.ActionState
import com.example.myapplication.todo.*
import com.fintonic.domain.commons.redux.types.CombineState
import com.fintonic.domain.commons.redux.types.Store
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class MyApplication : Application() {
    companion object {

        val graph = AppGraphAndroid(
            store = configureStore(
                createSlice(initialState = BooksState()) { a: ActionState<BooksState> -> a(this) },
                createSlice(initialState = PodcastsState()) { a: ActionState<PodcastsState> -> a(this) },
                createSlice(initialState = ToolbarState("App")) { a: ActionState<ToolbarState> -> a(this) },
                createSlice(initialState = BottomState(BottomItem.values().toList(), BottomItem.Home)) { a: ActionState<BottomState> -> a(this) },
            ),
            podcast = object : PodcastRepository {
                override suspend fun getAll(): Either<DomainError, Podcasts> {
                    delay(1.seconds)
                    return Podcasts(listOf(Podcast("sadf", "adsfasf"))).right()
                }

            }
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}

data class AppGraphAndroid(
    override val store: Store<CombineState>,
    override val podcast: PodcastRepository,
) : AppGraph


interface AppGraph : RepositoryGraph {
    val store: Store<CombineState>
}

interface RepositoryGraph {
    val podcast: PodcastRepository
}

fun AppGraph(
    store: Store<CombineState> = MyApplication.graph.store
): AppGraph =
    object : AppGraph {
        override val store: Store<CombineState> = store
        override val podcast: PodcastRepository = object : PodcastRepository {
            override suspend fun getAll(): Either<DomainError, Podcasts> {
                TODO("Not yet implemented")
            }
        }
    }