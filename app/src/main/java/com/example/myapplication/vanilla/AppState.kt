package com.example.myapplication.vanilla

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.myapplication.asynchrony.with
import com.example.myapplication.vanilla.Button.State.Disabled
import com.example.myapplication.vanilla.Button.State.Enabled
import com.example.myapplication.vanilla.IsEqual.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface User

data class LoggedUser(
    val id: Id,
    val name: String
) : User {
    @JvmInline
    value class Id(val value: Int)
}

object Anonymous : User

data class Settings(
    val mode: Mode
)

enum class Mode {
    Light, Dark
}

fun Settings.toggle(): Mode =
    when (mode) {
        Mode.Light -> Mode.Dark
        Mode.Dark -> Mode.Light
    }


data class AppState(
    val screen: Screen = Initial,
    val user: User = Anonymous,
    val settings: Settings = Settings(Mode.Light)
)

context(Reducer, SideEffect)
fun AppState.init() {
    navigate<Initial> { createSplash() }
}

inline fun User.logged(f: (LoggedUser) -> Unit): Unit =
    when (this) {
        is Anonymous -> {}
        is LoggedUser -> f(this)
    }

sealed interface Screen

object Initial : Screen

data class Splash(
    val duration: Duration,
    val action: () -> Unit
) : Screen


data class ProfileScreen(
    val login: Button,
    val signUp: Button,
) : Screen

data class SignupScreen(
    val toolbar: Toolbar,
    val email: String,
    val next: Button,
    val login: Button,
    val google: Button,
    val microsoft: Button,
) : Screen

data class SignupPasswordScreen(
    val toolbar: Toolbar,
    val password: TextField,
    val next: Button,
    val login: Button,
    val google: Button,
    val microsoft: Button,
    val errors: List<Error>,
) : Screen {
    sealed interface Error {
        object InvalidPassword : Error
    }
}

data class LoginScreen(
    val toolbar: Toolbar,
    val email: TextField,
    val next: Button,
    val signUp: Button,
    val google: Button,
    val microsoft: Button,
    val errors: List<Error>
) : Screen {
    sealed interface Error {
        object InvalidEmail : Error
    }
}

data class ChatScreen(
    val toolbar: Toolbar,
    val content: Content,
    val toSend: TextField,
    val send: Button,
    val receive: () -> Unit
) : Screen {
    sealed interface Content {
        data class Messages(val msg: List<Message>) : Content
        data class Examples(
            val exmaple1: Text,
            val exmaple2: Text,
            val exmaple3: Text,
        ) : Content
    }
}

data class Message(
    val author: String,
    val text: String,
    val date: String
)

data class Conservation(
    val id: Id,
    val name: String,
    val user: LoggedUser.Id,
    val msg: List<Message>,
) {
    @JvmInline
    value class Id(val value: Int)
}

interface Action

data class Login(
    val password: String,
    val name: String
) : Action

interface UiComponent

data class Toolbar(
    val title: Text,
    val itemLeft: Item? = null,
    val itemRight: List<Item> = emptyList(),
) {
    sealed interface Item {
        object Back : Item
        object Close : Item

        object Menu : Item
        object ContactUs : Item
    }
}

data class Button(
    val text: Text,
    val state: State,
) : UiComponent {
    sealed interface State {
        data class Enabled(val action: () -> Unit) : State
        object Disabled : State
    }
}

fun Button.action(): Unit =
    when (state) {
        Disabled -> {}
        is Enabled -> state.action()
    }

data class TextField(
    val text: Text,
    val onChange: (String) -> Unit
)

sealed interface Text {
    val value: String
}

@JvmInline
value class Normal(override val value: String) : Text

@JvmInline
value class H1(override val value: String) : Text


suspend fun login(
    name: String,
    password: String,
): User? {
    return if (name == "root" && password == "root")
        LoggedUser(LoggedUser.Id(1), "name")
    else
        null
}

suspend fun signupEmail(
    email: String
): String? {
    return email
}

suspend fun signupPassword(
    password: String
): User? {
    return LoggedUser(LoggedUser.Id(1), "name")
}

suspend fun navigateSignup() {

}

interface MessageRepository {
    suspend fun Message.send()
    fun receive(): Flow<Message>
}

fun MessageRepository(): MessageRepository = object : MessageRepository {
    private val f = MutableSharedFlow<Message>(1)

    override suspend fun Message.send() {
        f.emit(Message("Both", "response to $text", ""))
    }

    override fun receive(): Flow<Message> =
        f.onEach { delay(100) }
}

