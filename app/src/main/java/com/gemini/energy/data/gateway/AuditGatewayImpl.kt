package com.gemini.energy.data.gateway

import com.gemini.energy.data.gateway.mapper.SystemMapper
import com.gemini.energy.data.local.model.GraveLocalModel
import com.gemini.energy.data.repository.*
import com.gemini.energy.domain.entity.*
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGatewayImpl(
        private val auditRepository: AuditRepository,
        private val zoneRepository: ZoneRepository,
        private val typeRepository: TypeRepository,
        private val featureRepository: FeatureRepository,
        private val computableRepository: ComputableRepository,
        private val gravesRepository: GravesRepository) : AuditGateway {

    private val mapper = SystemMapper()

    /*Audit*/
    override fun getAudit(auditId: Int): Observable<Audit> = auditRepository.get(auditId).map { mapper.toEntity(it) }
    override fun getAuditList(): Observable<List<Audit>> =
            auditRepository.getAll()
                    .doOnError { println("Audit Get Error") }
                    .map { it.map { mapper.toEntity(it) } }

    override fun saveAudit(audit: Audit): Observable<Unit> = auditRepository.save(audit)
    override fun updateAudit(audit: Audit): Observable<Unit> = auditRepository.update(audit)
    override fun deleteAudit(auditId: Int): Observable<Unit> = auditRepository.delete(auditId)

    /*Zone*/
    override fun getZone(zoneId: Int): Observable<Zone> = zoneRepository.get(zoneId).map { mapper.toEntity(it) }
    override fun getZoneList(auditId: Int): Observable<List<Zone>> =
            zoneRepository.getAllByAudit(auditId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun saveZone(zone: Zone): Observable<Unit> = zoneRepository.save(zone)
    override fun updateZone(zone: Zone): Observable<Unit> = zoneRepository.update(zone)
    override fun deleteZone(zoneId: Int): Observable<Unit> = zoneRepository.delete(zoneId)
    override fun deleteZoneByAuditId(id: Int): Observable<Unit> = zoneRepository.deleteByAuditId(id)

    /*Audit Zone Type*/
    override fun getAuditScope(id: Int): Observable<Type> =
            typeRepository.get(id).map { mapper.toEntity(it) }

    override fun getAuditScopeList(zoneId: Int, type: String): Observable<List<Type>> =
            typeRepository.getAllTypeByZone(zoneId, type)
                    .map { it.map { mapper.toEntity(it) } }

    override fun getAuditScopeByAudit(auditId: Int): Observable<List<Type>> =
        typeRepository.getAllTypeByAudit(auditId)
                .map { it.map { mapper.toEntity(it) } }

    override fun saveAuditScope(auditScope: Type) = typeRepository.save(auditScope)
    override fun updateAuditScope(auditScope: Type): Observable<Unit> = typeRepository.update(auditScope)
    override fun deleteAuditScope(id: Int): Observable<Unit> = typeRepository.delete(id)
    override fun deleteAuditScopeByZoneId(id: Int): Observable<Unit> = typeRepository.deleteByZoneId(id)
    override fun deleteAuditScopeByAuditId(id: Int): Observable<Unit> = typeRepository.deleteByAuditId(id)

    /*Feature Data*/
    override fun getFeature(auditId: Int): Observable<List<Feature>> =
            featureRepository.getAllByAudit(auditId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun getFeatureByType(zoneId: Int): Observable<List<Feature>> =
            featureRepository.getAllByType(zoneId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun getFeatureByAudit(auditId: Int): Observable<List<Feature>> =
            featureRepository.getAllByAudit(auditId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun saveFeature(feature: List<Feature>): Observable<Unit> = featureRepository.save(feature)

    override fun deleteFeature(feature: List<Feature>): Observable<Unit> =
            featureRepository.delete(feature)

    override fun deleteFeatureByTypeId(id: Int): Observable<Unit> = featureRepository.deleteByTypeId(id)
    override fun deleteFeatureByAuditId(id: Int): Observable<Unit> = featureRepository.deleteByAuditId(id)
    override fun deleteFeatureByZoneId(id: Int): Observable<Unit> = featureRepository.deleteByZoneId(id)

    /*Computable*/
    override fun getComputable(): Observable<List<Computable<*>>> =
        computableRepository.getAllComputable()
                .map { it.map { mapper.toEntity(it) } }

    /*Graves*/
    override fun saveGraves(grave: GraveLocalModel) = gravesRepository.save(grave)
    override fun updateGraves(oid: Int, usn: Int) = gravesRepository.update(oid, usn)
    override fun deleteGraves(oid: Int) = gravesRepository.delete(oid)

}