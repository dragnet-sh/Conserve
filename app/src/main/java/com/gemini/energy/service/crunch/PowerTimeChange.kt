package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.type.UsageHours
import timber.log.Timber

/**
 * Supporting class for the Energy Efficiency Calculation
 * Energy Optimization can be categorized as follows:
 *
 * 1. Change in Power
 * 2. Change in Time
 * 3. Change in Power + Time
 * */
class PowerTimeChange {

    var checkPowerChange = false
    var checkTimeChange = false
    var checkPowerTimeChange = false

    lateinit var energyPowerChange: () -> Double
    lateinit var energyTimeChange: () -> Double
    lateinit var energyPowerTimeChange: () -> Double

    /**
     * Call the appropriate Methods and set the Change Flag accordingly
     * */
    fun delta(computable: Computable<*>): PowerTimeChange {

        checkPowerChange = energyPowerChange() != 0.0
        checkTimeChange = energyTimeChange() != 0.0
        checkPowerTimeChange = energyPowerChange() != 0.0 && energyTimeChange() != 0.0

        return this
    }

    /**
     * Returns which ever is true first
     * */
    fun energySaving() = when {
        checkPowerChange            -> energyPowerChange()
        checkTimeChange             -> energyTimeChange()
        checkPowerTimeChange        -> energyPowerTimeChange()
        else -> 0.0
    }

    companion object {
        enum class Type {
            PowerChange, TimeChange, PowerTimeChange
        }
    }

    /**
     * Gives the Energy Savings Mapped to the Type
     * */
    fun energyMap() = hashMapOf(
            Type.PowerChange to energyPowerChange(),
            Type.TimeChange to energyTimeChange(),
            Type.PowerTimeChange to energyPowerTimeChange())

    override fun toString() = "" +
                "----::::---- Energy Power Change : (${energyPowerChange()}) ----::::---- \n" +
                "----::::---- Energy Time Change : (${energyTimeChange()}) ----::::---- \n" +
                "----::::---- Energy Power Time Change : (${energyPowerTimeChange()}) ----::::---- \n" +

                "----::::---- Check Power Change : ($checkPowerChange) ----::::---- \n" +
                "----::::---- Check Time Change : ($checkTimeChange) ----::::---- \n" +
                "----::::---- Check Power Time Change : ($checkPowerTimeChange) ----::::----"

}