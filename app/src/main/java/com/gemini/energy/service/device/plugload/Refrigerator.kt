package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.*
import com.gemini.energy.service.device.EBase
import io.reactivex.Flowable
import org.json.JSONObject

class Refrigerator(computable: Computable<*>, energyUtility: EnergyUtility,
                   energyUsage: EnergyUsage) : EBase(computable, energyUtility, energyUsage), IComputable {

    /**
     * IMP !! This is the Main Compute Method
     * */
    override fun compute(): Flowable<List<OutgoingRow>> {
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


                    // *** Pass these on to Drop Box *** //

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