val repository = MessageRepository()

suspend fun sendMessage(msg: String, author: String): Unit? {
    if (msg.isBlank()) return null
    with(repository) { Message(author, msg, "").send() }
    return Unit
}

fun receiveSms(): Flow<Message> =
    repository.receive()

context(Reducer, SideEffect)
fun createLoginScreen(): LoginScreen {
    return LoginScreen(
        toolbar = Toolbar(title = H1("Login - Screen")),
        email = TextField(
            text = Normal(""),
            onChange = {
                navigate<LoginScreen> {
                    copy(email = email.copy(text = Normal(it)))
                }
            }),
        signUp = Button(
            text = Normal("SignUp"),
            state = Enabled { navigate { createSignupScreen() } }
        ),
        microsoft = Button(
            text = Normal(value = "Microsoft Account"),
            state = Disabled
        ),
        google = Button(
            text = Normal("Google Account"),
            state = Disabled
        ),
        next = Button(
            text = Normal("Continue"),
            state = Enabled {
                sideEffect {
                    if (screen is LoginScreen)
                        if (isValidEmail(screen.email.text.value)) navigate<LoginScreen> { createSignupPasswordScreen() }
                        else reduce { copy(screen = (screen as LoginScreen).copy(errors = screen.errors.plus(LoginScreen.Error.InvalidEmail))) }
                }
            }
        ),
        errors = emptyList()
    )
}

val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")

fun isValidEmail(email: String): Boolean {
    return emailRegex.matches(email)
}


val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}\$")

fun isValidPassword(password: String): Boolean {
    return password.isNotEmpty()
//    return passwordRegex.matches(password)
}

fun interface Reducer {
    fun reduce(old: AppState.() -> AppState)
}

@JvmName("other")
inline fun <reified A : Screen> Reducer.navigate(crossinline old: A.() -> Screen): Unit =
    reduce {
        copy(
            screen = when (screen) {
                is A -> old(screen)
                else -> screen
            }
        )
    }

inline fun Reducer.navigate(crossinline old: Screen.() -> Screen): Unit =
    navigate<Screen>(old)

fun interface SideEffect {
    fun sideEffect(old: context(AppState) CoroutineScope.() -> Unit)
}

inline fun <reified A : Screen> SideEffect.sideEffect(crossinline old: context(A) CoroutineScope.() -> Unit): Unit =
    sideEffect {
        when (screen) {
            is A -> old(screen, this)
            else -> screen
        }
    }


context(Reducer, SideEffect)
fun createSplash(): Splash =
    Splash(
        duration = 1.seconds,
        action = { navigate { createProfileScreen() } }
    )

context(Reducer, SideEffect)
fun createProfileScreen(): ProfileScreen =
    ProfileScreen(
        login = Button(
            text = Normal("Login"),
            state = Enabled {
                reduce {
                    copy(
                        screen = createLoginScreen(),
                        user = LoggedUser(LoggedUser.Id(1), "Yo")
                    )
                }
            }
        ),
        signUp = Button(text = Normal("Signup"), state = Disabled)
    )

