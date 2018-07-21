package com.gemini.energy.internal.injection.module.home

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.interactor.*
import com.gemini.energy.internal.injection.scope.HomeScope
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditCreateViewModel
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditDeleteViewModel
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditFragment
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditGetViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneCreateViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListViewModel
import com.gemini.energy.presentation.audit.dialog.AuditCreateViewModel
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.AuditListViewModel
import com.gemini.energy.presentation.base.BaseFormFragment
import com.gemini.energy.presentation.base.Crossfader
import com.gemini.energy.presentation.base.GmailStyleCrossFadeSlidingPaneLayout
import com.gemini.energy.presentation.util.Navigator
import com.gemini.energy.presentation.type.dialog.TypeCreateViewModel
import com.gemini.energy.presentation.type.feature.FeatureCreateViewModel
import com.gemini.energy.presentation.type.feature.FeatureDataFragment
import com.gemini.energy.presentation.type.feature.FeatureGetViewModel
import com.gemini.energy.presentation.type.list.TypeListFragment
import com.gemini.energy.presentation.type.list.TypeListViewModel
import com.gemini.energy.service.*
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

    @ContributesAndroidInjector
    internal abstract fun contributeTypeListFragment(): TypeListFragment

    @ContributesAndroidInjector
    internal abstract fun contributeFeatureDataFragment(): FeatureDataFragment

    @ContributesAndroidInjector
    internal abstract fun contributeBaseFormFragment(): BaseFormFragment

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
        internal fun provideZoneTypeGetAllUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                ZoneTypeGetAllUseCase {
            return ZoneTypeGetAllUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideZoneTypeSaveUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                ZoneTypeSaveUseCase {
            return ZoneTypeSaveUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideFeatureSaveUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                FeatureSaveUseCase {
            return FeatureSaveUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideFeatureGetAllUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                FeatureGetAllUseCase {
            return FeatureGetAllUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideFeatureGetAllByTypeUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                FeatureGetAllByTypeUseCase {
            return FeatureGetAllByTypeUseCase(schedulers, auditGateway)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideFeatureDeleteUseCase(schedulers: Schedulers, auditGateway: AuditGateway):
                FeatureDeleteUseCase {
            return FeatureDeleteUseCase(schedulers, auditGateway)
        }


        //**** Energy Services **** //

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideEnergyUtility(context: Context): EnergyUtility {
            return EnergyUtility(context)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideEnergyUsage(): EnergyUsage {
            return EnergyUsage()
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideOutgoingRows(context: Context): OutgoingRows {
            return OutgoingRows(context)
        }

        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideEnergyService(schedulers: Schedulers, auditGateway: AuditGateway,
                                          energyUtility: EnergyUtility, energyUsage: EnergyUsage, outgoingRows: OutgoingRows):
                EnergyService {
            return EnergyService(schedulers, auditGateway, energyUtility, energyUsage, outgoingRows)
        }

        //**** End :: Energy Services **** //


        @HomeScope
        @Provides
        @JvmStatic
        internal fun provideViewModelFactory(

                context: Context,

                auditGetAllUseCase: AuditGetAllUseCase,
                auditSaveUseCase: AuditSaveUseCase,

                zoneGetAllUseCase: ZoneGetAllUseCase,
                zoneSaveUseCase: ZoneSaveUseCase,

                zoneTypeGetAllUseCase: ZoneTypeGetAllUseCase,
                zoneTypeSaveUseCase: ZoneTypeSaveUseCase,

                featureSaveUseCase: FeatureSaveUseCase,
                featureGetAllUseCase: FeatureGetAllUseCase,
                featureGetAllByTypeUseCase: FeatureGetAllByTypeUseCase,
                featureDeleteUseCase: FeatureDeleteUseCase


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

                        modelClass.isAssignableFrom(TypeListViewModel::class.java) ->
                            TypeListViewModel(context, zoneTypeGetAllUseCase) as T

                        modelClass.isAssignableFrom(TypeCreateViewModel::class.java) ->
                            TypeCreateViewModel(context, zoneTypeSaveUseCase) as T

                        modelClass.isAssignableFrom(PreAuditCreateViewModel::class.java) ->
                            PreAuditCreateViewModel(context, featureSaveUseCase,
                                    featureGetAllUseCase, featureDeleteUseCase) as T

                        modelClass.isAssignableFrom(PreAuditGetViewModel::class.java) ->
                            PreAuditGetViewModel(context, featureGetAllUseCase) as T

                        modelClass.isAssignableFrom(PreAuditDeleteViewModel::class.java) ->
                            PreAuditDeleteViewModel(context, featureDeleteUseCase) as T

                        modelClass.isAssignableFrom(FeatureCreateViewModel::class.java) ->
                            FeatureCreateViewModel(context, featureSaveUseCase,
                                    featureGetAllByTypeUseCase, featureDeleteUseCase) as T

                        modelClass.isAssignableFrom(FeatureGetViewModel::class.java) ->
                            FeatureGetViewModel(context, featureGetAllByTypeUseCase) as T

                        else -> throw IllegalArgumentException("Unknown ViewModel class : ${modelClass.name}")
                    }
                }
            }

        }
    }
}