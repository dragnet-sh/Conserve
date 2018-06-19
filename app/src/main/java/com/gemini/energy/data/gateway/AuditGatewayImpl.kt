package com.gemini.energy.data.gateway

import com.gemini.energy.data.gateway.mapper.SystemMapper
import com.gemini.energy.data.repository.AuditRepository
import com.gemini.energy.data.repository.PreAuditRepository
import com.gemini.energy.data.repository.TypeRepository
import com.gemini.energy.data.repository.ZoneRepository
import com.gemini.energy.domain.entity.*
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGatewayImpl(
        private val auditRepository: AuditRepository,
        private val preAuditRepository: PreAuditRepository,
        private val zoneRepository: ZoneRepository,
        private val typeRepository: TypeRepository) : AuditGateway {

    private val mapper = SystemMapper()


    /*Audit*/
    override fun getAuditList(): Observable<List<Audit>> =
        auditRepository.getAll()
                .doOnError { println("Audit Get Error") }
                .map { it.map { mapper.toEntity(it) }}

    override fun saveAudit(audit: Audit): Observable<Unit> = auditRepository.save(audit)


    /*Zone*/
    override fun getZoneList(auditId: Int): Observable<List<Zone>> =
            zoneRepository.getAllByAudit(auditId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun saveZone(zone: Zone): Observable<Unit> = zoneRepository.save(zone)


   /*PreAudit*/
    override fun getPreAudit(auditId: Int): Observable<List<PreAudit>> =
            preAuditRepository.getAllByAudit(auditId)
                    .map { it.map { mapper.toEntity(it) } }

    override fun savePreAudit(preAudit: List<PreAudit>): Observable<Unit> = preAuditRepository.save(preAudit)


    /*Audit Zone Type*/
    override fun getAuditScopeList(zoneId: Int, type: String): Observable<List<Type>> =
        typeRepository.getAllTypeByZone(zoneId, type)
                .map { it.map { mapper.toEntity(it) } }

    override fun saveAuditScope(auditScope: Type) = typeRepository.save(auditScope)


    /*Feature Data*/

}