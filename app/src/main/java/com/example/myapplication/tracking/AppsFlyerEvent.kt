package com.example.myapplication.tracking

import com.example.myapplication.asynchrony.EventScreen


sealed interface AppsFlyerEvent : EventScreen {
    data class Custom(val value: String) : AppsFlyerEvent
}

fun EventTracker<AppsFlyerEvent>.Custom(value: String): AppsFlyerEvent =
    AppsFlyerEvent.Custom(value)