package com.example.myapplication.empty.book

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.todo.Book
import com.example.myapplication.todo.Books

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksThunk.BooksScreen(b1: Modifier, b: LazyListState) {
    LaunchedEffect(Unit) {
        dispatch(BooksAction.Load)
    }

    val s: BooksState by state.collectAsState()

    Scaffold(
        modifier = b1,
        topBar = {
            s.toolbar?.run {
                androidx.compose.material3.TopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {}
                )
            }
        },
        bottomBar = {
            s.bottom?.run {
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                )
            }
        }
    ) {
        BooksComponent(
            books = s.books,
            onSelect = { dispatch(BooksAction.Selected(it)) },
            b = b
        )
    }
}

@Composable
fun BooksComponent(
    books: Books,
    onSelect: (Book) -> Unit,
    b: LazyListState
) {
    LazyColumn(
        state = b
    ) {
        items(books.value) {
            Text(
                text = it.title,
                modifier = Modifier
                    .clickable { onSelect(it) }
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(58.dp)
            )
        }
    }
}