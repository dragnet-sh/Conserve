
package com.gemini.energy.service

import android.content.Context
import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.gateway.AuditGateway
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.merge


class EnergyService(
        private val context: Context,
        private val schedulers: Schedulers,
        private val auditGateway: AuditGateway,
        private val energyUtilityElectricity: EnergyUtility,
        private val energyUsage: EnergyUsage,
        private val outgoingRows: OutgoingRows) {

    /**
     * Holds the Unit of Work i.e the IComputables
     * */
    private var taskHolder: MutableList<Observable<Computable<*>>> = mutableListOf()


    /**
     * Holds the reference to the Observed Stream
     * */
    private var disposables: MutableList<Disposable> = mutableListOf()


    /**
     * Energy Utility Gas - Electricity
     * */
    private val energyUtilityGas = EnergyUtility(context)


    /**
     * Energy Calculation - Main Entry Point
     * */
    fun run(callback: (status: Boolean) -> Unit) {

        cleanup()

        Log.d(TAG, "------------------------------------\n" +
                        ":::: Gemini Energy - Crunch Inc ::::\n" +
                        "------------------------------------\n")

        disposables.add(auditGateway.getComputable()
                .subscribeOn(schedulers.subscribeOn)
                .subscribe { computables ->
                    Observable.fromIterable(computables)
                            .subscribe({ eachComputable ->
                                Log.d(TAG, "**** Computables Iterable - (${thread()}) ****")
                                Log.d(TAG, eachComputable.toString())
                                getComputable(eachComputable)
                                        .subscribe({
                                            synchronized(taskHolder) {
                                                taskHolder.add(it)
                                            }
                                        }, { Log.d(TAG, "##### Error !! Error !! Error #####"); it.printStackTrace() }, {})
                            }, {}, {
                                Log.d(TAG, "**** Computables Iterable - [ON COMPLETE] ****")
                                doWork(callback) // << ** Executed Only One Time ** >> //
                            })
                })

    }

    /**
     * Required to Clean Up Existing Disposables before a new CRUNCH begins
     * */
    private fun cleanup() {
        Log.d(TAG, "Cleanup - (${thread()})")
        if (disposables.count() > 0) {
            Log.d(TAG, "DISPOSABLE COUNT - [${disposables.count()}] - (${thread()})")
            disposables.forEach {
                it.dispose()
                Log.d(TAG, "POST DISPOSABLE - Status - [${it.isDisposed}]")
            }
        }

        disposables.clear()
        taskHolder.clear()
    }

    /**
     * Holds the Main Work - i.e Running each Energy Calculations for each of the IComputables
     * */
    private fun doWork(callback: (status: Boolean) -> Unit) {
        Log.d(TAG, "####### DO WORK - COUNT [${taskHolder.count()}] - (${thread()}) #######")
        disposables.add(taskHolder.merge()
                .observeOn(schedulers.observeOn)
                .subscribe({}, { exception ->
                    exception.printStackTrace()
                    callback(false)
                }, {
                    Log.d(TAG, "**** Merge - [ON COMPLETE] ****")
                    callback(true) // << ** The final Exit Point ** >> //
                }))
    }

    /**
     * Takes in two Observables
     * 1. To get the Feature Pre Audit (pre-audit)
     * 2. To get the Feature Audit Scope (feature data)
     *
     * The build() method returns the fully packaged IComputable as a Flowable - Wrapped by an Observable
     * */
    private fun getComputable(computable: Computable<*>): Observable<Observable<Computable<*>>> {

        fun build(computable: Computable<*>, featureAuditScope: List<Feature>,
                          featurePreAudit: List<Feature>): Observable<Computable<*>>{

            computable.featureAuditScope = featureAuditScope
            computable.featurePreAudit = featurePreAudit

            return ComputableFactory.createFactory(computable, energyUtilityGas,
                    energyUtilityElectricity, energyUsage,
                    outgoingRows).build().compute()
        }

        return Observable.zip(
                auditGateway.getFeature(computable.auditId),
                auditGateway.getFeatureByType(computable.auditScopeId),
                BiFunction<List<Feature>, List<Feature>, Observable<Computable<*>>> { featurePreAudit, featureAuditScope ->
                    build(computable, featureAuditScope, featurePreAudit) })
    }

    private fun thread() = Thread.currentThread().name

    companion object {
        private const val TAG = "Gemini.Energy.Service"
    }

}

