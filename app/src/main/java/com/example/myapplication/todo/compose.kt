package com.example.myapplication.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import recomposeHighlighter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThunkScreen.compose() {

    LaunchedEffect(Unit) {
        load()
    }

    Scaffold(
        topBar = {
            ToolbarComponent(modifier = Modifier.recomposeHighlighter())
        }
    ) {
        Column(Modifier.padding(it)) {
            BooksComponent(
                modifier = Modifier
                    .weight(1f)
                    .recomposeHighlighter()
            )

            PodcastComponent(
                modifier = Modifier
                    .weight(1f)
                    .recomposeHighlighter()
            )
        }
    }
}