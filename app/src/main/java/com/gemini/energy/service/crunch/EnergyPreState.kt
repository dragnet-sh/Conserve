package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.DataHolder
import com.google.gson.JsonArray
import io.reactivex.Observable
import timber.log.Timber
import java.util.*

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

    fun getObservable(remoteExtract: List<Observable<JsonArray>>, cost: () -> Double): Observable<DataHolder> {
        return Observable.zip(remoteExtract, { responses ->
            Timber.d("##### Pre-State Energy Calculation - (${thread()}) #####")
            responses.forEach { response ->
                if (response is JsonArray) {
                    //ToDo: Create a HashMap of each [RemoteExtract <-> Collection of JsonElement]
                    try {
                        val jsonElements = response.map { it.asJsonObject.get("data") }
                        Timber.d(jsonElements.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val dataHolderPreState = initDataHolder()
            val preRow = mutableMapOf<String, String>()
            featureDataFields.forEach { field ->
                preRow[field] = if (featureData.containsKey(field)) featureData[field].toString() else ""
            }

            //ToDo: Pass the JsonElement HasMap to the Cost - Now the Specific Child Class can consume this
            val costValue = cost()
            dataHolderPreState.header?.add("__electric_cost")
            preRow["__electric_cost"] = costValue.toString()

            dataHolderPreState.rows?.add(preRow)
            computable.energyPreState = preRow

            Timber.d("## Data Holder - PRE STATE - (${thread()}) ##")
            Timber.d(dataHolderPreState.toString())

            dataHolderPreState
        })

    }

    private fun thread() = Thread.currentThread().name
}