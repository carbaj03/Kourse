package com.example.myapplication

import com.example.myapplication.asynchrony.with
import com.example.myapplication.vanilla.*
import io.ktor.util.reflect.*
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
    fun useAppContext(): TestResult = runTest(dispatchTimeoutMs = 10000) {
        var jobs = mutableListOf<Job>()

        var appState = MutableStateFlow(AppState())
        val s = SideEffect {
            jobs.add(launch { it(appState.value, this) })
        }
        val n = Reducer {
            appState.value = it(appState.value)
        }
        with(s, n) {
            assertEquals(appState.value.screen, Initial)

            appState.value.init()

            appState.assertScreen<Splash> {
//                assert(appState.value.screen is Splash)
                delay(screen.duration)
                screen.action()
                advanceTimeBy(1030)
            }

            appState.assertScreen<ProfileScreen> {
//                assert(appState.value.screen is ProfileScreen)
                screen.login.action()
            }

            appState.assertScreen<LoginScreen> {
//                assert(appState.value.screen is LoginScreen)
                screen.next.action()
                advanceUntilIdle()
                screen.errors.contains(LoginScreen.Error.InvalidEmail)
                screen.email.onChange("carbaj03@gmail.com")
                screen.next.action()
                advanceUntilIdle()
            }

            appState.assertScreen<SignupPasswordScreen> {
//            assert(appState.value.screen is SignupPasswordScreen)
                screen.next.action()
                screen.errors.contains(SignupPasswordScreen.Error.InvalidPassword)
                screen.password.onChange("asdf")
                screen.next.action()
                advanceUntilIdle()
            }

            appState.assertScreen<ChatScreen> {
//                assert(appState.value.screen is ChatScreen)
                screen.receive()
                screen.toSend.onChange("a")
                when (val content = screen.content) {
                    is ChatScreen.Content.Examples -> assertEquals(
                        content,
                        ChatScreen.Content.Examples(Normal(""), Normal(""), Normal(""))
                    )
                    is ChatScreen.Content.Messages -> assert(false)
                }
                screen.send.action()
                when (val content = screen.content) {
                    is ChatScreen.Content.Examples -> assert(false)
                    is ChatScreen.Content.Messages -> assertEquals(
                        content.msg,
                        listOf(Message("Both", "a", ""))
                    )
                }
                advanceUntilIdle()
                when (val content = screen.content) {
                    is ChatScreen.Content.Examples -> assert(false)
                    is ChatScreen.Content.Messages -> assertEquals(
                        listOf(
                            Message("Both", "a", ""),
                            Message("Both", "response to a", "")
                        ),
                        content.msg
                    )
                }
            }
        }

        jobs.forEach { it.cancel() }
    }
}

operator fun <T> MutableStateFlow<AppState>.invoke(): T =
    value.screen as T

inline fun <reified A : Screen> MutableStateFlow<AppState>.assertScreen(f: ScreenBuilder<A>.() -> Unit) {
    assert(value.screen is A)
    f(object : ScreenBuilder<A> {
        override val screen: A get() = value.screen as A
    })
}

interface ScreenBuilder<A : Screen> {
    val screen: A
}
