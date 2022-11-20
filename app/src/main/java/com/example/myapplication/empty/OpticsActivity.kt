package com.example.myapplication.empty

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.empty.blog.BlogsScreen
import com.example.myapplication.empty.book.BookDB
import com.example.myapplication.empty.book.BookNetwork
import com.example.myapplication.empty.book.BookRepository
import com.example.myapplication.empty.book.BookScreen
import com.example.myapplication.empty.book.BookState
import com.example.myapplication.empty.book.BookThunkAndroid
import com.example.myapplication.empty.book.BooksNetwork
import com.example.myapplication.empty.book.BooksScreen
import com.example.myapplication.empty.book.BooksState
import com.example.myapplication.empty.book.BooksThunkAndroid
import com.example.myapplication.empty.book.BottomAction
import com.example.myapplication.empty.book.BottomState
import com.example.myapplication.empty.book.ToolbarState
import com.example.myapplication.empty.home.HomeState
import com.example.myapplication.empty.home.HomeThunk
import com.example.myapplication.empty.home.HomeThunkAndroid
import com.example.myapplication.empty.login.LoginScreen
import com.example.myapplication.empty.login.LoginThunk
import com.example.myapplication.empty.login.LoginThunkAndroid
import com.example.myapplication.empty.main.MainState
import com.example.myapplication.empty.main.MainThunkAndroid
import com.example.myapplication.empty.main.Tab
import com.example.myapplication.empty.main.invoke
import com.example.myapplication.empty.podcast.PodcastDB
import com.example.myapplication.empty.podcast.PodcastNetwork
import com.example.myapplication.empty.podcast.PodcastRepository
import com.example.myapplication.empty.podcast.PodcastScreen
import com.example.myapplication.empty.user.RepositoryDispatcher
import com.example.myapplication.empty.user.User
import com.example.myapplication.empty.user.UserDB
import com.example.myapplication.empty.user.UserNetwork
import com.example.myapplication.empty.user.UserRepository
import com.example.myapplication.empty.video.VideoDB
import com.example.myapplication.empty.video.VideoNetwork
import com.example.myapplication.empty.video.VideoRepository
import com.example.myapplication.todo.Books
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
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
    
    companion object
}

interface BookGraph {
    val booksNetwork: BookNetwork
    val booksDB: BookDB
    val booksRepository: BookRepository
    
    companion object
}

interface VideoGraph {
    val videoNetwork: VideoNetwork
    val videoDB: VideoDB
    val videoRepository: VideoRepository
    
    companion object
}

operator fun VideoGraph.Companion.invoke(): VideoGraph =
    object : VideoGraph {
        override val videoNetwork: VideoNetwork = VideoNetwork()
        override val videoDB: VideoDB = VideoDB()
        override val videoRepository: VideoRepository = with(videoNetwork) { with(videoDB) { VideoRepository() } }
    }

interface PodcastGraph {
    val podcastNetwork: PodcastNetwork
    val podcastDB: PodcastDB
    val podcastRepository: PodcastRepository
    
    companion object
}

operator fun PodcastGraph.Companion.invoke(): PodcastGraph =
    object : PodcastGraph {
        override val podcastNetwork: PodcastNetwork = PodcastNetwork()
        override val podcastDB: PodcastDB = PodcastDB()
        override val podcastRepository: PodcastRepository = with(podcastNetwork) { with(podcastDB) { PodcastRepository() } }
    }

operator fun BookGraph.Companion.invoke(): BookGraph =
    object : BookGraph {
        override val booksNetwork: BookNetwork = BooksNetwork()
        override val booksDB: BookDB = BookDB()
        override val booksRepository: BookRepository = with(booksNetwork) { with(booksDB) { BookRepository() } }
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

var appGraph: AppGraph =
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
                            with(BookGraph()) {
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
    
    val app: AppThunk = remember { AppAndroid() }
    
    val scope = rememberCoroutineScope()
    
    val bookRepository = remember { with(BooksNetwork()) { with(BookDB()) { BookRepository() } } }
    val podcastRepository = remember { with(PodcastNetwork()) { with(PodcastDB()) { PodcastRepository() } } }
    val videoRepository = remember { with(VideoNetwork()) { with(VideoDB()) { VideoRepository() } } }
    
    val nav: (NavGraph) -> Unit = { app.dispatch(AppAction.Navigate(it)) }
    
    val HomeScreen: HomeThunk = remember {
        HomeThunkAndroid(HomeState(), nav)
    }
    
    val loginScreen: LoginThunk = remember {
        with(appGraph.authService) { with(scope) { LoginThunkAndroid(nav) } }
    }
    
    val appState: AppState by app.state.collectAsState()
    
    BackHandler(appState.currentScreen !is NavGraph.Main) {
        app.dispatch(AppAction.Navigate(NavGraph.Back))
    }
    
    val b = rememberLazyListState()
    val b1 = rememberLazyListState()
    
    var t by remember {
        mutableStateOf(
            BooksThunkAndroid(
                repository = bookRepository,
                nav = nav,
                initialState = BooksState(
                    books = Books(emptyList()),
                    toolbar = ToolbarState(
                        title = "Books",
                        onBack = { nav(NavGraph.Back) }
                    ),
                    bottom = BottomState(BottomAction.values().toList())
                )
            )
        )
    }
    
    var booksThunkAndroid: BooksThunkAndroid by remember {
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
                    t
                } else t
            }
            
            r.BooksScreen(Modifier, b)
        }
        
        is NavGraph.Main -> {
            booksThunkAndroid.run {
                HomeScreen.run {
                    with(screen.userGraph.userRepository) {
                        with(screen.userGraph.booksRepository) {
                            with(screen.userGraph.videoRepository) {
                                with(screen.userGraph.podcastRepository) {
                                    with(scope) {
                                        MainThunkAndroid(
                                            initialState = MainState(selectedTab = Tab.Home, drawer = false),
                                            withScope = WithScope(),
                                            nav
                                        ).invoke(booksLazyListState = b1)
                                    }
                                }
                            }
                        }
                    }
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
                repository = bookRepository,
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
//            app.dispatch(AppAction.Navigate(NavGraph.Main()))
        }
    }
}