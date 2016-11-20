package com.lifepath.swaribrus

import android.app.Application
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        println("App Created...")
        // Configure Kovenant with standard dispatchers
        // suitable for an Android environment.
        // It's just convenience, you can still use
        // `Kovenant.configure { }` if you want to keep
        // matters in hand.
        startKovenant()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Dispose of the Kovenant thread pools.
        // For quicker shutdown you could use
        // `force=true`, which ignores all current
        // scheduled tasks
        stopKovenant()
    }
}
