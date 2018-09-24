package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.*
import io.reactivex.Observable

interface AuditGateway {
    fun getAudit(auditId: Int): Observable<Audit>
    fun getAuditList(): Observable<List<Audit>>
    fun saveAudit(audit: Audit): Observable<Unit>
    fun updateAudit(audit: Audit): Observable<Unit>
    fun deleteAudit(auditId: Int): Observable<Unit>

    fun getZone(zoneId: Int): Observable<Zone>
    fun getZoneList(auditId: Int): Observable<List<Zone>>
    fun saveZone(zone: Zone): Observable<Unit>
    fun updateZone(zone: Zone): Observable<Unit>
    fun deleteZone(zoneId: Int): Observable<Unit>
    fun deleteZoneByAuditId(id: Int): Observable<Unit>

    fun getAuditScope(id: Int): Observable<Type>
    fun getAuditScopeList(zoneId: Int, type: String): Observable<List<Type>>
    fun getAuditScopeByAudit(auditId: Int): Observable<List<Type>>
    fun saveAuditScope(auditScope: Type): Observable<Unit>
    fun updateAuditScope(auditScope: Type): Observable<Unit>
    fun deleteAuditScope(id: Int): Observable<Unit>
    fun deleteAuditScopeByZoneId(id: Int): Observable<Unit>
    fun deleteAuditScopeByAuditId(id: Int): Observable<Unit>

    fun getFeature(auditId: Int): Observable<List<Feature>>
    fun getFeatureByType(zoneId: Int): Observable<List<Feature>>
    fun saveFeature(feature: List<Feature>): Observable<Unit>
    fun deleteFeature(feature: List<Feature>): Observable<Unit>

    fun getFeatureByAudit(auditId: Int): Observable<List<Feature>> // ToDo - Remove
    fun deleteFeatureByTypeId(id: Int): Observable<Unit> // ToDo - Remove
    fun deleteFeatureByAuditId(id: Int): Observable<Unit> // ToDo - Remove
    fun deleteFeatureByZoneId(id: Int): Observable<Unit> // ToDo - Remove

    fun getComputable(): Observable<List<Computable<*>>>
}