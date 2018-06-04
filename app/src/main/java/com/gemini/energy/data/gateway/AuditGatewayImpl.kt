package com.gemini.energy.data.gateway

import com.gemini.energy.data.gateway.mapper.SystemMapper
import com.gemini.energy.data.repository.AuditRepository
import com.gemini.energy.data.repository.PreAuditRepository
import com.gemini.energy.data.repository.ScopeRepository
import com.gemini.energy.data.repository.ZoneRepository
import com.gemini.energy.domain.entity.*
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGatewayImpl(
        private val auditRepository: AuditRepository,
        private val preAuditRepository: PreAuditRepository,
        private val zoneRepository: ZoneRepository,
        private val auditScopeRepository: ScopeRepository) : AuditGateway {

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


    /*Audit Scope Parent*/
    override fun getAuditScopeParentList(zoneId: Int): Observable<List<AuditScopeParent>> =
        auditScopeRepository.getAllParentByZone(zoneId)
                .map { it.map { mapper.toEntity(it) } }

    override fun saveAuditScopeParent(auditScope: AuditScopeParent) = auditScopeRepository.saveParent(auditScope)


    /*Audit Scope Child*/
    override fun getAuditScopeChildList(parentId: Int): Observable<List<AuditScopeChild>> =
        auditScopeRepository.getAllChildByParent(parentId)
                .map { it.map { mapper.toEntity(it) } }

    override fun saveAuditScopeChild(auditScope: AuditScopeChild) = auditScopeRepository.saveChild(auditScope)

}