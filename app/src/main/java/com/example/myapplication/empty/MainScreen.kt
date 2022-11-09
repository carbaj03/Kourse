package com.example.myapplication.empty

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

context(HomeThunk, BooksThunk)
        @OptIn(ExperimentalMaterial3Api::class)
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        @Composable
        operator fun MainThunk.invoke(b1: LazyListState) {
    LaunchedEffect(Unit) {
        dispatch(MainAction.Load)
    }

    val s: MainState by state.collectAsState()

    BackHandler(s.selectedTab != Tab.Home) {
        dispatch(MainAction.ChangeTab(Tab.Home))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.title) },
                navigationIcon = {
                    IconButton(onClick = { dispatch(MainAction.Account) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                    }
                },
                actions = {}
            )
        },
        bottomBar = {
            BottomAppBar {
                s.tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        alwaysShowLabel = true,
                        label = { Text(screen.title) },
                        selected = s.selectedTab == screen,
                        onClick = { dispatch(MainAction.ChangeTab(screen)) }
                    )
                }
            }
        }
    ) {
        when (s.selectedTab) {
            Tab.Home -> HomeScreen(Modifier.padding(it))
            Tab.Podcast -> PodcastScreen(Modifier.padding(it))
            Tab.Blogs -> BlogsScreen(Modifier.padding(it))
            Tab.Books -> BooksScreen(Modifier.padding(it), b1)
        }
    }
}