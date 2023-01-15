package com.example.myapplication.navigation

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.with
import kotlinx.coroutines.flow.MutableStateFlow

class ComposeImpl : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = MutableStateFlow(App(screen = Start, finish = { finish() }))

        val navigator = Navigator { option ->
            when (option.addToStack) {
                true -> when (option.clearAll) {
                    true -> listOf(this)
                    false -> when (option.unique) {
                        true -> state.value.stack.screens
                            .filterNot { it.route == state.value.screen.route }
                            .plus(state.value.screen)
                            .filterNot { it.route == route }
                            .plus(this)
                        false -> state.value.stack.screens
                            .filterNot { it.route == state.value.screen.route }
                            .plus(state.value.screen)
                            .plus(this)
                    }
                }
                false -> when (option.clearAll) {
                    true -> emptyList()
                    false -> state.value.stack.screens
                }
            }.let {
                state.value = state.value.copy(
                    screen = this,
                    stack = BackStack(it)
                )
            }
        }

        val provider = object : Reducer {
            override fun App.reduce() {
                state.value = this
            }

            override val state: App
                get() = state.value
        }

        with(navigator, provider)
        {
            setContent {
                val screen by state.collectAsState()

                BackHandler(enabled = true) { back() }

                when (val s = screen.screen) {
                    is Dashboard -> {
                        Column {
                            Text(text = s.currentTab.toString())
                            when (val tab = s.currentTab) {
                                is Tab.Tab1 -> {
                                    Text(text = tab.counter.toString())
                                    Button(onClick = { tab.setCounter(tab.counter + 2) }) {
                                        Text(text = "Set Counter")
                                    }
                                }
                                is Tab.Tab2 -> {
                                    Text(text = screen.counter.toString())
                                    Button(onClick = { tab.setCounter(screen.counter + 2) }) {
                                        Text(text = "Set Counter")
                                    }
                                    Button(onClick = { tab.detail() }) {
                                        Text(text = "Detail")
                                    }
                                }
                                is Tab.Tab3 -> {
                                    when (val content = tab.screen) {
                                        is Tab3Screen1 -> {
                                            Text(text = "Tab 3 content")
                                            Button(onClick = content.next) {
                                                Text(text = "next")
                                            }
                                        }
                                        is Tab3Screen2 -> {
                                            Text(text = "Tab 3 content")
                                            Button(onClick = content.back) {
                                                Text(text = "Back")
                                            }
                                        }
                                    }
                                }
                            }
                            Row() {
                                Button(onClick = { s.onTabSelected(s.tab1) }) {
                                    Text(text = "${s.currentTab is Tab.Tab1} Tab 1")
                                }
                                Button(onClick = { s.onTabSelected(s.tab2) }) {
                                    Text(text = "${s.currentTab is Tab.Tab2} Tab 2")
                                }
                                Button(onClick = { s.onTabSelected(s.tab3) }) {
                                    Text(text = "${s.currentTab is Tab.Tab3} Tab 3")
                                }
                            }
                        }
                    }
                    is Home -> {
                        Column() {
                            Text(text = "Home")
                            Button(onClick = s.login) {
                                Text(text = "Login")
                            }
                            Button(onClick = s.signUp) {
                                Text(text = "Signup")
                            }
                        }
                    }
                    is Login -> {
                        Column() {
                            Text(text = "Login")
                            Text(text = s.name)
                            Button(onClick = s.next) {
                                Text(text = "Next")
                            }
                            Button(onClick = s.back) {
                                Text(text = "Back")
                            }
                        }
                    }
                    is Password.Login -> {
                        Column() {
                            Text(text = "Password")
                            Text(text = s.password)
                            Button(onClick = s.next) {
                                Text(text = "Next")
                            }
                            Button(onClick = s.back) {
                                Text(text = "Back")
                            }
                        }
                    }
                    is Password.Signup -> TODO()
                    is Screen1Detail -> {
                        Column() {
                            Text(text = "Detail")
                            Text(text = "Counter : " + screen.counter)
                            Button(onClick = { s.setCounter(screen.counter + 2) }) {
                                Text(text = "Set counter")
                            }
                            Button(onClick = s.close) {
                                Text(text = "Close")
                            }
                        }
                    }
                    is SignUp -> {
                        s.next
                    }
                    is Splash -> {
                        s.next()
                    }
                    Start -> {
                        Splash().navigate(false)
                    }
                }
            }
        }
    }
}