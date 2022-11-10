package com.example.myapplication.empty.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeThunk.HomeScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) {
        Column(Modifier.padding(it)) {
            Button(onClick = { dispatch(HomeAction.Podcasts) }) {
                Text(text = "podcast")
            }

            Button(onClick = { dispatch(HomeAction.Blogs) }) {
                Text(text = "blogs")
            }

            Button(onClick = { dispatch(HomeAction.Books) }) {
                Text(text = "Books")
            }
        }
    }
}