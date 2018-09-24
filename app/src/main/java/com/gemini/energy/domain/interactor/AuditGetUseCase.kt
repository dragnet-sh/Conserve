package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGetUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Int, Audit>(schedulers) {

    override fun buildObservable(params: Int?): Observable<Audit> {
        return auditGateway.getAudit(params!!)
    }

}