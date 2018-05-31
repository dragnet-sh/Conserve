package com.gemini.energy.internal.injection.module

import android.content.Context
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.internal.AppSchedulers
import com.gemini.energy.internal.injection.DaggerApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class AppModule {

    @Provides
    @Singleton
    internal fun provideContext(application: DaggerApplication): Context = application.applicationContext

    @Provides
    @Singleton
    internal fun provideSchedulers(): Schedulers = AppSchedulers()

}