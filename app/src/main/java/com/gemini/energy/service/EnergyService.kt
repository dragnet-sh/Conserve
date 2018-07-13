package com.gemini.energy.service

import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver


class EnergyService(
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val computableFactory: ComputableFactory) {

    // *** This returns a Unit Flag - to signal the processing is Done or Error Out as Applicable *** //
    fun crunch(): Disposable {

        return auditGateway.getComputable()
                .observeOn(schedulers.observeOn)
                .subscribeOn(schedulers.subscribeOn)
                .subscribeWith(object : DisposableObserver<List<Computable>>() {

                    override fun onNext(computables: List<Computable>) {

                        val auditMap = computables.associateBy { it.auditId }
                        auditMap.forEach { auditId, computable ->
                            auditGateway.getFeature(auditId).subscribe { preAudit ->
                                auditGateway.getFeatureByType(computable.auditScopeId)
                                        .subscribe { featureData ->

                                            // *** This Factory should give me a stream of Computable *** //
                                            // *** Who is going to Listen to this *** //
                                            computableFactory.build()


                                        }

                            }
                        }


                        // Step 1: Build all the Computable
                        computableFactory.build()

                        // Step 2: Call the Compute Method
                        // Step 3: Somehow Orchestrate all this Energy Calculations
                        // Step 4. Once it is done push it to the
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}

                })

    }

    companion object {
        private val TAG = "EnergyService"
    }

}
