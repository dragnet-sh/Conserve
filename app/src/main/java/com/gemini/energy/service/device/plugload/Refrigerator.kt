package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.EnergyUsage
import com.gemini.energy.service.EnergyUtility
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import io.reactivex.Flowable
import org.json.JSONObject
import java.util.*

class Refrigerator(computable: Computable<*>, energyUtility: EnergyUtility,
                   energyUsage: EnergyUsage, private val outgoingRows: OutgoingRows) : EBase(computable, energyUtility, energyUsage), IComputable {

    /**
     * IMP !! This is the Main Compute Method
     * */
    override fun compute(): Flowable<List<OutgoingRows>> {
        super.initialize()
        super.compute(extra = {
            Log.d(TAG, it)
        })
                .subscribeOn(schedulers.subscribeOn)
                .observeOn(schedulers.observeOn)
                .subscribe {

                    // 1. Original Computable - Done
                    // 2. Efficient Alternative JSON Data - Done
                    // 3. Utility Rate - In Progress
                    // 4. Usage Hours - Done

                    val power = featureData?.get("Power Consumed")?.valueString?.toDouble()
                    val usage = energyUsage.initUsage(mappedUsageHours()).build()
                    val energyConsumed = (power ?: 0.0) * usage.yearly()
                    val cost = costElectricity(energyConsumed, usage, electricityUtility)

                    Log.d(TAG, "Power - $power")
                    Log.d(TAG, "Usage Mapped by Peak (Yearly)- ${usage.mappedPeakHourYearly()}")
                    Log.d(TAG, "Usage (Yearly)- ${usage.yearly()}")
                    Log.d(TAG, "Energy Consumed - $energyConsumed")
                    Log.d(TAG, "Total Cost - $cost")


                    outgoingRows.header = mutableListOf("power", "yearly_usage_hrs", "energy_consumed", "cost")
                    outgoingRows.rows = mutableListOf(mapOf("power" to power.toString(),
                            "yearly_usage_hrs" to usage.yearly().toString(),
                            "energy_consumed" to energyConsumed.toString(),
                            "cost" to cost.toString()))

                    val path = StringBuilder()
                    path.append("${it.auditName.toLowerCase().replace(" ", "_")}/")
                    path.append("${it.zoneName.toLowerCase().replace(" ", "_")}/")
                    path.append("${it.auditScopeType?.value?.toLowerCase()}/")
                    path.append("${it.auditScopeSubType?.toString()?.toLowerCase()}/")
                    path.append("${it.auditScopeName.toLowerCase().replace("[^a-zA-Z0-9]".toRegex(), "_")}/")

                    val filename = "${Date().toInstant().epochSecond}"

                    outgoingRows.setFilePath(path.toString(), filename)
                    outgoingRows.saveFile()

                    Log.d(TAG, "File Path: ${outgoingRows.filePath}")
                }

        return Flowable.just(listOf())
    }

    override fun efficientLookup() = true
    override fun queryFilter() = JSONObject()
            .put("data.style_type", "Reach-in")
            .put("data.total_volume", 17.89)
            .toString()

    companion object {
        private const val TAG = "Refrigerator"

        fun fields() = listOf(
                "company", "model_number", "style_type", "total_volume","daily_energy_use", "rebate",
                "pgne_measure_code", "purchase_price_per_unit", "vendor",

                "__daily_operating_hours", "__electric_energy", "__electric_cost"
        )
    }
}
