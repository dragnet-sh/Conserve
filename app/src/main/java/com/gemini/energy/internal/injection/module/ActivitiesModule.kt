package com.gemini.energy.internal.injection.module

import com.gemini.energy.internal.injection.module.home.HomeModule
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.home.BaseActivity
import com.gemini.energy.presentation.home.AuditActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivitiesModule {

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeBaseHomeActivity(): BaseActivity

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeHomeActivity(): AuditActivity
}