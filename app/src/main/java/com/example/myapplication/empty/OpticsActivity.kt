package com.example.myapplication.empty

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.empty.blog.BlogsScreen
import com.example.myapplication.empty.book.*
import com.example.myapplication.empty.home.HomeState
import com.example.myapplication.empty.home.HomeThunk
import com.example.myapplication.empty.home.HomeThunkAndroid
import com.example.myapplication.empty.login.LoginScreen
import com.example.myapplication.empty.login.LoginThunkAndroid
import com.example.myapplication.empty.main.MainScreen
import com.example.myapplication.empty.main.MainState
import com.example.myapplication.empty.main.MainThunkAndroid
import com.example.myapplication.empty.main.Tab
import com.example.myapplication.empty.podcast.PodcastDB
import com.example.myapplication.empty.podcast.PodcastNetwork
import com.example.myapplication.empty.podcast.PodcastRepository
import com.example.myapplication.empty.podcast.PodcastScreen
import com.example.myapplication.empty.user.*
import com.example.myapplication.empty.video.VideoDB
import com.example.myapplication.empty.video.VideoNetwork
import com.example.myapplication.empty.video.VideoRepository
import com.example.myapplication.todo.Books
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

class OpticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
//                Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                App()
//                }
            }
        }
    }
}

interface AppGraph {
    val client: HttpClient
    val authService: AuthService
    val repositoryDispatcher: RepositoryDispatcher
}

interface BookGraph {
    val bookNetwork: BookNetwork
    val bookDB: BookDB
    val bookRepository: BookRepository
}

interface VideoGraph {
    val videoNetwork: VideoNetwork
    val videoDB: VideoDB
    val videoRepository: VideoRepository
}

fun VideoGraph(): VideoGraph =
    object : VideoGraph {
        override val videoNetwork: VideoNetwork = VideoNetwork()
        override val videoDB: VideoDB = VideoDB()
        override val videoRepository: VideoRepository = with(videoNetwork) { with(videoDB) { VideoRepository() } }
    }

interface PodcastGraph {
    val podcastNetwork: PodcastNetwork
    val podcastDB: PodcastDB
    val podcastRepository: PodcastRepository
}

fun PodcastGraph(): PodcastGraph =
    object : PodcastGraph {
        override val podcastNetwork: PodcastNetwork = PodcastNetwork()
        override val podcastDB: PodcastDB = PodcastDB()
        override val podcastRepository: PodcastRepository = with(podcastNetwork) { with(podcastDB) { PodcastRepository() } }
    }

fun BookGraph(userClient: HttpClient): BookGraph =
    object : BookGraph {
        override val bookNetwork: BookNetwork = BookNetwork(userClient)
        override val bookDB: BookDB = BookDB()
        override val bookRepository: BookRepository = with(bookNetwork) { with(bookDB) { BookRepository() } }
    }

interface UserGraph : AppGraph, BookGraph, VideoGraph, PodcastGraph {
    val user: User
    val userClient: HttpClient
    val userDB: UserDB
    val userNetwork: UserNetwork
    val userRepository: UserRepository
}

context(AppGraph, BookGraph, PodcastGraph, VideoGraph)
fun createUserGraph(user: User, userClient: HttpClient): UserGraph =
    object : UserGraph, AppGraph by this@AppGraph, BookGraph by this@BookGraph, PodcastGraph by this@PodcastGraph, VideoGraph by this@VideoGraph {
        override val user: User = user
        override val userClient: HttpClient = userClient
        override val userDB: UserDB = UserDB()
        override val userNetwork: UserNetwork = UserNetwork(userClient)
        override val userRepository: UserRepository = with(userDB) { with(userNetwork) { with(repositoryDispatcher) { UserRepository() } } }
    }

