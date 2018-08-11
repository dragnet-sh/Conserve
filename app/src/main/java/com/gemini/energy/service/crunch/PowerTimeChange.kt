package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.type.UsageHours
import timber.log.Timber

class PowerTimeChange {
    lateinit var usageHoursSpecific: UsageHours
    lateinit var usageHoursBusiness: UsageHours
    lateinit var featureData: Map<String, Any>

    var checkPowerChange = false
    var checkTimeChange = false
    var checkPowerTimeChange = false

    var energyPowerChange = 0.0
    var energyTimeChange = 0.0
    var energyPowerTimeChange = 0.0

    fun delta(computable: Computable<*>): PowerTimeChange {

        /**
         * UsageHours Hours
         * */
        val preRunHours = usageHoursSpecific.yearly()
        val postRunHours = usageHoursBusiness.yearly()

        /**
         * Energy Use
         * */
        val preHourlyEnergyUse = featureData["Daily Energy Used (kWh)"] as Double
        var postHourlyEnergyUse = 0.0

        if (computable.energyPostStateLeastCost.count() > 0) {
            val energyEfficientAlternative = computable.energyPostStateLeastCost[0]
            val energyUse = energyEfficientAlternative.getValue("daily_energy_use")
            postHourlyEnergyUse = energyUse.toDouble()
        }

        /**
         * Compute Power
         * */
        val prePower = preHourlyEnergyUse / 24
        val postPower = postHourlyEnergyUse / 24

        /**
         * Compute Power Time Change Delta
         * If there is no input for the Post Run Hours - Post Run Hours Should Equal to Pre Run Hours
         * */
        energyPowerChange = preRunHours * (prePower - postPower)
        energyTimeChange = (preRunHours - postRunHours) * prePower
        energyPowerTimeChange = (preRunHours - postRunHours) * (prePower - postPower)

        /**
         * Validate Power Time Change - Set the appropriate Flag
         * */
        checkPowerChange = energyPowerChange != 0.0
        checkTimeChange = energyTimeChange != 0.0
        checkPowerTimeChange = energyPowerChange != 0.0 && energyTimeChange != 0.0

        Timber.d("----::::---- Pre Run Hours : ($preRunHours) ----::::----")
        Timber.d("----::::---- Post Run Hours : ($postRunHours) ----::::----")
        Timber.d("----::::---- Pre Power : ($prePower) ----::::----")
        Timber.d("----::::---- Post Power : ($postPower) ----::::----")

        Timber.d("----::::---- Energy Power Change : ($energyPowerChange) ----::::----")
        Timber.d("----::::---- Energy Time Change : ($energyTimeChange) ----::::----")
        Timber.d("----::::---- Energy Power Time Change : ($energyPowerTimeChange) ----::::----")

        Timber.d("----::::---- Check Power Change : ($checkPowerChange) ----::::----")
        Timber.d("----::::---- Check Time Change : ($checkTimeChange) ----::::----")
        Timber.d("----::::---- Check Power Time Change : ($checkPowerTimeChange) ----::::----")

        return this

    }

    //ToDo : Verify if only one of the following is supposed to be true or we could have more than one true values
    fun energySaving() = when {
        checkPowerChange            -> energyPowerChange
        checkTimeChange             -> energyTimeChange
        checkPowerTimeChange        -> energyPowerTimeChange
        else -> 0.0
    }

}