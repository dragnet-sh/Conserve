package com.gemini.energy

import com.gemini.energy.internal.injection.DaggerApplication
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber

class App : DaggerApplication() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Timber.plant(Timber.DebugTree())
        RxJavaPlugins.setErrorHandler({Timber.e(it)})
    }
}