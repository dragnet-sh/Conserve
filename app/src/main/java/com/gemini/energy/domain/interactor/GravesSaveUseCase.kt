package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.data.local.model.GraveLocalModel
import com.gemini.energy.domain.UseCase
import io.reactivex.Observable

class GravesSaveUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<GraveLocalModel, Unit>(schedulers) {

    override fun buildObservable(params: GraveLocalModel?): Observable<Unit> {
        return auditGateway.saveGraves(params!!)
    }

}