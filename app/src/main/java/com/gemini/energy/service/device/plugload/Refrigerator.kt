package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.EnergyUsage
import com.gemini.energy.service.EnergyUtility
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.json.JSONObject

class Refrigerator(private val computable: Computable<*>, energyUtility: EnergyUtility,
                   energyUsage: EnergyUsage, outgoingRows: OutgoingRows) :
        EBase(computable, energyUtility, energyUsage, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Flowable<Boolean> {

        Log.d(TAG, "Refrigerator : Compute : Start")
        Log.d(TAG, computable.toString())

        return Flowable.create<Boolean>({ emitter ->

            super.initialize()
            super.compute(extra = {
                Log.d(TAG, it)
            })
                    .subscribeOn(schedulers.subscribeOn)
                    .observeOn(schedulers.observeOn)
                    .subscribe {

                        Log.d(TAG, "Refrigerator : Compute : Saving Data")
//                        outgoingRows.saveFile()

                        emitter.onNext(true)
                        emitter.onComplete()
                    }

        }, BackpressureStrategy.BUFFER)

    }

    /**
     * Energy Cost Calculation Formula
     * */
    override fun cost(vararg params: Any): Double {
        val dailyEnergyUsed = params[0] as Double
        val yearlyEnergyUsed = dailyEnergyUsed * (operatingHours.yearly())

        return costElectricity(yearlyEnergyUsed, operatingHours, electricityUtility)
    }

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryFilter() = JSONObject()
            .put("data.style_type", featureData["Product Type"] as String)
            .put("data.total_volume", featureData["Total Volume"] as Double)
            .toString()


    /**
     * Define all the fields here - These would be used to Generate the Outgoing Rows.
     * */
    override fun preAuditFields() = mutableListOf("Number of Vacation days")
    override fun featureDataFields() = mutableListOf("Company", "Model Number", "Fridge Capacity", "Age", "Control",
            "Daily Energy Used (kWh)", "Product Type", "Total Volume")

    override fun preStateFields() = mutableListOf<String>()
    override fun postStateFields() = mutableListOf("company", "model_number", "style_type",
            "total_volume", "daily_energy_use", "rebate", "pgne_measure_code", "purchase_price_per_unit", "vendor")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")

    companion object {
        private const val TAG = "Refrigerator"
    }
}
