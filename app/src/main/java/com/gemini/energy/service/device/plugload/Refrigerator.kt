package com.gemini.energy.service.device.plugload

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class Refrigerator(computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
                   usageHours: UsageHours, outgoingRows: OutgoingRows) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    /**
     * Energy Cost Calculation Formula
     * */
    override fun cost(vararg params: Any) = 0.0

    /**
     * Cost - Pre State
     * */
    override fun costPreState(): Double {
        var powerUsed = 0.0

        try {
            val dailyEnergyUsed = featureData["Daily Energy Used (kWh)"]!! as Double
            powerUsed = dailyEnergyUsed / 24
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val costElectricity: Double
        costElectricity = costElectricity(powerUsed, super.energyUsageBusiness, super.electricityUtilityRate)

        return costElectricity
    }

    /**
     * Cost - Post State
     * */
    override fun costPostState(element: JsonElement): Double {
        var powerUsed = 0.0

        try {
            val postDailyEnergyUsed = element.asJsonObject.get("daily_energy_use").asDouble
            powerUsed = postDailyEnergyUsed / 24
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val costElectricity: Double
        costElectricity = costElectricity(powerUsed, super.energyUsageBusiness, super.electricityUtilityRate)

        return costElectricity
    }

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = JSONObject()
            .put("data.style_type", featureData["Product Type"])
            .put("data.total_volume", JSONObject()
                    .put("\$gte", featureData["Total Volume"] as Double - 2)
                    .put("\$lte", featureData["Total Volume"] as Double + 2))
            .toString()

    /**
     * State if the Equipment has a Post UsageHours Hours (Specific) ie. A separate set of
     * Weekly UsageHours Hours apart from the PreAudit
     * */
    override fun usageHoursSpecific() = false

    /**
     * Define all the fields here - These would be used to Generate the Outgoing Rows or perform the Energy Calculation
     * */
    override fun preAuditFields() = mutableListOf("Number of Vacation days")
    override fun featureDataFields() = mutableListOf("Company", "Model Number", "Fridge Capacity", "Age", "Control",
            "Daily Energy Used (kWh)", "Product Type", "Total Volume")

    override fun preStateFields() = mutableListOf("Daily Energy Used (kWh)")
    override fun postStateFields() = mutableListOf("company", "model_number", "style_type",
            "total_volume", "daily_energy_use", "rebate", "pgne_measure_code", "purchase_price_per_unit", "vendor")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")

}
