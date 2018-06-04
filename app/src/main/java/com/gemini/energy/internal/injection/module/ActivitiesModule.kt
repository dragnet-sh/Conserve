package com.gemini.energy.internal.injection.module

import com.gemini.energy.internal.injection.module.entity.EntityModule
import com.gemini.energy.presentation.home.HomeActivity
import com.gemini.energy.internal.injection.module.home.HomeModule
import com.gemini.energy.internal.injection.scope.AuditEntityScope
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.scope.ScopeParentActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivitiesModule {

    @HomeScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeHomeActivity(): HomeActivity

    @AuditEntityScope
    @ContributesAndroidInjector(modules = [EntityModule::class])
    internal abstract fun contributeScopeParentActivity(): ScopeParentActivity
}