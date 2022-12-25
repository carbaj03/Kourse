package com.example.myapplication.tracking

import com.example.myapplication.asynchrony.EventScreen

sealed interface AmplitudeEvent : EventScreen {
    data class User(val properties: Properties) : AmplitudeEvent
    data class Custom(val value: String, val properties: Properties? = null) : AmplitudeEvent
    data class Screen(val properties: Properties) : AmplitudeEvent
    data class Click(val properties: Properties) : AmplitudeEvent
}

fun Custom(value: String, vararg properties: Property): AmplitudeEvent.Custom =
    AmplitudeEvent.Custom(value, Properties(properties.toList()))

fun User(vararg properties: Property): AmplitudeEvent.User =
    AmplitudeEvent.User(Properties(properties.toList()))

fun Name(value: String, vararg properties: String): AmplitudeEvent.Custom =
    AmplitudeEvent.Custom(value, Properties(properties.map { Property.Name(it) }.toList()))

fun Screen(value: String): AmplitudeEvent.Screen =
    AmplitudeEvent.Screen(Properties(Property.Name(value)))

fun Screen(vararg property: Property): AmplitudeEvent.Screen =
    AmplitudeEvent.Screen(Properties(property.toList()))

fun Click(vararg property: Property): AmplitudeEvent.Click =
    AmplitudeEvent.Click(Properties(property.toList()))

fun Click(value: String): AmplitudeEvent.Click =
    AmplitudeEvent.Click(Properties(Property.Name(value)))

fun Click(screen : String, value: String): AmplitudeEvent.Click =
    AmplitudeEvent.Click(Properties(Property.Name(value)))