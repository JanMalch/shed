package com.example.app

import android.app.Application
import android.util.Log
import io.github.janmalch.shed.Shed
import timber.log.Timber

class ShedDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.plant(Shed.createTree(
            context = this,
            filter = { priority, _, _, _ ->
                // When you click the Verbose button, no entry will be in the database
                // but in Logcat thanks to the Timber.DebugTree from above.
                priority > Log.VERBOSE
            }
        ))
    }
}