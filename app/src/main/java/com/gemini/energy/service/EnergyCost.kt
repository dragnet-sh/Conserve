package com.gemini.energy.service

import com.gemini.energy.presentation.util.ERateKey
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
        val usageByPeak = energyUsage.mappedPeakHourYearly()
        val usageByYear = energyUsage.yearly()

        if (electricRateStructure == NONE) { throw  EmptyRateStructureException("Rate Structure Empty") }

        if (electricRateStructure.matches(regex)) {

            val costSummerOn = usageByPeak[ERateKey.SummerOn]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOn.value]!![0].toDouble()
            val costSummerPart = usageByPeak[ERateKey.SummerPart]!! * .504 * powerUsed * utility.structure[ERateKey.SummerPart.value]!![0].toDouble()
            val costSummerOff = usageByPeak[ERateKey.SummerOff]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOff.value]!![0].toDouble()

            val costWinterPart = usageByPeak[ERateKey.WinterPart]!! * .496 * powerUsed * utility.structure[ERateKey.WinterPart.value]!![0].toDouble()
            val costWinterOff = usageByPeak[ERateKey.WinterOff]!! * .496 * powerUsed * utility.structure[ERateKey.WinterOff.value]!![0].toDouble()

            val costAggregateSummer = costSummerOn + costSummerPart + costSummerOff
            val costAggregateWinter = costWinterPart + costWinterOff

            Timber.d("##### TOU - COST Calculation #####")
            Timber.d(">>> Cost Summer On : $costSummerOn")
            Timber.d(">>> Cost Summer Part : $costSummerPart")
            Timber.d(">>> Cost Summer Off : $costSummerOff")

            Timber.d(">>> Cost Winter Part : $costWinterPart")
            Timber.d(">>> Cost Winter Off : $costWinterOff")

            val total = costAggregateSummer + costAggregateWinter
            Timber.d(">>> Cost Total : $total")

            return total

        } else {

            Timber.d("##### Non TOU - COST Calculation #####")
            Timber.d(">>> Usage By Year : $usageByYear")
            Timber.d(">>> Power Used : $powerUsed")
            Timber.d(">>> Rate Summer : ${utility.structure[ERateKey.SummerNone.value]!![0]}")
            Timber.d(">>> Rate Winter : ${utility.structure[ERateKey.WinterNone.value]!![0]}")

            val costSummer = usageByYear * .504 * powerUsed * utility.structure[ERateKey.SummerNone.value]!![0].toDouble()
            val costWinter = usageByYear * .496 * powerUsed * utility.structure[ERateKey.WinterNone.value]!![0].toDouble()

            Timber.d(">>> Cost Summer : $costSummer")
            Timber.d(">>> Cost Winter : $costWinter")

            val total = costSummer + costWinter
            Timber.d(">>> Total Cost : $total")

            return total

        }
    }

    companion object {
        private const val NONE = "none"
    }

}