package com.example.myapplication.empty.book

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun BooksThunk.BooksHomeScreen(modifier: Modifier, lazyListState: LazyListState) {
    LaunchedEffect(Unit) {
        dispatch(BooksAction.Load)
    }

    val booksState: BooksState by state.collectAsState()

    BooksComponent(
        books = booksState.books,
        onSelect = { dispatch(BooksAction.Selected(it)) },
        onLongPress = { dispatch(BooksAction.LongPress(it)) },
        lazyListState = lazyListState,
        modifier = modifier
    )
}