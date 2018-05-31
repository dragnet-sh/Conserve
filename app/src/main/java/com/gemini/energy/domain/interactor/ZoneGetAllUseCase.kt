package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class ZoneGetAllUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Int, List<Zone>>(schedulers) {

    override fun buildObservable(auditId: Int?): Observable<List<Zone>> {
        return auditGateway.getZoneList(auditId!!)
    }

}