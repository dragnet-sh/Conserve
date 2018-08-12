package com.gemini.energy.service

import com.gemini.energy.service.type.*
import timber.log.Timber

interface ICost {
    fun cost(): Double
}

class EmptyRateStructureException(message: String) : Exception(message)

class CostElectric(private val usageHours: UsageHours, private val utilityRate: UtilityRate) : ICost {

    var structure: String = NONE
    var power: Double = 0.0

    private lateinit var hours: IUsageType
    private lateinit var rate: IUsageType

    override fun cost(): Double {
        if (structure == NONE) { throw  EmptyRateStructureException("Rate Structure Empty") }

        initialize()
        logIUsageType()
        return costSummer() + costWinter()
    }

    /**
     * Setting up the Correct Type of Hours and Rate [TOU | Non TOU]
     * */
    private fun initialize() {
        hours = if (isTOU(structure)) usageHours.timeOfUse() else usageHours.nonTimeOfUse()
        rate = if (isTOU(structure)) utilityRate.timeOfUse() else utilityRate.nonTimeOfUse()
    }

    /**
     * Calculates the Summer Cost (Proper UsageHours Type Cast done via isTOU() flag check)
     * */
    private fun costSummer(): Double {
        return if (isTOU(structure)) {

            val costSummerOn = hours.summerOn() * power * rate.summerOn()
            val costSummerPart = hours.summerPart() * power * rate.summerPart()
            val costSummerOff = hours.summerOff() * power * rate.summerOff()

            Timber.d("## TOU Cost - Summer ##")
            Timber.d(">>> Summer On : $costSummerOn")
            Timber.d(">>> Summer Part : $costSummerPart")
            Timber.d(">>> Summer Off : $costSummerOff")

            costSummerOn + costSummerPart + costSummerOff

        } else {

            val costSummerNone = hours.summerNone() * power * rate.summerNone()
            Timber.d("## Non TOU Cost - Summer ##")
            Timber.d(">>> Cost Summer None : $costSummerNone")

            costSummerNone

        }
    }

    /**
     * Calculates the Winter Cost (Proper UsageHours Type Cast done via isTOU() flag check)
     * */
    private fun costWinter(): Double {
        return if (isTOU(structure)) {

            val costWinterPart = hours.winterPart() * power * rate.winterPart()
            val costWinterOff = hours.winterOff()  * power * rate.winterOff()

            Timber.d("## TOU Cost - Winter ##")
            Timber.d(">>> Winter Part : $costWinterPart")
            Timber.d(">>> Winter Off : $costWinterOff")

            costWinterPart + costWinterOff

        } else {

            val costWinterNone = hours.winterNone() * power * rate.winterNone()
            Timber.d("## NON TOU Cost - Winter ##")
            Timber.d(">>> Winter None : $costWinterNone")

            costWinterNone
        }
    }

    private fun logIUsageType() {

        if (isTOU(structure)) {

            val hours = hours as TOU
            val rate = rate as TOU

            Timber.d("## TOU Hours ##")
            Timber.d(hours.toString())
            Timber.d("## TOU Rate ##")
            Timber.d(rate.toString())

        } else {

            val hours = hours as TOUNone
            val rate = rate as TOUNone

            Timber.d("## Non TOU Hours ##")
            Timber.d(hours.toString())
            Timber.d("## Non TOU Rate ##")
            Timber.d(rate.toString())

        }

    }

    companion object {
        private const val NONE = "none"
        private val regex = "^.*TOU$".toRegex()
        private fun isTOU(rate: String) = rate.matches(regex)
    }

}