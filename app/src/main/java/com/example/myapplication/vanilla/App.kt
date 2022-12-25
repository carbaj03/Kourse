package com.example.myapplication.vanilla

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import com.example.myapplication.asynchrony.with
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


data class User(
    val id: Id,
    val name: String
) {
    @JvmInline
    value class Id(val value: Int)
}

data class App(
    val screen: Screen,
    val user: User? = null
)

sealed interface Screen

object Splash : Screen

data class ProfileScreen(
    val title: Text,
    val password: String,
    val name: String,
    val login: Button,
    val signUp: Button,
) : Screen


data class Message(
    val author: String,
    val text: String,
    val date: String
)

data class ChatScreen(
    val title: Text,
    val msg: List<Message>,
    val toSend: String,
    val send: Button,
) : Screen


interface Action

data class Login(
    val password: String,
    val name: String
) : Action

interface UiComponent

data class Toolbar(
    val title: Text,
    val itemLeft: Item,
    val itemRight: List<Item>,
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
//    val action: suspend () -> Unit
) : UiComponent

data class TextField(
    val text: Text,
    val onChange: () -> Unit
)

sealed interface Text {
    val value: String
}

@JvmInline
value class Normal(override val value: String) : Text

@JvmInline
value class H1(override val value: String) : Text


suspend fun login(): User? {
    return User(User.Id(1), "name")
}

suspend fun signup() {

}

interface MessageRepository {
    suspend fun Message.send()
    fun receive(): Flow<Message>
}

fun MessageRepository(): MessageRepository = object : MessageRepository {
    private val f = MutableSharedFlow<Message>(1)

    override suspend fun Message.send() {
        f.emit(Message("Both", "reposnse to ${text}", ""))
    }

    override fun receive(): Flow<Message> =
        f.onEach { delay(100) }
}

context(MessageRepository)
suspend fun sendMessage(msg: String, author: String): Unit? {
    if (msg.isBlank()) return null
    Message(author, msg, "").send()
    return Unit
}

context(MessageRepository)
fun receiveSms(): Flow<Message> =
    receive()

context(App)
fun createProfileScreen(): ProfileScreen {
    return ProfileScreen(
        title = H1("Profile"),
        password = "",
        name = "",
        login = Button(text = Normal("Login")),
        signUp = Button(text = Normal("SignUp")),
    )
}


fun createChatScreen(): ChatScreen {
    return ChatScreen(
        title = H1("Chat"),
        msg = listOf(
            Message(
                author = "Pepe",
                text = "asdf",
                date = "asdf"
            ),
        ),
        send = Button(H1("Send")),
        toSend = ""
    )
}

val Text.toStyle: FontStyle?
    @Composable get() = when (this) {
        is H1 -> MaterialTheme.typography.headlineLarge.fontStyle
        is Normal -> MaterialTheme.typography.titleLarge.fontStyle
    }

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()

            var app by remember {
                mutableStateOf(App(screen = Splash))
            }

            with(scope, app) {

                LaunchedEffect(Unit) {
                    delay(1000)
                    app = app.copy(screen = createProfileScreen())
                }

                when (val screen: Screen = app.screen) {
                    is ProfileScreen -> {
                        Column(Modifier.fillMaxSize()) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        login()?.let {
                                            app = app.copy(
                                                screen = createChatScreen(),
                                                user = it
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = screen.login.text.value,
                                    fontStyle = screen.login.text.toStyle,
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        signup()
                                    }
                                }
                            ) {
                                Text(
                                    text = screen.signUp.text.value,
                                    fontStyle = screen.signUp.text.toStyle,
                                )
                            }
                        }
                    }
                    is Splash -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Splash")
                        }
                    }
                    is ChatScreen -> {
                        val repo = remember { MessageRepository() }
                        with(repo) { //this create multiple instances of a repository
                            LaunchedEffect(Unit) {
                                receiveSms().collect {
                                    app = app.copy(screen = (app.screen as ChatScreen).copy(msg = (app.screen as ChatScreen).msg.plus(it)))
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                screen.msg.forEach {
                                    Text(
                                        text = it.text,
                                        color = if (it.author == app.user?.name) Color.Blue else Color.Green
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Row() {
                                    OutlinedTextField(
                                        value = screen.toSend,
                                        onValueChange = { app = app.copy(screen = screen.copy(toSend = it)) }
                                    )
                                    IconButton(onClick = {
                                        scope.launch {
                                            sendMessage(screen.toSend, app.user!!.name)?.let {
                                                app = app.copy(
                                                    screen = screen.copy(
                                                        msg = screen.msg.plus(Message(app.user?.name ?: "", screen.toSend, "")),
                                                        toSend = ""
                                                    )
                                                )
                                            }
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}