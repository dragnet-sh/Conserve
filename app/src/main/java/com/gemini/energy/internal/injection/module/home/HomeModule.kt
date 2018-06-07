package com.gemini.energy.internal.injection.module.home

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.interactor.AuditGetAllUseCase
import com.gemini.energy.domain.interactor.AuditSaveUseCase
import com.gemini.energy.domain.interactor.ZoneGetAllUseCase
import com.gemini.energy.domain.interactor.ZoneSaveUseCase
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditFragment
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneCreateViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListViewModel
import com.gemini.energy.presentation.audit.dialog.AuditCreateViewModel
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.AuditListViewModel
import com.gemini.energy.presentation.home.Crossfader
import com.gemini.energy.presentation.home.GmailStyleCrossFadeSlidingPaneLayout
import com.gemini.energy.presentation.util.Navigator
import com.mobsandgeeks.saripaar.Validator
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector


@Module
internal abstract class HomeModule {


    @ContributesAndroidInjector
    internal abstract fun contributeAuditListFragment(): AuditListFragment

    @ContributesAndroidInjector
    internal abstract fun contributeAuditDialogFragment(): AuditDialogFragment

    @ContributesAndroidInjector
    internal abstract fun contributePreAuditFragment(): PreAuditFragment

    @ContributesAndroidInjector
    internal abstract fun contributeZoneListFragment(): ZoneListFragment

    @ContributesAndroidInjector
    internal abstract fun contributeZoneDialogFragment(): ZoneDialogFragment

    @Module
    companion object {

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideValidator(context: Context): Validator = Validator(context)

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideNavigator(context: Context): Navigator = Navigator(context)

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideCrossfader(): Crossfader<GmailStyleCrossFadeSlidingPaneLayout> {
            return Crossfader<GmailStyleCrossFadeSlidingPaneLayout>()
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideAuditSaveUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                AuditSaveUseCase {
            return AuditSaveUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideAuditGetAllUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                AuditGetAllUseCase {
            return AuditGetAllUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideZoneGetAllUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                ZoneGetAllUseCase {
            return ZoneGetAllUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideZoneSaveUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                ZoneSaveUseCase {
            return ZoneSaveUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideViewModelFactory(

                context: Context,
                auditGetAllUseCase: AuditGetAllUseCase,
                auditSaveUseCase: AuditSaveUseCase,
                zoneGetAllUseCase: ZoneGetAllUseCase,
                zoneSaveUseCase: ZoneSaveUseCase

        ): ViewModelProvider.Factory {

            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return when {

                        modelClass.isAssignableFrom(AuditListViewModel::class.java) ->
                                AuditListViewModel(context, auditGetAllUseCase) as T

                        modelClass.isAssignableFrom(AuditCreateViewModel::class.java) ->
                                AuditCreateViewModel(context, auditSaveUseCase) as T

                        modelClass.isAssignableFrom(ZoneListViewModel::class.java) ->
                                ZoneListViewModel(context, zoneGetAllUseCase) as T

                        modelClass.isAssignableFrom(ZoneCreateViewModel::class.java) ->
                                ZoneCreateViewModel(context, zoneSaveUseCase) as T

                        else -> throw IllegalArgumentException("Unknown ViewModel class : ${modelClass.name}")
                    }
                }
            }

        }
    }
}