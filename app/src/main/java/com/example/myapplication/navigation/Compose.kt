package com.example.myapplication.navigation

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.TypeWrapper
import com.example.myapplication.with


@Composable
@Suppress("SUBTYPING_BETWEEN_CONTEXT_RECEIVERS")
inline fun <A, B, C, R> remember(a: A, b: B, c: C, crossinline block: context(A, B, C) (TypeWrapper<C>) -> R): R =
    remember { with(a, b, c, block) }

@Composable
@Suppress("SUBTYPING_BETWEEN_CONTEXT_RECEIVERS")
inline fun <A, B, R> remember(a: A, b: B, crossinline block: context(A, B) (TypeWrapper<B>) -> R): R =
    remember { with(a, b, block) }


class ComposeImpl : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = SearchRepository()

        setContent {
            val scope = rememberCoroutineScope()
            val store: Store = remember(repo, scope) {
                Store(
                    start = { Start() },
                    finish = { finish() }
                )
            }
            with(store) {
                val state by state.collectAsState()

                BackHandler(enabled = true) { back() }

                when (val screen = state.screen) {
                    is Dashboard -> {
                        LaunchedEffect(key1 = Unit, block = { screen.load() })
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            LazyColumn(content = {
                                items(screen.counters.map { it.key + " : " + it.value }) {
                                    Text(text = it.toString())
                                }
                            })
                            when (val tab = screen.currentTab) {
                                is Tab.One -> {
                                    LaunchedEffect(key1 = Unit, block = { tab.load() })
                                    var field by remember { mutableStateOf("") }

                                    TextField(
                                        value = field,
                                        onValueChange = { field = it }
                                    )
                                    Text(text = tab.counter.toString())
                                    Button(onClick = { tab.setCounter(field.toInt()); field = "" }) {
                                        Text(text = "Set Counter")
                                    }
                                }
                                is Tab.Two -> {
                                    LaunchedEffect(key1 = Unit, block = { tab.load() })
                                    if (tab.isLoading) {
                                        Text(text = "Loagind")
                                    }
                                    TextField(
                                        value = tab.toSearch,
                                        onValueChange = { tab.changeSearch(it) }
                                    )
                                    LazyColumn {
                                        items(tab.suggestions) {
                                            Row(
                                                Modifier
                                                    .clickable { tab.selectSuggestion(it) }
                                                    .padding(10.dp)
                                            ) {
                                                Text(text = it)
                                            }
                                        }
                                    }
                                    Button(onClick = { tab.search() }) {
                                        Text(text = "Search")
                                    }
                                    LazyColumn {
                                        items(tab.results) {
                                            Row(
                                                Modifier
                                                    .clickable { tab.selectResult() }
                                                    .padding(10.dp)
                                            ) {
                                                Text(text = it)
                                            }
                                        }
                                    }
                                }
                                is Tab.Three -> {
                                    when (val content = tab.screen) {
                                        is Tab3Screen1 -> {
                                            LaunchedEffect(key1 = Unit, block = { tab.load() })
                                            Text(text = "Tab 3 Screen 1")
                                            Button(onClick = content.next) {
                                                Text(text = "next")
                                            }
                                        }
                                        is Tab3Screen2 -> {
                                            LaunchedEffect(key1 = Unit, block = { content.load() })
                                            Text(text = "Tab 3 Screen 2")
                                            Button(onClick = { content.back() }) {
                                                Text(text = "Back")
                                            }
                                        }
                                    }
                                }
                                is Tab.Four -> {
                                    LaunchedEffect(key1 = Unit, block = { tab.load() })
                                    Column {
                                        Row() {
                                            val f = remember { { condition: Boolean -> if (condition) Color.Green else Color.Blue } }
                                            OutlinedButton(
                                                onClick = { tab.onSubTabSelected(tab.tab1) },
                                                colors = ButtonDefaults.buttonColors(containerColor = f(tab.currentSubTab is SubTab.One))
                                            ) {
                                                Text(text = "One")
                                            }
                                            OutlinedButton(
                                                onClick = { tab.onSubTabSelected(tab.tab2) },
                                                colors = ButtonDefaults.buttonColors(containerColor = f(tab.currentSubTab is SubTab.Two))
                                            ) {
                                                Text(text = "Two")
                                            }
                                            OutlinedButton(
                                                onClick = { tab.onSubTabSelected(tab.tab3) },
                                                colors = ButtonDefaults.buttonColors(containerColor = f(tab.currentSubTab is SubTab.Three))
                                            ) {
                                                Text(text = "Three")
                                            }
                                        }
                                    }
                                    when (val content = tab.currentSubTab) {
                                        is SubTab.One -> {
                                            var field by remember { mutableStateOf("") }
                                            Text(text = content.counter.toString())
                                            TextField(
                                                value = field,
                                                onValueChange = { field = it }
                                            )
                                            Button(onClick = { content.setCounter(field.toInt()); field = "" }) {
                                                Text(text = "Set Counter")
                                            }
                                        }
                                        is SubTab.Two -> {
                                            var field by remember { mutableStateOf("") }
                                            Text(text = content.counter.toString())
                                            TextField(
                                                value = field,
                                                onValueChange = { field = it }
                                            )
                                            Button(onClick = { content.setCounter(field.toInt()); field = "" }) {
                                                Text(text = "Set Counter")
                                            }
                                        }
                                        is SubTab.Three -> {
                                            var field by remember { mutableStateOf("") }
                                            Text(text = content.counter.toString())
                                            TextField(
                                                value = field,
                                                onValueChange = { field = it }
                                            )
                                            Button(onClick = { content.setCounter(field.toInt()); field = "" }) {
                                                Text(text = "Set Counter")
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                val f = remember { { condition: Boolean -> if (condition) Color.Green else Color.Blue } }
                                Button(
                                    onClick = { screen.onTabSelected(screen.tab1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = f(screen.currentTab is Tab.One))
                                ) {
                                    Text(text = "Tab 1")
                                }
                                Button(
                                    onClick = { screen.onTabSelected(screen.tab2) },
                                    colors = ButtonDefaults.buttonColors(containerColor = f(screen.currentTab is Tab.Two))
                                ) {
                                    Text(text = "Tab 2")
                                }
                                Button(
                                    onClick = { screen.onTabSelected(screen.tab3) },
                                    colors = ButtonDefaults.buttonColors(containerColor = f(screen.currentTab is Tab.Three))
                                ) {
                                    Text(text = "Tab 3")
                                }
                                Button(
                                    onClick = { screen.onTabSelected(screen.tab4) },
                                    colors = ButtonDefaults.buttonColors(containerColor = f(screen.currentTab is Tab.Four))
                                ) {
                                    Text(text = "Tab 4")
                                }
                            }
                        }
                    }
                    is Home -> {
                        Column {
                            Text(text = "Home")
                            Button(onClick = screen.login) {
                                Text(text = "Login")
                            }
                            Button(onClick = screen.signUp) {
                                Text(text = "Signup")
                            }
                        }
                    }
                    is Login -> {
                        Column() {
                            LaunchedEffect(key1 = Unit, block = { screen.onChange("name") })
                            Text(text = "Login")
                            Text(text = screen.name)
                            Button(onClick = { screen.next() }) {
                                Text(text = "Next")
                            }
                            Button(onClick = { screen.back() }) {
                                Text(text = "Back")
                            }
                        }
                    }
                    is Password.Login -> {
                        Column() {
                            Text(text = "Password")
                            Text(text = screen.password)
                            Button(onClick = screen.next) {
                                Text(text = "Next")
                            }
                            Button(onClick = { screen.back() }) {
                                Text(text = "Back")
                            }
                        }
                    }
                    is Password.Signup -> TODO()
                    is SignUp -> {
                        screen.next
                    }
                    is Splash -> {
                        Text(text = "Splash")
                        LaunchedEffect(Unit) {
                            screen.next()
                        }
                    }
                    is Start -> {

                    }
                    is Detail -> {
                        Text(text = screen.title)
                        Button(onClick = { screen.back() }) {
                            Text(text = "Back")
                        }
                    }
                }
            }
        }
    }
}