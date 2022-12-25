package com.example.myapplication.todo

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import recomposeHighlighter
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


context(ThunkHome)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {

    LaunchedEffect(Unit) {
        HomeAction.Load()
    }

    Scaffold(
        topBar = {
            ToolbarComponent(modifier = Modifier.recomposeHighlighter())
        }
    ) {
        Column(Modifier.padding(it)) {
            Button(onClick = { HomeAction.Podcast() }) {
                Text(text = "podcast")
            }

            Button(onClick = { HomeAction.Blogs() }) {
                Text(text = "blogs")
            }
        }
    }
}
//
//sealed class Screen(val route: String, @StringRes val resourceId: Int) {
//    object Profile : Screen("profile", R.string.profile)
//    object FriendsList : Screen("friendslist", R.string.friends_list)
//}
//
//val items = listOf(
//    Screen.Profile,
//    Screen.FriendsList,
//)

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T?>.collectAsState(
    initial: T,
    context: CoroutineContext = EmptyCoroutineContext
): State<T> =
    collectAsStateNull(value ?: initial, context)

@Composable
fun <T : R, R> Flow<T?>.collectAsStateNull(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext
): State<R> =
    produceState(initial, this, context) {
        if (context == EmptyCoroutineContext) {
            collect { it?.let { value = it } }
        } else withContext(context) {
            collect { it?.let { value = it } }
        }
    }

context(ThunkHome)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraphBuilder.Bottom(
    navController: NavHostController,
    thunkScreen: ThunkScreen,
    thunkScreen1: ThunkScreen,
) {
    val state: BottomState by bottom.collectAsState(initial = BottomState())

    LaunchedEffect(Unit) {
        HomeAction.Load()
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                state.list.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = state.selected == screen,
                        onClick = { action(BottomAction.OnItemClick(screen)) }
                    )
                }
            }
        }
    ) { innerPadding ->
//        tabsGraph(
//            navController = navController,
//            thunkScreen = thunkScreen,
//            thunkScreen1 = thunkScreen1,
//            thunkHome = this@ThunkHome,
//            modifier = Modifier.padding(innerPadding)
//        )
        NavHost(navController, startDestination = booksTabRoute, modifier = Modifier.padding(innerPadding)) {
            composable(booksTabRoute) {

            }
            composable(podcastTabRoute) {

            }
            composable(videosTabRoute) {

            }
        }
    }
}

context(ThunkHome) @Composable
fun BottomHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute,
    thunkScreen: ThunkScreen,
    thunkScreen1: ThunkScreen,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        bookmarksScreen(thunkScreen, thunkScreen1, navController)
    }
}