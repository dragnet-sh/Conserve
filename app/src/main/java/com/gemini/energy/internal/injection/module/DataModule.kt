package com.gemini.energy.internal.injection.module

import android.content.Context
import com.gemini.energy.data.gateway.AuditGatewayImpl
import com.gemini.energy.data.local.AuditLocalDataSource
import com.gemini.energy.data.local.TypeLocalDataSource
import com.gemini.energy.data.local.ZoneLocalDataSource
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.dao.TypeDao
import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.local.system.AuditDatabase
import com.gemini.energy.data.remote.AuditRemoteDataSource
import com.gemini.energy.data.remote.PreAuditRemoteDataSource
import com.gemini.energy.data.remote.TypeRemoteDataSource
import com.gemini.energy.data.remote.ZoneRemoteDataSource
import com.gemini.energy.data.repository.AuditRepository
import com.gemini.energy.data.repository.TypeRepository
import com.gemini.energy.data.repository.ZoneRepository
import com.gemini.energy.data.repository.mapper.AuditMapper
import com.gemini.energy.data.repository.mapper.TypeMapper
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
    internal fun provideZoneDao(auditDatabase: AuditDatabase): ZoneDao = auditDatabase.zoneDao()


    @Provides
    @Singleton
    internal fun provideAuditZoneTypeDao(auditDatabase: AuditDatabase): TypeDao = auditDatabase.auditScopeDao()

    /*End of DAO*/


    /*Mapper*/

    @Provides
    @Singleton
    internal fun provideAuditMapper() = AuditMapper()


    @Provides
    @Singleton
    internal fun provideZoneMapper() = ZoneMapper()


    @Provides
    @Singleton
    internal fun provideAuditScopeMapper() = TypeMapper()


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
    internal fun provideAuditScopeRemoteDataSource() = TypeRemoteDataSource()

    /*End of Remote Data Source*/


    /*Local Data Source*/

    @Provides
    @Singleton
    internal fun provideAuditLocalDataSource(auditDao: AuditDao): AuditLocalDataSource =
            AuditLocalDataSource(auditDao)


    @Provides
    @Singleton
    internal fun provideZoneLocalDataSource(zoneDao: ZoneDao) = ZoneLocalDataSource(zoneDao)


    @Provides
    @Singleton
    internal fun provideAuditScopeLocalDataSource(auditScopeDao: TypeDao) =
            TypeLocalDataSource(auditScopeDao)

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
    internal fun provideZoneRepository(zoneLocalDataSource: ZoneLocalDataSource,
                                       zoneRemoteDataSource: ZoneRemoteDataSource,
                                       zoneMapper: ZoneMapper) =
            ZoneRepository(zoneLocalDataSource, zoneRemoteDataSource, zoneMapper)


    @Provides
    @Singleton
    internal fun provideAuditScopeRepository(typeLocalDataSource: TypeLocalDataSource,
                                             auditScopeRemoteDataSource: TypeRemoteDataSource,
                                             auditScopeMapper: TypeMapper) =
            TypeRepository(typeLocalDataSource, auditScopeRemoteDataSource, auditScopeMapper)
    /*End of Repository*/


    @Provides
    @Singleton
    internal fun provideAuditGateway(
            auditRepository: AuditRepository,
            zoneRepository: ZoneRepository,
            auditTypeRepository: TypeRepository): AuditGateway {
        return AuditGatewayImpl(auditRepository, zoneRepository, auditTypeRepository)
    }
}