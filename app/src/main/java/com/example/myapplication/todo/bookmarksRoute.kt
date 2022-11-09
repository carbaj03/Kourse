package com.example.myapplication.todo

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

const val homeRoute = "home_route"
const val booksRoute = "books_route"
const val podcastRoute = "podcast_route"
const val bottomRoute = "bottom_route"

fun NavController.navigateToBooks(navOptions: NavOptions? = null) {
    navigate(booksRoute, navOptions)
}

fun NavController.navigateToPodcast(navOptions: NavOptions? = null) {
    navigate(podcastRoute, navOptions)
}

context(ThunkHome)
fun NavGraphBuilder.bookmarksScreen(
    thunkScreen1: ThunkScreen,
    thunkScreen2: ThunkScreen,
    navController: NavHostController,
) {
    composable(route = bottomRoute) {
        Bottom(navController, thunkScreen1, thunkScreen2)
    }
    composable(route = homeRoute) {
        Screen()
    }
    composable(route = booksRoute) {
        thunkScreen1.compose()
    }
    composable(route = podcastRoute) {
        thunkScreen2.compose()
    }
}

const val booksTabRoute = "books_tab_route"
const val podcastTabRoute = "podcast_tab_route"
const val videosTabRoute = "videos_tab_route"

fun NavGraphBuilder.tabsGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "books",
    thunkScreen: ThunkScreen,
    thunkScreen1: ThunkScreen,
    thunkHome: ThunkHome
) {
    navigation(
        startDestination = booksTabRoute,
        route = "tabs"
    ) {
        composable(booksTabRoute) {

        }
        composable(podcastTabRoute) {

        }
        composable(videosTabRoute) {

        }
    }
}