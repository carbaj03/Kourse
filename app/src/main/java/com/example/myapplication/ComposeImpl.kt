package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

class ComposeImpl : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val network = object : Network {}
            val db = object : DB {}
            val repository: RandomRepository by lazy {
                with(network, db) { randomRepository() }
            }
            App(repository)
        }
    }
}

@Composable
fun App(repository: RandomRepository) {

    val scope = rememberCoroutineScope()
    val store = remember {
        Store(
            start = with(repository) { Start() },
            coroutineScope = scope
        )
    }
    with(store.navigator, store.reducer, store.navigator, store.sideEffect, store.tracker, repository) {
        val state: Counter by store.state.collectAsState()

        when (val screen = state.screen.screens.last()) {
            is Main -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column {
                        Text(text = screen.display)
                        Button(onClick = screen.increment) {
                            Text(text = "Increment")
                        }
                        Button(onClick = screen.decrement) {
                            Text(text = "Decrement")
                        }
                        Button(onClick = screen.restart) {
                            Text(text = "Restart")
                        }
                        Button(onClick = screen.random) {
                            Text(text = "Random")
                        }
                    }
                    if (screen.loading)
                        CircularProgressIndicator(modifier = Modifier.testTag("loader"))
                }
            }
            is Splash -> {
                LaunchedEffect(Unit) {
                    screen.next()
                }
                Column {
                    Text(text = "Splash")
                }
            }
            is Start -> {
                LaunchedEffect(Unit) {
                    screen.next(store.sideEffect, store.navigator, store.reducer, store.tracker)
                }
            }
        }
    }

}