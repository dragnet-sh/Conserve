
package com.gemini.energy.service

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction


class EnergyService(
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val energyUtility: EnergyUtility,
        private val energyUsage: EnergyUsage,
        private val outgoingRows: OutgoingRows) {

    private var primary: MutableList<Observable<IComputable>> = mutableListOf()
    private var secondary: MutableList<Flowable<Boolean>> = mutableListOf()
    private var disposableStream: MutableList<Disposable> = mutableListOf()

    fun run(callback: (status: Boolean) -> Unit) {

        if (disposableStream.count() > 0) {
            disposableStream.forEach {
                it.dispose()
            }
        }

        Log.d(TAG, ":: Crunch Inc ::")
        disposableStream.add(auditGateway.getComputable()
                .subscribeOn(schedulers.subscribeOn)
                .observeOn(schedulers.observeOn)
                .subscribe {
                    Observable.fromIterable(it)
                            .subscribe({
                                primary.add(getComputable(it))
                                Log.d(TAG, it.toString())
                            }, {}, { doWorkPrimary(callback) })
                })

    }

    private fun doWorkPrimary(callback: (status: Boolean) -> Unit) {
        Log.d(TAG, "####### DO WORK PRIMARY #######")
        Log.d(TAG, primary.toString())
        Log.d(TAG, primary.count().toString())

        disposableStream.addAll(primary.map {
            it.subscribe({
                secondary.add(it.compute())
            }, {}, {
                Log.d(TAG, "##### PRIMARY WORK - DONE #####")
                doWorkSecondary(callback)
            })
        })

    }

    private fun doWorkSecondary(callback: (status: Boolean) -> Unit) {
        Log.d(TAG, "####### DO WORK SECONDARY #######")
        Log.d(TAG, secondary.toString())
        Log.d(TAG, secondary.count().toString())

        Flowable.merge(secondary)
                .subscribeOn(schedulers.subscribeOn)
                .observeOn(schedulers.observeOn)
                .subscribe ({
                    // ** Do Nothing ** //
                }, {}, {
                    Log.d(TAG, "##### SECONDARY WORK - DONE #####")
                    callback(true)
                })

    }

    private fun getComputable(computable: Computable<*>): Observable<IComputable> {
        return Observable.zip(
                auditGateway.getFeature(computable.auditId),
                auditGateway.getFeatureByType(computable.zoneId),
                BiFunction<List<Feature>, List<Feature>, IComputable> { featurePreAudit, featureAuditScope ->
                    buildComputable(computable, featureAuditScope, featurePreAudit) })
    }


    private fun buildComputable(computable: Computable<*>, featureAuditScope: List<Feature>,
                                featurePreAudit: List<Feature>): IComputable {

        computable.featureAuditScope = featureAuditScope
        computable.featurePreAudit = featurePreAudit

        return ComputableFactory.createFactory(computable, energyUtility, energyUsage,
                outgoingRows).build()
    }

    companion object {
        private const val TAG = "Gemini.EnergyService"
    }

}

