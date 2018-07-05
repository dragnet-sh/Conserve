package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class FeatureDeleteUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway)
    : UseCase<List<Feature>, Unit>(schedulers) {

    override fun buildObservable(params: List<Feature>?): Observable<Unit> {
        return auditGateway.deleteFeature(params!!)
    }
}