package com.example.app

import android.app.Application
import com.github.janmalch.shed.Shed
import timber.log.Timber

class ShedDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.plant(Shed.createTree(context = this))
    }
}