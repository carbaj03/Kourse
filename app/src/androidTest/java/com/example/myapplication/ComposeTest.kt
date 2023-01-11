package com.example.myapplication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun useAppContext() {
        val repository = object : RandomRepositoryMock {
            override var error: Boolean = false
        }
        rule.setContent { App(repository) }
        rule.onNodeWithText("Splash").assertExists()
        rule.mainClock.advanceTimeBy(1020)
        rule.onNodeWithText("0").assertExists()
        rule.onNodeWithText("Increment").performClick()
        rule.onNodeWithText("1").assertExists()
        rule.onNodeWithText("Increment").performClick()
        rule.onNodeWithText("2").assertExists()
        rule.onNodeWithText("Restart").performClick()
        rule.onNodeWithText("0").assertExists()
        repository.error = true
        rule.onNodeWithText("Random").performClick()
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag("loader").assertExists()
        rule.mainClock.advanceTimeBy(1020)
        rule.onNodeWithTag("loader").assertDoesNotExist()
        rule.onNodeWithText("Bad number").assertExists()
    }
}