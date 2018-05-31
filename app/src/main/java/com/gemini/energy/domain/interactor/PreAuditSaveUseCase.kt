package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.PreAudit
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class PreAuditSaveUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<List<PreAudit>, Unit>(schedulers) {

    override fun buildObservable(preAudit: List<PreAudit>?): Observable<Unit> {
        return auditGateway.savePreAudit(preAudit!!)
    }

}
