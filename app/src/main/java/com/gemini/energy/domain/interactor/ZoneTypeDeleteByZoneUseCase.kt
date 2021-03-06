package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class ZoneTypeDeleteByZoneUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<Int, Unit>(schedulers) {

    override fun buildObservable(params: Int?): Observable<Unit> {
        return auditGateway.deleteAuditScopeByZoneId(params!!)
    }
}