fun createAppGraph(): AppGraph =
    object : AppGraph {
        override val repositoryDispatcher: RepositoryDispatcher =
            object : RepositoryDispatcher {
                override val io: CoroutineContext = Dispatchers.IO
                override val default: CoroutineContext = Dispatchers.Default
            }

        override val client: HttpClient by lazy(LazyThreadSafetyMode.NONE) {
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                    }
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.HEADERS
                }
                install(ContentNegotiation) {
                    json()
                }
            }
        }

        override val authService: AuthService =
            object : AuthService {
                override suspend fun login(
                    userName: String,
                    password: String,
                ): Either<AuthError, UserGraph> =
                    try {
                        val userClient = HttpClient(OkHttp) {
                            engine {
                                config {
                                    followRedirects(true)
                                }
                            }
                            install(Logging) {
                                logger = Logger.DEFAULT
                                level = LogLevel.HEADERS
                            }
                            install(ContentNegotiation) {
                                json()
                            }
                            install(Auth) {
                                basic {
                                    credentials {
                                        BasicAuthCredentials(username = userName, password = password)
                                    }
                                    realm = "Access to the '/' path"
                                }
                            }
                        }
                        if (userClient.get("http://192.168.0.101:5000/validate").status == HttpStatusCode.OK) {
                            with(BookGraph(userClient)) {
                                with(PodcastGraph()) {
                                    with(VideoGraph()) {
                                        createUserGraph(User(userName, password), userClient)
                                    }
                                }
                            }.right()
                        } else
                            AuthError.InvalidUser.left()
                    } catch (ex: Exception) {
                        AuthError.InvalidUser.left()
                    }
            }

    }

sealed interface AuthError {
    object InvalidUser : AuthError
}

interface AuthService {
    suspend fun login(userName: String, password: String): Either<AuthError, UserGraph>
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
    val appGraph: AppGraph = remember {
        createAppGraph()
    }

    val app: AppThunk = remember { AppAndroid() }

    val scope = rememberCoroutineScope()

    val nav: (NavGraph) -> Unit = { app.dispatch(AppAction.Navigate(it)) }

    val homeThunkAndroid: HomeThunk = remember {
        HomeThunkAndroid(HomeState(), nav)
    }

    val appState: AppState by app.state.collectAsState()

    BackHandler(appState.currentScreen !is UserNavGraph.Main) {
        app.dispatch(AppAction.Navigate(NavGraph.Back))
    }

    val b = rememberLazyListState()
    val b1 = rememberLazyListState()

    when (val screen: NavGraph? = appState.currentScreen) {
        is NavGraph.Back -> {
            app.dispatch(AppAction.Navigate(NavGraph.Back))
        }
        is AppNavGraph -> {
            with(appGraph.authService) {
                with(scope) {
                    when (screen) {
                        AppNavGraph.Login -> {
                            LoginThunkAndroid(
                                nav = nav,
                                appAction = { app.dispatch(it) }
                            ).LoginScreen()
                        }
                    }
                }
            }
        }
        is UserNavGraph -> {
            appState.userGraph?.run {
                var booksThunkAndroid by remember {
                    with(WithScope(scope.coroutineContext.job)) {
                        mutableStateOf(
                            BooksThunkAndroid(
                                repository = bookRepository,
                                nav = nav,
                                initialState = BooksState(
                                    books = Books(emptyList()),
                                    toolbar = null,
                                    bottom = null
                                )
                            )
                        )
                    }
                }

                when (screen) {
                    is UserNavGraph.Blogs -> {
                        BlogsScreen()
                    }
                    is UserNavGraph.BookDetail -> {
                        with(WithScope(scope.coroutineContext.job)) {
                            BookThunkAndroid(
                                repository = bookRepository,
                                nav = nav,
                                initialState = BookState(screen.book)
                            ).run {
                                BookScreen()
                            }
                        }
                    }
                    is UserNavGraph.Books -> {
                        val r = remember(screen.new) {
                            if (screen.new) {
                                with(WithScope(scope.coroutineContext.job)) {
                                    booksThunkAndroid = BooksThunkAndroid(
                                        repository = bookRepository,
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
                                    booksThunkAndroid
                                }
                            } else booksThunkAndroid
                        }
                        r.BooksScreen(Modifier, b)
                    }
                    is UserNavGraph.Main -> {
                        with(userRepository) {
                            with(bookRepository) {
                                with(videoRepository) {
                                    with(podcastRepository) {
                                        with(WithScope(scope.coroutineContext.job)) {
                                            booksThunkAndroid.run {
                                                homeThunkAndroid.run {
                                                    MainThunkAndroid(
                                                        initialState = MainState(selectedTab = Tab.Home, drawer = false),
                                                        nav = nav
                                                    ).run {
                                                        MainScreen(booksLazyListState = b1)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is UserNavGraph.Podcasts -> {
                        PodcastScreen()
                    }
                }
            }
        }
        null -> if (appState.userGraph == null) {
            app.dispatch(AppAction.Navigate(AppNavGraph.Login))
        } else {
//            app.dispatch(AppAction.Navigate(NavGraph.Main()))
        }
    }
}