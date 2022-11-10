package com.example.myapplication.empty

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.empty.blog.BlogsScreen
import com.example.myapplication.empty.book.*
import com.example.myapplication.empty.home.HomeState
import com.example.myapplication.empty.home.HomeThunk
import com.example.myapplication.empty.home.HomeThunkAndroid
import com.example.myapplication.empty.main.*
import com.example.myapplication.empty.podcast.PodcastScreen
import com.example.myapplication.todo.Books


class OpticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    //                var nav: NavGraph by remember { mutableStateOf(NavGraph.Main) }
//                val stack = remember { Stack<NavGraph>().apply { push(NavGraph.Main) } }
//                val navigator: (NavGraph) -> Unit = {
//                    nav = stack.push(it)
//                }
//                BackHandler(!stack.empty()) {
//                    stack.pop()
//                    nav = stack.peek()
//                }

    val app: AppThunk = remember { AppAndroid() }

    val repo = remember { RepositoryAndroid() }
    val booksRepository = remember { RepositoryBooksAndroid() }
    val nav: (NavGraph) -> Unit = { app.dispatch(AppAction.Navigate(it)) }

    val HomeScreen: HomeThunk = remember {
        HomeThunkAndroid(HomeState(), nav)
    }
    val MainScreen: MainThunk = remember {
        MainThunkAndroid(
            initialState = MainState(selectedTab = Tab.Home),
            repository = RepositoryAndroid(),
            booksRepository = booksRepository,
            withScope = WithScope()
        )
    }
    val loginScreen: LoginThunk = remember {
        LoginThunkAndroid(nav)
    }

    val appState: AppState by app.state.collectAsState()

    BackHandler(appState.currentScreen != NavGraph.Main) {
        app.dispatch(AppAction.Navigate(NavGraph.Back))
    }

    val b = rememberLazyListState()
    val b1 = rememberLazyListState()

    var t by remember {
        mutableStateOf(
            BooksThunkAndroid(
                repository = booksRepository,
                nav = nav,
                initialState = BooksState(
                    books = Books(emptyList()),
                    toolbar = ToolbarState(
                        title = "Books",
                        onBack = { nav(NavGraph.Back) }
                    ),
                    bottom = BottomState(BottomAction.values().toList(),{})
                )
            )
        )
    }

    var t1 by remember {
        mutableStateOf(
            BooksThunkAndroid(
                repository = booksRepository,
                nav = nav,
                initialState = BooksState(
                    books = Books(emptyList()),
                    toolbar = null,
                    bottom = null
                )
            )
        )
    }


    when (val screen: NavGraph? = appState.currentScreen) {
        is NavGraph.Blogs -> {
            BlogsScreen()
        }

        is NavGraph.Podcasts -> {
            PodcastScreen()
        }

        is NavGraph.Books -> {
            val r = remember(screen.new) {
                if (screen.new) {
                    t = BooksThunkAndroid(
                        repository = booksRepository,
                        nav = nav,
                        initialState = BooksState(
                            books = Books(emptyList()),
                            toolbar = ToolbarState(
                                title = "Books",
                                onBack = { nav(NavGraph.Back) }
                            ),
                            bottom = null
                        )
                    )
                    t
                }
                else t
            }

            r.BooksScreen(Modifier, b)
        }

        is NavGraph.Main -> {
            HomeScreen.run {
                t1.run {
                    MainScreen(b1)
                }
            }
        }

        is NavGraph.Login -> {
            loginScreen.run {
                LoginScreen()
            }
        }

        is NavGraph.BookDetail -> {
            BookThunkAndroid(
                repository = booksRepository,
                nav = nav,
                initialState = BookState(screen.book)
            ).run {
                BookScreen()
            }
        }

        is NavGraph.Back -> {
            app.dispatch(AppAction.Navigate(NavGraph.Back))
        }

        null -> if (appState.user == null) {
            app.dispatch(AppAction.Navigate(NavGraph.Login))
        } else {
            app.dispatch(AppAction.Navigate(NavGraph.Main))
        }
    }

}