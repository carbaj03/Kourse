package com.example.myapplication.tracking


@JvmInline
value class Properties(val value: List<Property>) {
    fun toMap(): HashMap<String, String> =
        HashMap(value.associate { it.pair() })
}

fun Properties(vararg property: Property): Properties =
    Properties(property.toList())

sealed class Property(open val value: String, open val event: String) {
    data class Name(override val event: String) : Property("Nombre", event)
    data class Status(override val event: String) : Property("Estado", event)
    data class Success(override val event: String) : Property("Success", event)
    data class Type(override val event: String) : Property("Tipo", event)
    data class Amount(override val event: String) : Property("amount", event)
    data class Category(override val event: String) : Property("Category", event)

    data class Custom(override val value: String, override val event: String) : Property(value, event)

    fun pair(): Pair<String, String> =
        value to event

    val properties: Properties
        get() = Properties(this)
}

suspend operator fun EventTracker<AmplitudeEvent>.invoke(value: String, property: Property? = null) {
    property?.let {
        invoke(Custom(value, property))
    } ?: invoke(Custom(value))
}