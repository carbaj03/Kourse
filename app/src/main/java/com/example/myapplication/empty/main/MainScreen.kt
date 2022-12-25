package com.example.myapplication.empty.main

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.empty.blog.BlogsScreen
import com.example.myapplication.empty.book.BooksHomeScreen
import com.example.myapplication.empty.book.BooksThunk
import com.example.myapplication.empty.home.HomeScreen
import com.example.myapplication.empty.home.HomeThunk
import com.example.myapplication.empty.podcast.PodcastScreen
import com.example.myapplication.empty.video.VideoScreen
import kotlinx.coroutines.launch
import recomposeHighlighter

context(HomeThunk, BooksThunk)
        @OptIn(ExperimentalMaterial3Api::class)
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        @Composable
fun MainThunk.MainScreen(
    booksLazyListState: LazyListState,
) {
    LaunchedEffect(Unit) {
        dispatch(MainAction.Load)
    }
    
    val mainState: MainState by state.collectAsState()
    
    BackHandler(mainState.selectedTab != Tab.Home) {
        dispatch(MainAction.ChangeTab(Tab.Home))
    }
    
    Drawer(
        logOut = { dispatch(MainAction.LogOut) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = mainState.title) },
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
                    mainState.tabs.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Filled.Favorite, contentDescription = null) },
                            alwaysShowLabel = true,
                            label = { Text(screen.title) },
                            selected = mainState.selectedTab == screen,
                            onClick = { dispatch(MainAction.ChangeTab(screen)) }
                        )
                    }
                }
            }
        ) {
            when (mainState.selectedTab) {
                Tab.Home -> HomeScreen(Modifier.padding(it))
                Tab.Podcast -> PodcastScreen(Modifier.padding(it))
                Tab.Blogs -> BlogsScreen(Modifier.padding(it))
                Tab.Books -> BooksHomeScreen(Modifier.padding(it), booksLazyListState)
                Tab.Videos -> VideoScreen(Modifier.padding(it))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    logOut: () -> Unit,
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(items[0]) }
    
    ModalNavigationDrawer(
        modifier = Modifier
            .fillMaxHeight()
            .background(
                color = NavigationDrawerItemDefaults
                    .colors()
                    .containerColor(selected = false).value
            ),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
//                    .verticalScroll(rememberScrollState())
            ) {
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item, contentDescription = null) },
                        label = {
                            Row {
                                Text(item.name)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "22")
                            }
                        },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                }
                Spacer(Modifier.padding(16.dp))
                Divider()
                Text(text = "sadf", Modifier.padding(16.dp))
                repeat(3) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        label = {
                            Text("Notifications", style = MaterialTheme.typography.labelMedium)
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .recomposeHighlighter(),
                        badge = { Text(text = "22") }
                    )
                }
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = {
                        Text("Setting", style = MaterialTheme.typography.labelMedium)
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .recomposeHighlighter(),
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = {
                        Text("Log out", style = MaterialTheme.typography.labelMedium)
                    },
                    selected = false,
                    onClick = {
                        logOut()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .recomposeHighlighter(),
                )
            }
        },
        content = content
    )
}
