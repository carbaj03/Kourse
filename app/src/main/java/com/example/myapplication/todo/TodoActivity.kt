package com.example.myapplication.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.MyApplication
import com.example.myapplication.asynchrony.WithScope


class TodoActivity : AppCompatActivity() {

    val withScope = WithScope()

    val screen2: ThunkScreen = object : ThunkScreen,
        ThunkToolbar by ThunkToolbar(
            store = MyApplication.graph.store,
            withScope = withScope,
            navigator = {
                when (it) {
                    is ToolbarAction.OnBack -> {}
                    is ToolbarAction.OnClose -> {}
                    else -> {}
                }
            }
        ),
        ThunkBooks by ThunkBooks(
            store = MyApplication.graph.store,
            withScope = withScope
        ),
        ThunkPodcast by ThunkPodcast(
            store = MyApplication.graph.store,
            withScope = withScope,
            repository = MyApplication.graph.podcast
        ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme() {
                val navController = rememberNavController()
                val screen: ThunkScreen = remember {
                    object : ThunkScreen,
                        ThunkToolbar by ThunkToolbar(
                            store = MyApplication.graph.store,
                            withScope = withScope,
                            navigator = {
                                withScope.Main {
                                    when (it) {
                                        is ToolbarAction.OnBack -> navController.popBackStack()
                                        is ToolbarAction.OnClose -> finish()
                                        else -> {}
                                    }
                                }
                            }
                        ),
                        ThunkBooks by ThunkBooks(
                            store = MyApplication.graph.store
                        ),
                        ThunkPodcast by ThunkPodcast(
                            store = MyApplication.graph.store,
                            withScope = withScope,
                            repository = MyApplication.graph.podcast
                        ) {}
                }
                val home = remember {
                    object : ThunkHome by ThunkHome(
                        store = MyApplication.graph.store,
                        withScope = withScope,
                        homeNavigator = {
                            withScope.Main {
                                when (it) {
                                    is HomeAction.Blogs -> navController.navigateToBooks()
                                    is HomeAction.Bottom -> {}
                                }
                            }
                        },
                        bottomNavigator = {
                            withScope.Main {
                                when (it) {
                                    is BottomAction.OnItemClick -> navController.navigate(it.item.route)
                                }
                            }
                        },
                        tracker = {}
                    ) {}
                }

                with(home) {
                    TodoHost(
                        navController = navController,
                        thunkScreen = screen,
                        thunkScreen1 = screen2,
                    )
                }
            }
        }

        val action = intent.action
        val type = intent.type
        if ("android.intent.action.SEND" == action && type != null && "text/plain" == type) {
            Log.e("saf", intent.getStringExtra(Intent.EXTRA_SUBJECT).toString())
            Log.e("saf", intent.getStringExtra(Intent.EXTRA_TEXT).toString())
        }
    }

}


context(ThunkHome) @Composable
fun TodoHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = bottomRoute,
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