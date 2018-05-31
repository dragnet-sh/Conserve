package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.PreAudit
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class PreAuditGetAllUseCase (schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<Int, List<PreAudit>>(schedulers) {

    override fun buildObservable(auditId: Int?): Observable<List<PreAudit>> {
        return auditGateway.getPreAudit(auditId!!)
    }

}
