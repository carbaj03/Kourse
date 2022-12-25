package com.example.myapplication.empty.book

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import arrow.core.Either
import arrow.core.right
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.todo.Book
import com.example.myapplication.todo.BookId
import com.example.myapplication.todo.Books

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksThunk.BooksScreen(modifier: Modifier, b: LazyListState) {
    LaunchedEffect(Unit) {
        dispatch(BooksAction.Load)
    }

    val s: BooksState by state.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            s.toolbar?.run {
                TopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {},
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = s.bottom != null,
                enter = fadeIn() + slideInVertically(animationSpec = tween(400), initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically()
            ) {
                s.bottom?.run {
                    BottomAppBar(
                        actions = {
                            actions.forEach {
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { /*TODO*/ },
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (s.bottom == null)
                FloatingActionButton(
                    onClick = { /*TODO*/ },
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                }
        },
    ) {
        BooksComponent(
            books = s.books,
            onSelect = { dispatch(BooksAction.Selected(it)) },
            onLongPress = { dispatch(BooksAction.LongPress(it)) },
            lazyListState = b,
            modifier = Modifier.padding(it)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BooksComponent(
    books: Books,
    onSelect: (Book) -> Unit,
    onLongPress: (Book) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier,
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        items(books.value) {
            Text(
                text = it.title,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onSelect(it) },
                        onLongClick = { onLongPress(it) },
                    )
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(58.dp)
            )
        }
    }
}

@Preview
@Composable
internal fun Preview() {
    val books = Books(buildList {
        repeat(30) {
            add(Book(id = BookId(it), title = "Book $it"))
        }
    })
    val bookNetwork = object : BookNetwork {
        override suspend fun getAll(): Either<NetworkError, Books> = books.right()
    }
    with(WithScope()) {
        BookThunkAndroid(
            repository = with(bookNetwork) { with(BookDB()) { BookRepository() } },
            nav = {},
            initialState = BookState(books.value.first())
        ).BookScreen()
    }
}