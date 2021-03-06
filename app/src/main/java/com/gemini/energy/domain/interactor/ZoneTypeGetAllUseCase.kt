package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.Schedulers
import io.reactivex.Observable

class ZoneTypeGetAllUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<List<Any>, List<Type>>(schedulers) {

    override fun buildObservable(params: List<Any>?): Observable<List<Type>> {
        return auditGateway.getAuditScopeList(params?.get(0) as Int, params[1] as String)
    }

}