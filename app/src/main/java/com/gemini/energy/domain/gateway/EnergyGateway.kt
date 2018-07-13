package com.gemini.energy.domain.gateway

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.service.OutgoingRows
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface EnergyGateway {
    fun compute(audit: Audit, auditGateway: AuditGateway): Disposable
    fun upload(outgoingRows: List<OutgoingRows>): Observable<Unit>
}