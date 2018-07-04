package com.gemini.energy.domain.interactor

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.UseCase
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable

class FeatureGetAllUseCase(schedulers: Schedulers, private val auditGateway: AuditGateway) :
        UseCase<Int, List<Feature>>(schedulers) {

    override fun buildObservable(auditId: Int?): Observable<List<Feature>> {
        return auditGateway.getFeature(auditId!!)
    }

}