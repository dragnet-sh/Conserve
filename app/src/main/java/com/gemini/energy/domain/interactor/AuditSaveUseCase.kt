package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Audit
import io.reactivex.Observable

class AuditSaveUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<Audit, Unit>(schedulers) {

    override fun buildObservable(params: Audit?): Observable<Unit> {
        return auditGateway.saveAudit(params!!)
    }

}