context(Reducer, SideEffect)
fun createChatScreen(): ChatScreen {
    return ChatScreen(
        toolbar = Toolbar(title = H1("Chat")),
        content = ChatScreen.Content.Examples(
            exmaple1 = Normal(""),
            exmaple2 = Normal(""),
            exmaple3 = Normal("")
        ),
        send = Button(H1("Send"),
            state = Enabled {
                sideEffect {
                    if (screen is ChatScreen)
                        launch { sendMessage(screen.toSend.text.value, (user as LoggedUser).name) }
                }
                navigate {
                    if (this is ChatScreen) copy(
                        content = when (content) {
                            is ChatScreen.Content.Examples -> ChatScreen.Content.Messages(listOf(Message("Both", text = toSend.text.value, "")))
                            is ChatScreen.Content.Messages -> ChatScreen.Content.Messages(content.msg.plus(Message("Both", toSend.text.value, "")))
                        }
                    ) else this
                }
            }
        ),
        toSend = TextField(
            text = Normal(""),
            onChange = {
                navigate<ChatScreen> {
                    copy(toSend = toSend.copy(text = Normal(it)))
                }
            }
        ),
        receive = {
            sideEffect {
                launch {
                    receiveSms().collect {
                        navigate<ChatScreen> {
                            copy(
                                content = when (content) {
                                    is ChatScreen.Content.Examples -> content
                                    is ChatScreen.Content.Messages -> content.copy(content.msg.plus(it))
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

fun createSignupScreen(): SignupScreen {
    return SignupScreen(
        toolbar = Toolbar(title = H1("Signup")),
        google = Button(text = Normal("Google Account"), state = Disabled),
        microsoft = Button(text = Normal("Microsoft Account"), state = Disabled),
        login = Button(text = Normal("Log in"), state = Disabled),
        next = Button(text = Normal("Continue"), state = Disabled),
        email = "",
    )
}

context(Reducer, SideEffect)
fun createSignupPasswordScreen(): SignupPasswordScreen {
    return SignupPasswordScreen(
        toolbar = Toolbar(title = H1("Signup - Password")),
        google = Button(Normal("Google Account"), state = Disabled),
        microsoft = Button(Normal("Microsoft Account"), state = Disabled),
        login = Button(Normal("Log in"), state = Disabled),
        next = Button(
            text = Normal("Continue"),
            state = Disabled
        ),
        password = TextField(
            text = Normal(""),
            onChange = {
                navigate<SignupPasswordScreen> {
                    copy(
                        password = password.copy(text = Normal(it)),
                        next = next.copy(
                            state = if (isValidPassword(it)) Enabled { navigate { createChatScreen() } }
                            else Disabled
                        )
                    )
                }
            }),
        errors = emptyList()
    )
}

val Text.toStyle: TextStyle
    @Composable get() = when (this) {
        is H1 -> MaterialTheme.typography.headlineLarge
        is Normal -> MaterialTheme.typography.titleLarge
    }

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val scope: CoroutineScope = rememberCoroutineScope()

    val color: Mode = if (isSystemInDarkTheme()) Mode.Dark else Mode.Light

    var app: AppState by remember {
        mutableStateOf(
            AppState(
                screen = Initial,
                settings = Settings(mode = color)
            )
        )
    }

    val reducer: Reducer = remember {
        Reducer { old -> app = old(app) }
    }

    val sideEffect: SideEffect = remember {
        SideEffect { old -> old(app, scope) }
    }

    with(scope, app, reducer, sideEffect) {
        when (val screen: Screen = app.screen) {
            is LoginScreen -> {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        text = screen.toolbar.title.value,
                        style = screen.toolbar.title.toStyle,
                    )
                    OutlinedTextField(
                        value = screen.email.text.value,
                        onValueChange = {
                            app = app.copy(
                                screen = (app.screen as LoginScreen).copy(email = (app.screen as LoginScreen).email.copy(text = Normal(it)))
                            )
                        }
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                app = app.copy(
                                    screen = createSignupPasswordScreen()
                                )
                            }
                        }
                    ) {
                        Text(
                            text = screen.next.text.value,
                            style = screen.next.text.toStyle,
                        )
                    }
                    Text(
                        modifier = Modifier.clickable {
                            scope.launch {
                                app = app.copy(
                                    screen = createSignupScreen()
                                )
                            }
                        },
                        text = screen.signUp.text.value,
                        style = screen.signUp.text.toStyle,
                    )
                    Button(onClick = {}) {
                        Text(
                            text = screen.google.text.value,
                            style = screen.google.text.toStyle,
                        )
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            text = screen.microsoft.text.value,
                            style = screen.microsoft.text.toStyle,
                        )
                    }
                }
            }
            is Initial -> {
                LaunchedEffect(Unit) {
                    app.init()
                }
            }
            is Splash -> {
                LaunchedEffect(Unit) {
                    delay(screen.duration)
                    app = app.copy(screen = createProfileScreen())
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Splash")
                }
            }
            is ChatScreen -> {
                LaunchedEffect(Unit) {
                    receiveSms().collect {
                        app = app.copy(
                            screen = (app.screen as ChatScreen).copy(
                                content = when (val content = (app.screen as ChatScreen).content) {
                                    is ChatScreen.Content.Examples -> content
                                    is ChatScreen.Content.Messages -> content.copy(content.msg.plus(it))
                                }
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            modifier = Modifier.clickable {
                                app = app.copy(
                                    screen = createProfileScreen(),
                                    user = Anonymous
                                )
                            },
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                        Text(
                            text = screen.toolbar.title.value,
                            style = screen.toolbar.title.toStyle,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier.clickable {
                                app = app.copy(
                                    settings = app.settings.copy(mode = app.settings.toggle())
                                )
                            },
                            imageVector = when (app.settings.mode) {
                                Mode.Light -> Icons.Default.Settings
                                Mode.Dark -> Icons.Default.Create
                            },
                            contentDescription = null
                        )
                    }
                    when (val content = screen.content) {
                        is ChatScreen.Content.Examples -> {
                            Text(
                                text = content.exmaple1.value,
                                style = content.exmaple1.toStyle,
                            )
                            Text(
                                text = content.exmaple1.value,
                                style = content.exmaple1.toStyle,
                            )
                        }
                        is ChatScreen.Content.Messages -> {
                            content.msg.forEach {
                                Text(
                                    text = it.text,
                                    color = when (val user = app.user) {
                                        is Anonymous -> Color.Gray
                                        is LoggedUser -> when (it.author isEqual user.name) {
                                            Equal -> Color.Blue
                                            NotEqual -> Color.Black
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("messageToSend"),
                            value = screen.toSend.text.value,
                            onValueChange = {
//                                screen.toSend.onChange(it)
                                app = app.copy(screen = screen.copy(toSend = screen.toSend.copy(Normal(it))))
                            },
                            trailingIcon = {
                                IconButton(
                                    modifier = Modifier.testTag("sendMessage"),
                                    onClick = {
                                        scope.launch {
                                            app.user.logged { user ->
                                                sendMessage(screen.toSend.text.value, user.name)?.let {
                                                    app = app.copy(
                                                        screen = screen.copy(
                                                            toSend = screen.toSend.copy(text = Normal("")),
                                                            content = ChatScreen.Content.Messages(
                                                                when (val content = screen.content) {
                                                                    is ChatScreen.Content.Examples -> {
                                                                        listOf(Message(user.name, "me - " + screen.toSend.text.value, ""))
                                                                    }
                                                                    is ChatScreen.Content.Messages -> {
                                                                        content.msg.plus(Message(user.name, "me - " + screen.toSend.text.value, ""))
                                                                    }
                                                                }
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }
            }
            is SignupScreen -> {
                Column(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = screen.toolbar.title.value,
                        style = screen.toolbar.title.toStyle,
                    )
                    OutlinedTextField(
                        value = screen.email,
                        onValueChange = { app = app.copy(screen = screen.copy(email = it)) }
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                signupEmail(screen.email)?.let {
                                    app = app.copy(
                                        screen = createSignupPasswordScreen(),
                                    )
                                }
                            }
                        }
                    ) {
                        Text(
                            text = screen.next.text.value,
                            style = screen.next.text.toStyle,
                        )
                    }
                    Text(
                        modifier = Modifier.clickable {
                            scope.launch {
                                app = app.copy(
                                    screen = createProfileScreen(),
                                )
                            }
                        },
                        text = screen.login.text.value,
                        style = screen.login.text.toStyle,
                    )
                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            text = screen.google.text.value,
                            style = screen.google.text.toStyle,
                        )
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            text = screen.microsoft.text.value,
                            style = screen.microsoft.text.toStyle,
                        )
                    }
                }
            }
            is SignupPasswordScreen -> {
                Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = screen.toolbar.title.value,
                        style = screen.toolbar.title.toStyle,
                    )
                    OutlinedTextField(
                        value = screen.password.text.value,
                        onValueChange = { text ->
                            app = app.copy(
                                screen = screen.copy(
                                    password = screen.password.copy(
                                        text = Normal(text)
                                    )
                                )
                            )
                        }
                    )
                    Button(onClick = {
                        scope.launch {
                            signupPassword(screen.password.text.value)?.let {
                                app = app.copy(
                                    screen = createChatScreen(),
                                    user = it,
                                )
                            }
                        }
                    }) {
                        Text(
                            text = screen.next.text.value,
                            style = screen.next.text.toStyle,
                        )
                    }
                    Text(
                        modifier = Modifier.clickable {
                            scope.launch {
                                app = app.copy(
                                    screen = createProfileScreen()
                                )
                            }
                        },
                        text = screen.login.text.value,
                        style = screen.login.text.toStyle,
                    )
                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            text = screen.google.text.value,
                            style = screen.google.text.toStyle,
                        )
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            text = screen.microsoft.text.value,
                            style = screen.microsoft.text.toStyle,
                        )
                    }
                }
            }
            is ProfileScreen -> {
                Column {
                    Text("Profile")
                    Button(onClick = { screen.login.action() }) {
                        Text(
                            text = screen.login.text.value,
                            style = screen.login.text.toStyle,
                        )
                    }
                    Button(onClick = { screen.signUp.action() }) {
                        Text(
                            text = screen.signUp.text.value,
                            style = screen.signUp.text.toStyle,
                        )
                    }
                }
            }
        }
    }
}