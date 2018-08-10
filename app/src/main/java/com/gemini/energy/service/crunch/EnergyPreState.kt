package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.DataHolder
import io.reactivex.Observable
import timber.log.Timber
import java.util.Date

class EnergyPreState {

    lateinit var featureDataFields: MutableList<String>
    lateinit var featureData: Map<String, Any>
    lateinit var computable: Computable<*>

    private fun initDataHolder(): DataHolder {
        val dataHolderPreState = DataHolder()

        dataHolderPreState.header?.addAll(featureDataFields)
        dataHolderPreState.computable = computable
        dataHolderPreState.fileName = "${Date().time}_pre_state.csv"

        return dataHolderPreState
    }

    fun getObservable(cost: (energyUsed: Any) -> Double): Observable<DataHolder> {
        Timber.d("##### Pre-State Energy Calculation - (${thread()}) #####")
        val dataHolderPreState = initDataHolder()
        val preRow = mutableMapOf<String, String>()
        featureDataFields.forEach { field ->
            preRow[field] = if (featureData.containsKey(field)) featureData[field].toString() else ""
        }

        val dailyEnergyUsed = featureData["Daily Energy Used (kWh)"]
        dailyEnergyUsed?.let {
            val costValue = cost(it)
            dataHolderPreState.header?.add("__electric_cost")
            preRow["__electric_cost"] = costValue.toString()
        }

        dataHolderPreState.rows?.add(preRow)
        computable.energyPreState = preRow

        Timber.d("## Data Holder - PRE STATE - (${thread()}) ##")
        Timber.d(dataHolderPreState.toString())

        return Observable.just(dataHolderPreState)
    }

    private fun thread() = Thread.currentThread().name
}