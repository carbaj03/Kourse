package com.example.myapplication

import com.example.myapplication.vanilla.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {


    @Test
    fun addition_isCorrect() = runTest {
        val app = App()
        val msm = Message("a", "a", "")

        sendMessage("a", "a")

        var l = mutableListOf<Message>()
        val job = launch() {
            receiveSms().toList(l)
        }
        assertEquals(msm, l.first())
        job.cancel()
    }

//    @Test
//    fun useAppContext() = runTest {
//
//        val app: AppState = AppState()
//
//        assertEquals(app.screen , Splash)
////        rule.onNodeWithText("Splash").assertExists()
//
//        delay(1020)
//
//        rule.onNodeWithText("Profile").assertExists()
//
//        rule.mainClock.advanceTimeByFrame()
//
//        rule.onNodeWithText("Continue").performClick()
//
//        rule.mainClock.advanceTimeByFrame()
//
//        rule.onNodeWithText("Signup - Password", ignoreCase = false).assertExists()
//
//        rule.mainClock.advanceTimeByFrame()
//
//        rule.onNodeWithText("Continue").performClick()
//
//        rule.mainClock.advanceTimeByFrame()
//
//        rule.onNodeWithText("Chat").assertExists()
//    }
}