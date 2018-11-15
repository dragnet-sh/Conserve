package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import io.reactivex.Observable

class GravesUpdateUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway):
        UseCase<List<Any>, Unit>(schedulers) {

    override fun buildObservable(params: List<Any>?): Observable<Unit> {
        return auditGateway.updateGraves(params?.get(0) as Int, params[1] as Int)
    }

}