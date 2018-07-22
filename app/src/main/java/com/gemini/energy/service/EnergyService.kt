package com.gemini.energy.service

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver


class EnergyService(
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val energyUtility: EnergyUtility,
        private val energyUsage: EnergyUsage,
        private val outgoingRows: OutgoingRows) {

    // *** This returns a Unit Flag - to signal the processing is Done or Error Out  as Applicable *** //
    fun run(callback: (status: Boolean) -> Unit): Disposable {

        Log.d(TAG, "Energy - Service :: Crunch Inc.")

        val observer = object: DisposableObserver<List<Computable<*>>>() {
            override fun onNext(computables: List<Computable<*>>) {
                buildComputables(computables).subscribe {
                    it.compute().subscribe {
                        callback(it)
                    }
                }
            }
            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        }

        return auditGateway.getComputable()
                .observeOn(schedulers.observeOn)
                .subscribeOn(schedulers.subscribeOn)
                .subscribeWith(observer)
    }

    private fun buildComputables(models: List<Computable<*>>): Observable<IComputable> {
        return Observable.create<IComputable> { emitter ->
            val groupedComputables = models.groupBy { it.auditId }
            for ((auditId, computables) in groupedComputables) {
                auditGateway.getFeature(auditId).subscribe { featurePreAudit ->
                    computables.forEach { eachComputable ->
                        auditGateway.getFeatureByType(eachComputable.auditScopeId)
                                .subscribe { featureAuditScope ->

                                    eachComputable.featurePreAudit = featurePreAudit
                                    eachComputable.featureAuditScope = featureAuditScope

                                    emitter.onNext(
                                            ComputableFactory.createFactory(eachComputable,
                                                    energyUtility, energyUsage, outgoingRows).build())
                                }
                    }
                }
            }
            emitter.onComplete()
        }
    }

    companion object {
        private val TAG = "EnergyService"
    }

}

