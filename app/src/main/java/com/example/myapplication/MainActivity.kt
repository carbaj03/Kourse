package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import arrow.core.some
import arrow.optics.optics
import com.example.myapplication.asynchrony.Event
import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.asynchrony.dispatchAction
import com.example.myapplication.redux.Slice
import com.example.myapplication.redux.SliceName
import com.example.myapplication.redux.createSliceMutable
import com.example.myapplication.redux.select
import com.example.myapplication.redux.types.Action
import com.example.myapplication.redux.types.ActionState
import com.example.myapplication.tracking.AmplitudeEvent
import com.example.myapplication.tracking.AppsFlyerEvent
import com.example.myapplication.tracking.EventTracker
import com.fintonic.domain.commons.redux.types.*
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.utils.asynchrony.Screen
import com.fintonic.domain.utils.asynchrony.ThunkScreenCombine
import com.fintonic.domain.utils.asynchrony.ThunkScreenT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import recomposeHighlighter
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent {
//            val r = rememberCoroutineScope()
//            val a by t.state.collectAsState()
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column {
//                    Text(
//                        modifier = Modifier.clickable { t.clickaa(); t.clickb() },
//                        text = a.counter.toString(),
//                    )
//
//                    Text(
//                        modifier = Modifier.clickable { t.clickb() },
//                        text = a.counter.toString(),
//                    )
//
//                    a.other.tap {
//                        Text(text = it)
//                    }
//                }
//                if (a.isLoading)
//                    CircularProgressIndicator()
//
//            }
//        }
        t.run {
            setContent {
                val t1 by t<Test1>()
                val t2 by t<Test4>()
                val t3 by t<Test3>()

                LaunchedEffect(key1 = Unit, block = {
                    launch { delay(2.seconds); cancel() }

                })

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            modifier = Modifier.recomposeHighlighter(),
                            text = t1.t.toString(),
                        )

                        Text(
                            modifier = Modifier.recomposeHighlighter(),
                            text = t2.t.toString(),
                        )

                        Text(
                            modifier = Modifier.recomposeHighlighter(),
                            text = t3.t.toString(),
                        )

                        Button(onClick = { s1Action() }) {
                            Text(text = "action")
                        }

                        Button(onClick = { reset() }) {
                            Text(text = "Reset")
                        }
                    }
                }
            }
        }
    }
}


sealed class Router(override val route: String) : Screen
sealed interface Events : Event {
    object OnLoad : Events
}


data class Test1(val t: Int = 0) : State
data class Test4(val t: Int = 0) : State
data class Test3(val t: Int = 0) : State

sealed interface Action1 : Action {
    object Counter : Action1
}

sealed interface Action4 : Action {
    object Counter : Action4
}

sealed interface Action3 : Action {
    object Counter : Action3
}

//val s1: Slice<Test1> =
//    createSlice(initialState = Test1()) { a: Action1 ->
//        when (a) {
//            Action1.Counter -> copy(t = t + 1)
//        }
//    }
//val s4: Slice<Test4> =
//    createSlice(initialState = Test4()) { a: Action4 ->
//        when (a) {
//            Action4.Counter -> copy(t = t + 1)
//        }
//    }
//val s3: Slice<Test3> =
//    createSlice(initialState = Test3()) { a: Action3 ->
//        when (a) {
//            Action3.Counter -> copy(t = t + 1)
//        }
//    }

val s1: Slice<Test1> = createSliceMutable(initialState = Test1())
val s4: Slice<Test4> = createSliceMutable(initialState = Test4())
val s3: Slice<Test3> = createSliceMutable(initialState = Test3())

sealed class Actions : Action

val amplitude = EventTracker<AmplitudeEvent> {
    when (it) {
        is AmplitudeEvent.Click -> Log.e("Amplitude", "$it")
        is AmplitudeEvent.Custom -> Log.e("Amplitude", "$it")
        is AmplitudeEvent.Screen -> Log.e("Amplitude", "$it")
        is AmplitudeEvent.User -> Log.e("Amplitude", "$it")
    }
}

val appsFlyer = EventTracker<AppsFlyerEvent> {
    when (it) {
        is AppsFlyerEvent.Custom -> Log.e("appsFlyer", "$it")
    }
}

//val t: ThunkScreenT<Test> = ThunkScreenT(
//    initialState = Test(),
//    navigator = ThunkNavigator<Router> { },
//    withScope = WithScope(),
//    f = {
//        addTracker<Events, AmplitudeEvent>(amplitude) {
//            when (this) {
//                Events.OnLoad -> Screen("asdf")
//            }
//        }
//
//        addTracker<Events, AppsFlyerEvent>(appsFlyer) {
//            when (this) {
//                Events.OnLoad -> AppsFlyerEvent.Custom("asdf")
//            }
//        }
//    }
//)

val t: ThunkScreenT<CombineState> = ThunkScreenCombine<Router, Events>(
    s1, s4, s3,
    navigator = { },
    withScope = WithScope(),
    eventTracker = arrayOf()
)


