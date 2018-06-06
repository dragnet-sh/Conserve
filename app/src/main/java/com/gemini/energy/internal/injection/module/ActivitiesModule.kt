package com.gemini.energy.internal.injection.module

import com.gemini.energy.internal.injection.module.home.HomeModule
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.home.BaseHomeActivity
import com.gemini.energy.presentation.home.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivitiesModule {

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeBaseHomeActivity(): BaseHomeActivity

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeHomeActivity(): HomeActivity
}