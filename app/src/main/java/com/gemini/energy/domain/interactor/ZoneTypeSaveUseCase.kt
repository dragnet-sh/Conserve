package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.AuditScopeParent
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable


class ZoneTypeSaveUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<AuditScopeParent, Unit>(schedulers) {

    override fun buildObservable(params: AuditScopeParent?): Observable<Unit> {
        return auditGateway.saveAuditScopeParent(params!!)
    }

}