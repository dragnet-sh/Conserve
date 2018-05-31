package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGetAllUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Void, List<Audit>>(schedulers) {

    override fun buildObservable(params: Void?): Observable<List<Audit>> {
        return auditGateway.getAuditList()
    }

}