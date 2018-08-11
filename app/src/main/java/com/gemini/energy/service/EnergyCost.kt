package com.gemini.energy.service

import com.gemini.energy.service.usage.IUsageType
import timber.log.Timber

interface ICost {
    fun cost(): Double
}

class EmptyRateStructureException(message: String) : Exception(message)

class CostElectric(private var energyUsage: EnergyUsage, private var utility: EnergyUtility)
    : ICost {

    var electricRateStructure: String = NONE
    var powerUsed: Double = 0.0

    override fun cost(): Double {
        val regex = "^.*TOU$".toRegex()
        val usageByYear = energyUsage.yearly()

        if (electricRateStructure == NONE) { throw  EmptyRateStructureException("Rate Structure Empty") }

        if (electricRateStructure.matches(regex)) {

            val hours = energyUsage.timeOfUse()
            val rate = utility.timeOfUse()

            val costSummerOn = hours.summerOn * powerUsed * rate.summerOn
            val costSummerPart = hours.summerPart * powerUsed * rate.summerPart
            val costSummerOff = hours.summerOff * powerUsed * rate.summerOff

            val costWinterPart = hours.winterPart * powerUsed * rate.winterPart
            val costWinterOff = hours.winterOff  * powerUsed * rate.winterOff

            val costAggregateSummer = costSummerOn + costSummerPart + costSummerOff
            val costAggregateWinter = costWinterPart + costWinterOff

            Timber.d("## TOU Hours ##")
            Timber.d(hours.toString())
            Timber.d("## TOU Rate ##")
            Timber.d(rate.toString())
            Timber.d("## TOU Cost ##")
            Timber.d(">>> Cost Summer On : $costSummerOn")
            Timber.d(">>> Cost Summer Part : $costSummerPart")
            Timber.d(">>> Cost Summer Off : $costSummerOff")

            Timber.d(">>> Cost Winter Part : $costWinterPart")
            Timber.d(">>> Cost Winter Off : $costWinterOff")

            val total = costAggregateSummer + costAggregateWinter
            Timber.d(">>> Cost Total : $total")

            return total

        } else {

            val hours = energyUsage.nonTimeOfUse()
            val rate = utility.nonTimeOfUse()

            Timber.d("## Non TOU Hours ##")
            Timber.d(hours.toString())
            Timber.d("## Non TOU Rate ##")
            Timber.d(rate.toString())
            Timber.d("## Non TOU Cost ##")
            val costSummer = hours.summerNone * powerUsed * rate.summerNone
            val costWinter = hours.winterNone * powerUsed * rate.winterNone

            Timber.d(">>> Cost Summer : $costSummer")
            Timber.d(">>> Cost Winter : $costWinter")

            val total = costSummer + costWinter
            Timber.d(">>> Total Cost : $total")

            return total

        }
    }

    companion object {
        private const val NONE = "none"
        private val regex = "^.*TOU$".toRegex()
    }

}