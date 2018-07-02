package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.*
import io.reactivex.Observable

interface AuditGateway {
    fun getAuditList(): Observable<List<Audit>>
    fun saveAudit(audit: Audit): Observable<Unit>

    fun getZoneList(auditId: Int): Observable<List<Zone>>
    fun saveZone(zone: Zone): Observable<Unit>

    fun getAuditScopeList(zoneId: Int, type: String): Observable<List<Type>>
    fun saveAuditScope(auditScope: Type): Observable<Unit>

    fun saveFeature(feature: List<Feature>): Observable<Unit>
}