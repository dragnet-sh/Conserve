package com.gemini.energy.internal.injection.module

import com.gemini.energy.internal.injection.module.home.HomeModule
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.audit.AuditActivity
import com.gemini.energy.presentation.zone.TypeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivitiesModule {

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeBaseHomeActivity(): BaseActivity

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeAuditActivity(): AuditActivity

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeTypeActivity(): TypeActivity

}