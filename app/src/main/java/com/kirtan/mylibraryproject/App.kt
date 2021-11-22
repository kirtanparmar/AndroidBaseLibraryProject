package com.kirtan.mylibraryproject

import android.app.Application
import com.kirtan.mylibraryproject.apis.RetrofitHelper
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