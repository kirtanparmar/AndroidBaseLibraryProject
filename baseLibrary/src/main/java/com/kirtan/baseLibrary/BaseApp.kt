package com.kirtan.baseLibrary

import android.app.Application
import timber.log.Timber

open class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.d("onCreate")
    }
}