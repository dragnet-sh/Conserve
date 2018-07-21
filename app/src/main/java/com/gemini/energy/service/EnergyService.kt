package com.gemini.energy.service

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver


class EnergyService(
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val energyUtility: EnergyUtility,
        private val energyUsage: EnergyUsage,
        private val outgoingRows: OutgoingRows) {

    /**
     * Flowable Data Stream of Computable
     * Who is going to listen to this ??
     * */
    lateinit var computableFlow: Flowable<List<IComputable>>

    // *** This returns a Unit Flag - to signal the processing is Done or Error Out  as Applicable *** //
    fun run(): Disposable {

        Log.d(TAG, "Energy - Service :: Crunch Inc.")

        return auditGateway.getComputable()
                .observeOn(schedulers.observeOn)
                .subscribeOn(schedulers.subscribeOn)
                .subscribeWith(object : DisposableObserver<List<Computable<*>>>() {

                    override fun onNext(computables: List<Computable<*>>) {
                        var collector: MutableList<IComputable> = mutableListOf()
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

                                                        collector.add(
                                                                ComputableFactory
                                                                        .createFactory(eachComputable,
                                                                                energyUtility, energyUsage, outgoingRows)
                                                                        .build()
                                                        )
                                                    }
                                        }
                                    }
                                }

                        collector.forEach {
                            it.compute()
                                    .observeOn(schedulers.observeOn)
                                    .subscribeOn(schedulers.subscribeOn)
                                    .subscribe {

                                        // *** DropBox Upload *** //
                                        Log.d(TAG, it.toString())

                                    }
                        }

                        // Step 1: Build all the Computable
                        // Step 2: Call the Compute Method (Return Observable)
                        // Step 3: Observe the Observable you get from Step 2
                        // Step 4: Next Step would be to Upload the Stuff
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}

                })

    }

    companion object {
        private val TAG = "EnergyService"
    }

}
