package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import arrow.optics.optics
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.asynchrony.dispatchAction
import com.example.myapplication.redux.createStore
import com.example.myapplication.redux.types.ActionState
import com.example.myapplication.redux.types.reducerType
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.commons.redux.types.Store
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

class OpticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Thunk().compose()
        }
    }
}

@Composable
fun Thunk.compose() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val screenState by state.collectAsState()

        LaunchedEffect(Unit) {
            load()
        }

        Column {
            Button(onClick = { increase() }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            }
            Text(
                text = screenState.counter.toString(),
                fontSize = 20.sp
            )
            Button(onClick = { decrease() }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }

        if (screenState.isLoading) {
            CircularProgressIndicator()
        }
    }
}



@optics
data class OpticsState(
    val counter: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}



interface ThunkProvider<S : State> : WithScope, Store<S> {
    override val state: StateFlow<S>
}

interface Thunk : ThunkProvider<OpticsState> {
    fun load()
    fun increase()
    fun decrease()

    override val state: StateFlow<OpticsState>
}

val store: Store<OpticsState> by lazy {
    createStore(
        reducerType<OpticsState, ActionState<OpticsState>> { action -> action(this) },
        initialState = OpticsState(),
    )
}

fun Thunk(
    withScope: WithScope = WithScope(),
): Thunk =
    object : Thunk,
        WithScope by withScope,
        Store<OpticsState> by store {

        fun loading(): Unit =
            dispatch { copy(isLoading = true) }

        override fun load(): Unit =
            dispatchAction{
                repeat(10) {
                    dispatch { copy(counter = 10) }
                    delay(2.seconds)
                }
            }

        override fun increase(): Unit =
            dispatchAction{
                loading()
                delay(1000)
                dispatch { copy(counter = counter + 1, isLoading = false) }
            }

        override fun decrease(): Unit =
            dispatchAction{
                loading()
                delay(1000)
                dispatch { copy(counter = counter - 1, isLoading = false) }
            }

        context(WithScope, Store<S>)
        fun <S : State> dispatchState(f: S.() -> S): Unit =
            dispatchAction{
                dispatch { f() }
            }

    }
