package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.service.OutgoingRow
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface EnergyGateway {
    fun compute(audit: Audit, auditGateway: AuditGateway): Disposable
    fun upload(outgoingRows: List<OutgoingRow>): Observable<Unit>
}