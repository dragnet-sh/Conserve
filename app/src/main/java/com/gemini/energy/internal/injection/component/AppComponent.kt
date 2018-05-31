package com.gemini.energy.internal.injection.component

import com.gemini.energy.internal.injection.DaggerApplication
import com.gemini.energy.internal.injection.module.AppModule
import com.gemini.energy.internal.injection.module.ActivitiesModule
import com.gemini.energy.internal.injection.module.DataModule
import com.gemini.energy.presentation.navigation.Navigator
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    DataModule::class,
    ActivitiesModule::class])
internal interface AppComponent : AndroidInjector<DaggerApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<DaggerApplication>()

}