package com.example.myapplication.empty.podcast

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PodcastScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Podcast")
    }
}