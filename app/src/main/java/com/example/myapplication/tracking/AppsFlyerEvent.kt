package com.example.myapplication.tracking

import com.example.myapplication.asynchrony.Event

sealed interface AppsFlyerEvent : Event {
    data class Custom(val value: String) : AppsFlyerEvent
}

fun EventTracker<AppsFlyerEvent>.Custom(value: String): AppsFlyerEvent =
    AppsFlyerEvent.Custom(value)