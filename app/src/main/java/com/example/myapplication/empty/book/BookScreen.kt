package com.example.myapplication.empty.book

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapplication.empty.book.BookAction
import com.example.myapplication.empty.book.BookState
import com.example.myapplication.empty.book.BookThunk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookThunk.BookScreen() {
    val s: BookState by state.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(text = "Detail") },
                navigationIcon = {
                    IconButton(onClick = { dispatch(BookAction.Back) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {}
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    if (!s.isEditing) {
                        IconButton(onClick = { dispatch(BookAction.Edit) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = { dispatch(BookAction.Confirm) }) {
                            Icon(Icons.Default.Done, contentDescription = null)
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
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (s.isEditing) {
                TextField(
                    value = s.newName,
                    onValueChange = { dispatch(BookAction.ChangeName(it)) }
                )
            } else {
                Text(text = s.book.title)
            }
        }
    }
}