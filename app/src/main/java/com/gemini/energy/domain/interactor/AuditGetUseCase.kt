package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class AuditGetUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<Long, Audit>(schedulers) {

    override fun buildObservable(params: Long?): Observable<Audit> {
        return auditGateway.getAudit(params!!)
    }

}