package com.gemini.energy.service

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver


class EnergyService(
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway) {

    /**
     * Flowable Data Stream of Computable
     * Who is going to listen to this ??
     * */
    lateinit var computableFlow: Flowable<List<IComputable>>

    // *** This returns a Unit Flag - to signal the processing is Done or Error Out as Applicable *** //
    fun crunch(): Disposable {

        Log.d(TAG, "Energy - Service :: Crunch Inc.")

        return auditGateway.getComputable()
                .observeOn(schedulers.observeOn)
                .subscribeOn(schedulers.subscribeOn)
                .subscribeWith(object : DisposableObserver<List<Computable<*>>>() {

                    override fun onNext(computables: List<Computable<*>>) {
                        computables.groupBy { it.auditId }
                                .forEach { auditId, groupedComputables ->
                                    auditGateway.getFeature(auditId).subscribe { featurePreAudit ->
                                        groupedComputables.forEach { eachComputable ->
                                            auditGateway.getFeatureByType(eachComputable.auditScopeId)
                                                    .subscribe { featureAuditScope ->
                                                        eachComputable.featurePreAudit = featurePreAudit
                                                        eachComputable.featureAuditScope = featureAuditScope
                                                        Log.d(TAG, "*************************************")
                                                        Log.d(TAG, eachComputable.toString())
                                                        ComputableFactory.createFactory(eachComputable).build()
                                                    }
                                        }
                                    }
                                }

                        // Step 1: Build all the Computable
                        // Step 2: Call the Compute Method
                        // Step 3: Somehow Orchestrate all this Energy Calculations
                        // Step 4. Once it is done push it to the
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}

                })

    }

    fun subscribe() {
        // ** Observe the Flowable ** //
    }

    companion object {
        private val TAG = "EnergyService"
    }

}
