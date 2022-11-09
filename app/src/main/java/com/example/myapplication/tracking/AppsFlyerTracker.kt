package com.example.myapplication.tracking


suspend operator fun EventTracker<AppsFlyerEvent>.invoke(event: String) {
    invoke(AppsFlyerEvent.Custom(event))
}
