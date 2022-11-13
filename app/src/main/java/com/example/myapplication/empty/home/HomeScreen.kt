package com.example.myapplication.empty.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeThunk.HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
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