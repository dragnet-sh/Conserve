package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.AuditScopeParent
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.Schedulers
import io.reactivex.Observable

class ZoneTypeGetAllUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<List<Any>, List<AuditScopeParent>>(schedulers) {

    override fun buildObservable(params: List<Any>?): Observable<List<AuditScopeParent>> {
        return auditGateway.getAuditScopeParentList(0)
    }

}