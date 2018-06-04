package com.gemini.energy.internal.injection.module

import android.content.Context
import com.gemini.energy.data.gateway.AuditGatewayImpl
import com.gemini.energy.data.local.AuditLocalDataSource
import com.gemini.energy.data.local.AuditScopeLocalDataSource
import com.gemini.energy.data.local.PreAuditLocalDataSource
import com.gemini.energy.data.local.ZoneLocalDataSource
import com.gemini.energy.data.local.dao.*
import com.gemini.energy.data.remote.AuditRemoteDataSource
import com.gemini.energy.data.repository.AuditRepository
import com.gemini.energy.data.repository.mapper.AuditMapper
import com.gemini.energy.data.local.system.AuditDatabase
import com.gemini.energy.data.remote.AuditScopeRemoteDataSource
import com.gemini.energy.data.remote.PreAuditRemoteDataSource
import com.gemini.energy.data.remote.ZoneRemoteDataSource
import com.gemini.energy.data.repository.PreAuditRepository
import com.gemini.energy.data.repository.ScopeRepository
import com.gemini.energy.data.repository.ZoneRepository
import com.gemini.energy.data.repository.mapper.AuditScopeMapper
import com.gemini.energy.data.repository.mapper.PreAuditMapper
import com.gemini.energy.data.repository.mapper.ZoneMapper
import com.gemini.energy.domain.gateway.AuditGateway
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class DataModule {

    @Provides
    @Singleton
    internal fun provideAuditDatabase(context: Context): AuditDatabase {
        return AuditDatabase.newInstance(context)
    }

    /*DAO*/

    @Provides
    @Singleton
    internal fun provideAuditDao(auditDatabase: AuditDatabase): AuditDao = auditDatabase.auditDao()

    @Provides
    @Singleton
    internal fun providePreAuditDao(auditDatabase: AuditDatabase): PreAuditDao = auditDatabase.preAuditDao()

    @Provides
    @Singleton
    internal fun provideZoneDao(auditDatabase: AuditDatabase): ZoneDao = auditDatabase.zoneDao()


    @Provides
    @Singleton
    internal fun provideAuditEntityParentDao(auditDatabase: AuditDatabase): AuditScopeParentDao = auditDatabase.auditScopeParentDao()

    @Provides
    @Singleton
    internal fun provideAuditEntityChildDao(auditDatabase: AuditDatabase): AuditScopeChildDao = auditDatabase.auditScopeChildDao()

    /*End of DAO*/


    /*Mapper*/

    @Provides
    @Singleton
    internal fun provideAuditMapper() = AuditMapper()


    @Provides
    @Singleton
    internal fun providePreAuditMapper() = PreAuditMapper()


    @Provides
    @Singleton
    internal fun provideZoneMapper() = ZoneMapper()


    @Provides
    @Singleton
    internal fun provideAuditScopeMapper() = AuditScopeMapper()


    /*End of Mapper*/


    /*Remote Data Source*/

    @Provides
    @Singleton
    internal fun provideAuditRemoteDataSource() = AuditRemoteDataSource()

    @Provides
    @Singleton
    internal fun providePreAuditRemoteDataSource() = PreAuditRemoteDataSource()

    @Provides
    @Singleton
    internal fun provideZoneRemoteDataSource() = ZoneRemoteDataSource()

    @Provides
    @Singleton
    internal fun provideAuditScopeRemoteDataSource() = AuditScopeRemoteDataSource()

    /*End of Remote Data Source*/


    /*Local Data Source*/

    @Provides
    @Singleton
    internal fun provideAuditLocalDataSource(auditDao: AuditDao): AuditLocalDataSource =
            AuditLocalDataSource(auditDao)


    @Provides
    @Singleton
    internal fun providePreAuditLocalDataSource(preAuditDao: PreAuditDao): PreAuditLocalDataSource =
            PreAuditLocalDataSource(preAuditDao)

    @Provides
    @Singleton
    internal fun provideZoneLocalDataSource(zoneDao: ZoneDao) = ZoneLocalDataSource(zoneDao)


    @Provides
    @Singleton
    internal fun provideAuditScopeLocalDataSource(auditScopeParentDao: AuditScopeParentDao,
                                                  auditScopeChildDao: AuditScopeChildDao) =
            AuditScopeLocalDataSource(auditScopeParentDao, auditScopeChildDao)

    /*End of Local Data Source*/


    /*Repository*/

    @Provides
    @Singleton
    internal fun provideAuditRepository(auditLocalDataSource: AuditLocalDataSource,
                                        auditRemoteDataSource: AuditRemoteDataSource,
                                        auditMapper: AuditMapper): AuditRepository {
        return AuditRepository(auditLocalDataSource, auditRemoteDataSource, auditMapper)
    }

    @Provides
    @Singleton
    internal fun providePreAuditRepository(preAuditLocalDataSource: PreAuditLocalDataSource,
                                           preAuditRemoteDataSource: PreAuditRemoteDataSource,
                                           preAuditMapper: PreAuditMapper) =
            PreAuditRepository(preAuditLocalDataSource, preAuditRemoteDataSource, preAuditMapper)


    @Provides
    @Singleton
    internal fun provideZoneRepository(zoneLocalDataSource: ZoneLocalDataSource,
                                       zoneRemoteDataSource: ZoneRemoteDataSource,
                                       zoneMapper: ZoneMapper) =
            ZoneRepository(zoneLocalDataSource, zoneRemoteDataSource, zoneMapper)


    @Provides
    @Singleton
    internal fun provideAuditScopeRepository(auditScopeLocalDataSource: AuditScopeLocalDataSource,
                                             auditScopeRemoteDataSource: AuditScopeRemoteDataSource,
                                             auditScopeMapper: AuditScopeMapper) =
            ScopeRepository(auditScopeLocalDataSource, auditScopeRemoteDataSource, auditScopeMapper)
    /*End of Repository*/


    @Provides
    @Singleton
    internal fun provideAuditGateway(
            auditRepository: AuditRepository,
            preAuditRepository: PreAuditRepository,
            zoneRepository: ZoneRepository,
            auditScopeRepository: ScopeRepository): AuditGateway {
        return AuditGatewayImpl(auditRepository, preAuditRepository, zoneRepository, auditScopeRepository)
    }
}