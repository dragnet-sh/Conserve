package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.*
import io.reactivex.Observable

interface AuditGateway {
    fun getAuditList(): Observable<List<Audit>>
    fun saveAudit(audit: Audit): Observable<Unit>

    fun getZoneList(auditId: Int): Observable<List<Zone>>
    fun saveZone(zone: Zone): Observable<Unit>

    fun getPreAudit(auditId: Int): Observable<List<PreAudit>>
    fun savePreAudit(preAudit: List<PreAudit>): Observable<Unit>

    fun getAuditScopeParentList(zoneId: Int, type: String): Observable<List<AuditScopeParent>>
    fun saveAuditScopeParent(auditScope: AuditScopeParent): Observable<Unit>

    fun getAuditScopeChildList(parentId: Int): Observable<List<AuditScopeChild>>
    fun saveAuditScopeChild(auditScope: AuditScopeChild): Observable<Unit>
}