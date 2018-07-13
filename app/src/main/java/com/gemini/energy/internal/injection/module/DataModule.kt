package com.gemini.energy.internal.injection.module

import android.content.Context
import com.gemini.energy.data.gateway.AuditGatewayImpl
import com.gemini.energy.data.local.*
import com.gemini.energy.data.local.dao.*
import com.gemini.energy.data.local.system.AuditDatabase
import com.gemini.energy.data.remote.*
import com.gemini.energy.data.repository.*
import com.gemini.energy.data.repository.mapper.AuditMapper
import com.gemini.energy.data.repository.mapper.FeatureMapper
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

    @Provides
    @Singleton
    internal fun provideFeatureDao(auditDatabase: AuditDatabase): FeatureDao = auditDatabase.featureDao()

    @Provides
    @Singleton
    internal fun provideComputableDao(auditDatabase: AuditDatabase): ComputableDao = auditDatabase.computableDao()

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


    @Provides
    @Singleton
    internal fun provideFeatureScopeMapper() = FeatureMapper()


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

    @Provides
    @Singleton
    internal fun provideFeatureRemoteDataSource() = FeatureRemoteDataSource()

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


    @Provides
    @Singleton
    internal fun provideFeatureLocalDataSource(featureDao: FeatureDao) =
            FeatureLocalDataSource(featureDao)


    @Provides
    @Singleton
    internal fun provideComputableLocalDataSource(computableDao: ComputableDao) =
            ComputableLocalDataSource(computableDao)

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


    @Provides
    @Singleton
    internal fun provideFeatureRepository(featureLocalDataSource: FeatureLocalDataSource,
                                          featureRemoteDataSource: FeatureRemoteDataSource,
                                          featureMapper: FeatureMapper) =
            FeatureRepository(featureLocalDataSource, featureRemoteDataSource, featureMapper)


    @Provides
    @Singleton
    internal fun provideComputableRepository(computableLocalDataSource: ComputableLocalDataSource) =
            ComputableRepository(computableLocalDataSource)

    /*End of Repository*/


    @Provides
    @Singleton
    internal fun provideAuditGateway(
            auditRepository: AuditRepository,
            zoneRepository: ZoneRepository,
            auditTypeRepository: TypeRepository,
            featureRepository: FeatureRepository,
            computableRepository: ComputableRepository): AuditGateway {
        return AuditGatewayImpl(
                auditRepository,
                zoneRepository,
                auditTypeRepository,
                featureRepository,
                computableRepository)
    }
}