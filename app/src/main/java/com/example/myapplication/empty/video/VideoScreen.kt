package com.example.myapplication.empty.video

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VideoScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Video")
    }
}