//val store = configureStore(s1, s2, s3)

fun ThunkScreenT<CombineState>.reset(): Unit =
    dispatchAction {
        s1.dispatch {
            copy(t = 0)
        }
        s4.dispatch {
            copy(t = 0)
        }
        s3.dispatch {
            copy(t = 0)
        }
    }

//fun ThunkScreenT<CombineState>.s1Action() =
//    dispatchAction {
//        Action1.Counter.dispatch()
//        Action4.Counter.dispatch()
//        Action3.Counter.dispatch()
//    }

fun ThunkScreenT<CombineState>.d1Action(): Unit =
    dispatchAction("4") {
        delay(1000)
        s1.dispatch {
            copy(t = t + 100)
        }
    }

fun ThunkScreenT<CombineState>.s1Action(): Unit =
    dispatchAction(key = "asdf") {
//    eitherIo<String, String>(onError = {}) {

        d1Action()

        val a = asyncIo { delay(1000); 1 }
        val b = asyncIo { delay(1000); 1 }


        val r = a.await()
        s1.dispatch {
            copy(t = t + r)
        }

        s3.dispatch {
            copy(t = t + 1)
        }

        s4.dispatch {
            copy(t = t + 1)
        }

        delay(2000)
        println("Complete : s4-1")
        s4.dispatch {
            copy(t = t + 1)
        }
        println("Complete : s4-2")
        delay(2000)
        s4.dispatch {
            copy(t = t + 1)
        }
//    }.cancel()
    }

context(Store<CombineState>)
fun s2Action() =
    s4.dispatch {
        copy(t = 1)
    }

context(Store<CombineState>)
fun s3Action() =
    s3.dispatch {
        copy(t = 3)
    }


context(Store<S>)
fun <S : State> dispatch(f: S.() -> S) {
    ActionState(f).dispatch()
}

context(Store<CombineState>)
fun <S : State> Slice<S>.dispatch(f: S.() -> S) {
    ActionState(f).dispatch()
}

context(Store<CombineState>)
inline  fun <reified S : State> slice(noinline f: S.() -> S) {
    ActionState(f).dispatch()
}


context(Dispatcher) inline val <S> (() -> S).mutable
    get() = this()

fun ThunkScreenT<Test>.clickad(): Unit =
    dispatch {
        copy(
            isLoading = true,
            other = "a".some(),
            counter = 1
        )
    }

fun ThunkScreenT<Test>.clickaa(): Unit =
    dispatchAction {
//        "".right().bind()

        Events.OnLoad.dispatch()

        dispatch {
            copy(
                isLoading = true,
                other = "a".some(),
                counter = 1
            )
        }

        delay(2200)

        dispatch {
            copy(counter = counter + 2)
        }

//        Test.isLoading set true
//
//        Test.other set None
//
//        delay(1000)
//
//        Test.counter set this().counter + 2
//        Test.isLoading set false
//
//        delay(2000)
//        Test.other set "42".some()
    }

fun Store<Test>.load(): Unit =
    dispatch {
        copy(
            isLoading = !isLoading,
            other = "b".some(),
            counter = counter + 2
        )
    }

fun ThunkScreenT<Test>.clickb(): Unit =
    dispatchAction {
        "".right().bind()

        delay(1200)
        load()

        delay(1200)
        dispatch {
            copy(
                isLoading = false,
                other = "b1".some(),
                counter = counter + 2
            )
        }

//        Test.isLoading set true
//
//        Test.other set None
//
//        delay(1200)
//
//        Test.counter set this().counter + 2
//        Test.isLoading set false
//
//        delay(2000)
//        Test.other set "42".some()
    }


//context(Copy<A>, Store<A>)
//        infix fun <A : State, B> Setter<A, B>.set(b: B?) {
//    val current = this.set(state.value, b)
//    dispatch(ActionState(current))
//}


//val other: Lens<Test, String?> = Lens(
//    get = { it.other },
//    set = { test, other -> test.copy(other = other) }
//)

@optics
data class Test(
    val isLoading: Boolean = false,
    val counter: Int = 0,
    val other: Option<String> = None
) : State {
    companion object
}

@optics
data class Test2(
    val other: String? = null
) {
    companion object
}

//fun main() {
//    val test2 = Test2()
//    Test2.other.modify(test2) { null }
//
//    val test = Test()
//    Test.other.modify(test) { None }
//}


@Composable
inline operator fun <reified S : State, A> Store<CombineState>.invoke(
    crossinline f: S.() -> A
): androidx.compose.runtime.State<A> =
    remember(state.value.select()!!) { derivedStateOf { state.value.select<S>()?.let(f) ?: throw Exception("Implement slice") } }


@Composable
inline operator fun <reified S : State>  Store<CombineState>.invoke(): androidx.compose.runtime.State<S> =
    remember(state.value.select()!!) { derivedStateOf { state.value.select() ?: throw Exception("Implement slice") } }