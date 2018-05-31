package com.gemini.energy.internal.injection.module

import android.content.Context
import com.gemini.energy.data.gateway.AuditGatewayImpl
import com.gemini.energy.data.local.AuditLocalDataSource
import com.gemini.energy.data.local.PreAuditLocalDataSource
import com.gemini.energy.data.local.ZoneLocalDataSource
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.dao.PreAuditDao
import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.remote.AuditRemoteDataSource
import com.gemini.energy.data.repository.AuditRepository
import com.gemini.energy.data.repository.mapper.AuditMapper
import com.gemini.energy.data.local.system.AuditDatabase
import com.gemini.energy.data.remote.PreAuditRemoteDataSource
import com.gemini.energy.data.remote.ZoneRemoteDataSource
import com.gemini.energy.data.repository.PreAuditRepository
import com.gemini.energy.data.repository.ZoneRepository
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

    /*End of DAO*/


    /*Mapper*/

    @Provides
    @Singleton
    internal fun provideAuditMapper(): AuditMapper = AuditMapper()


    @Provides
    @Singleton
    internal fun providePreAuditMapper(): PreAuditMapper = PreAuditMapper()


    @Provides
    @Singleton
    internal fun provideZoneMapper(): ZoneMapper = ZoneMapper()

    /*End of Mapper*/


    /*Remote Data Source*/

    @Provides
    @Singleton
    internal fun provideAuditRemoteDataSource(): AuditRemoteDataSource = AuditRemoteDataSource()

    @Provides
    @Singleton
    internal fun providePreAuditRemoteDataSource(): PreAuditRemoteDataSource = PreAuditRemoteDataSource()

    @Provides
    @Singleton
    internal fun provideZoneRemoteDataSource(): ZoneRemoteDataSource = ZoneRemoteDataSource()

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

    /*End of Repository*/


    @Provides
    @Singleton
    internal fun provideAuditGateway(
            auditRepository: AuditRepository,
            preAuditRepository: PreAuditRepository,
            zoneRepository: ZoneRepository
                                         ): AuditGateway {
        return AuditGatewayImpl(auditRepository, preAuditRepository, zoneRepository)
    }
}