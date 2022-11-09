package com.example.myapplication.todo

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

context(ThunkPodcast)
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        internal fun PodcastComponent(
    modifier: Modifier = Modifier,
) {
    val podcastState by podcast.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            LazyColumn(content = {
                stickyHeader {
                    Button(onClick = { increasePodcast() }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }

                    Button(onClick = { decreasePodcast() }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
                items(podcastState.podcast.value) {
                    Text(
                        text = it.title,
                        fontSize = 20.sp
                    )
                }
            })

        }
        if (podcastState.isLoading) {
            CircularProgressIndicator()
        }
    }
}


context(ThunkBooks) @OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BooksComponent(
    modifier: Modifier = Modifier,
) {
    val state by books.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            LazyColumn(content = {
                stickyHeader {
                    Button(onClick = { increase() }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }

                    Button(onClick = { decrease() }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
                items(state.books.value) {
                    Text(
                        text = it.title,
                        fontSize = 20.sp
                    )
                }
            })

        }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }
}

context(ThunkToolbar) @Composable
fun ToolbarComponent(
    modifier: Modifier = Modifier,
) {
    val state by toolbar.collectAsState()

    val height by animateDpAsState(targetValue = state.subTitle?.let { 128.dp } ?: 58.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { action(ToolbarAction.OnBack) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = state.title)
            state.subTitle?.let {
                Text(text = it)
            }
        }
    }
}

@Preview
@Composable
fun ToolbarComponentPreview() {
    object : ThunkToolbar {
        val s = MutableStateFlow(ToolbarState("_", ":"))
        override fun action(action: ToolbarAction) {
            when (action) {
                ToolbarAction.Load -> TODO()
                ToolbarAction.OnBack -> s.value = s.value.copy("app", if(s.value.subTitle == null ) "rr" else null)
                ToolbarAction.OnClose -> TODO()
            }
        }

        override val toolbar: StateFlow<ToolbarState> = s
    }.run {
        ToolbarComponent()
    }
}


@Preview
@Composable
fun BooksComponentPreview() {
    with(
        ThunkBooks(store = AppGraph().store)
    ) {
        BooksComponent()
    }
}