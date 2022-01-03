package com.kirtan.baseLibrarySample

import android.app.Application
import com.kirtan.baseLibrarySample.apis.RetrofitHelper
import timber.log.Timber
import java.lang.ref.WeakReference

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitHelper.app = WeakReference(this)
        Timber.plant(Timber.DebugTree())
        Timber.i("onCreate: ")
    }
}