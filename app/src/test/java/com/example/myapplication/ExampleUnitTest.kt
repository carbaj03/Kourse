package com.example.myapplication

import com.example.myapplication.asynchrony.with
import com.example.myapplication.vanilla.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {


    @Test
    fun addition_isCorrect() = runTest {
//        val app = AppState(screen = Splash(SplashAction()))
//        val msm = Message("a", "a", "")
//
//        sendMessage("a", "a")
//
//        var l = mutableListOf<Message>()
//        val job = launch() {
//            receiveSms().toList(l)
//        }
//        assertEquals(msm, l.first())
//        job.cancel()
    }

    @Test
    fun useAppContext(): TestResult = runTest {

        var appState = MutableStateFlow(AppState())

        var jobs = mutableListOf<Job>()
        val s = SideEffect {
            jobs.add(launch { it(appState.value) })
        }
        val n = Reducer {
            appState.value = it(appState.value)
        }
        with(s, n) {
            assertEquals(appState.value.screen, Initial)
            appState.value.init()
            assert(appState.value.screen is Splash)
            (appState.value.screen as Splash).run {
                delay(duration)
                action()
            }
            advanceTimeBy(12030)
            assert(appState.value.screen is ProfileScreen)
            (appState.value.screen as ProfileScreen).login.action()
            assert(appState.value.screen is LoginScreen)
            (appState.value.screen as LoginScreen).next.action()
            assert(appState.value.screen is SignupPasswordScreen)
            (appState.value.screen as SignupPasswordScreen).next.action()
            assert(appState.value.screen is ChatScreen)
            (appState.value.screen as ChatScreen).receive()
            (appState.value.screen as ChatScreen).toSend.onChange("a")
            when (val content = (appState.value.screen as ChatScreen).content) {
                is ChatScreen.Content.Examples -> assertEquals(content, ChatScreen.Content.Examples(Normal(""), Normal(""), Normal("")))
                is ChatScreen.Content.Messages -> assert(false)
            }
            (appState.value.screen as ChatScreen).send.action()
            when (val content = (appState.value.screen as ChatScreen).content) {
                is ChatScreen.Content.Examples -> assert(false)
                is ChatScreen.Content.Messages -> assertEquals(content.msg, listOf(Message("Both", "a", "")))
            }
            advanceUntilIdle()
            when (val content = (appState.value.screen as ChatScreen).content) {
                is ChatScreen.Content.Examples -> assert(false)
                is ChatScreen.Content.Messages -> assertEquals(listOf(Message("Both", "a", ""), Message("Both", "response to a", "")), content.msg)
            }

            jobs.forEach { it.cancel() }
        }
    }
}