package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class ZoneUpdateUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Zone, Unit>(schedulers) {

    override fun buildObservable(params: Zone?): Observable<Unit> {
        return auditGateway.updateZone(params!!)
    }

}