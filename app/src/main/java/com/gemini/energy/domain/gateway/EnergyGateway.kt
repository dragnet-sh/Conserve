package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.service.OutgoingRows
import io.reactivex.Observable

interface EnergyGateway {
    fun compute(audit: Audit, auditGateway: AuditGateway): Observable<List<OutgoingRows>>
    fun upload(outgoingRows: List<OutgoingRows>): Observable<Unit>
}