package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class ZoneGetUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Int, Zone>(schedulers) {

    override fun buildObservable(params: Int?): Observable<Zone> {
        return auditGateway.getZone(params!!)
    }
}