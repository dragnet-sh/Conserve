package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.*
import io.reactivex.Observable

interface AuditGateway {
    fun getAuditList(): Observable<List<Audit>>
    fun saveAudit(audit: Audit): Observable<Unit>

    fun getZone(zoneId: Int): Observable<Zone>
    fun getZoneList(auditId: Int): Observable<List<Zone>>
    fun saveZone(zone: Zone): Observable<Unit>
    fun updateZone(zone: Zone): Observable<Unit>
    fun deleteZone(zoneId: Int): Observable<Unit>
    fun deleteZoneByAuditId(id: Int): Observable<Unit>

    fun getAuditScopeList(zoneId: Int, type: String): Observable<List<Type>>
    fun saveAuditScope(auditScope: Type): Observable<Unit>
    fun deleteAuditScopeByZoneId(id: Int): Observable<Unit>
    fun deleteAuditScopeByAuditId(id: Int): Observable<Unit>

    fun getFeature(auditId: Int): Observable<List<Feature>>
    fun getFeatureByType(zoneId: Int): Observable<List<Feature>>
    fun saveFeature(feature: List<Feature>): Observable<Unit>
    fun deleteFeature(feature: List<Feature>): Observable<Unit>
    fun deleteFeatureByTypeId(id: Int): Observable<Unit>
    fun deleteFeatureByAuditId(id: Int): Observable<Unit>
    fun deleteFeatureByZoneId(id: Int): Observable<Unit>

    fun getComputable(): Observable<List<Computable<*>>>
}