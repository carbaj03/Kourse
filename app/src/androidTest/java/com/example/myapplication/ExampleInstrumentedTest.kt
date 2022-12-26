package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.vanilla.App
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val rule = createComposeRule().apply { mainClock.autoAdvance = false }

    @Test
    fun useAppContext() {
        rule.setContent {
            App()
        }

        rule.onNodeWithText("Splash").assertExists()

        rule.mainClock.advanceTimeBy(1020)

        rule.onNodeWithText("Profile").assertExists()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText("Continue").performClick()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText("Signup - Password", ignoreCase = false).assertExists()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText("Continue").performClick()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText("Chat").assertExists()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithTag("messageToSend").performTextInput("Hi")

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithTag("sendMessage").performClick()
        rule.onNodeWithTag("sendMessage").performClick()

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithTag("messageToSend").performTextInput("")

        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText("Hi").assertExists()

    }

}