package com.gemini.energy.service

import com.gemini.energy.presentation.util.EDay
import com.gemini.energy.presentation.util.ERateKey
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class EnergyUsage {

    /**
     * Usage Hours - Could either be from the PreAudit i.e. PRE
     * or could be Device Specific i.e. POST
     * */
    private lateinit var usage: Map<EDay, String?>
    private lateinit var mapper: PeakHourMapper

    /**
     * Average Usage - Utility Methods
     * */
    fun daily() = mapper.dailyHours()
    fun weekly() = mapper.weeklyHours()
    fun yearly() = mapper.yearlyHours()

    /**
     * Mapped Peak Hour by Utility Rate Structure
     * */
    fun mappedPeakHourDaily() = mapper.mappedHoursDaily()
    fun mappedPeakHourWeekly() = mapper.mappedHoursWeekly()
    fun mappedPeakHourYearly() = mapper.mappedHoursYearly()

    fun initUsage(usage: Map<EDay, String?>): EnergyUsage {
        this.usage = usage
        return this
    }

    fun build(): EnergyUsage {
        this.mapper = PeakHourMapper().run(usage)
        return this
    }


    /**
     * Different Utility Company would have their own Specific Peak Hour Mapper
     * */
    class PeakHourMapper {

        private var outgoing = hashMapOf<ERateKey, Double>()
        private var aggregate: Double = 0.0

        init {
            ERateKey.getAllElectric().forEach { outgoing[it] = 0.0 }
        }

        fun run(usage: Map<EDay, String?>): PeakHourMapper {

            for ((_, hourRange) in usage) {

                hourRange?.split(",")?.forEach { time ->

                    val component = time.split("\\s+".toRegex())
                    if (component.count() == 2) {

                        val t1 = component[0]
                        val t2 = component[1]

                        val start = getTime(t1)
                        val end = getTime(t2)

                        if (end > start) {
                            val delta = 1
                            val calendar = Calendar.getInstance()
                            var current = start

                            while (current < end) {

                                if (isSummerOffPeak(current)) {
                                    val tmp = outgoing[ERateKey.SummerOff]
                                    outgoing[ERateKey.SummerOff] = tmp!! + delta
                                }

                                if (isSummerPartialPeak(current)) {
                                    val tmp = outgoing[ERateKey.SummerPart]
                                    outgoing[ERateKey.SummerPart] = tmp!! + delta
                                }

                                if (isSummerPeak(current)) {
                                    val tmp = outgoing[ERateKey.SummerOn]
                                    outgoing[ERateKey.SummerOn] = tmp!! + delta
                                }

                                if (isWinterOffPeak(current)) {
                                    val tmp = outgoing[ERateKey.WinterOff]
                                    outgoing[ERateKey.WinterOff] = tmp!! + delta
                                }

                                if (isWinterPartialPeak(current)) {
                                    val tmp = outgoing[ERateKey.WinterPart]
                                    outgoing[ERateKey.WinterPart] = tmp!! + delta
                                }

                                calendar.time = current
                                calendar.add(Calendar.MINUTE, delta)
                                current = calendar.time

                                aggregate += 1
                            }
                        }

                    }
                }
            }


            return this
        }

        fun weeklyHours() = aggregate
        fun dailyHours() = weeklyHours() / 7
        fun yearlyHours() = dailyHours() * 365

        fun mappedHoursDaily(): HashMap<ERateKey, Double> {
            val clone = outgoing.clone() as HashMap<ERateKey, Double>
            ERateKey.getAllElectric().forEach {
                val tmp = clone[it]
                clone[it] = tmp!! / (60 * 24)
            }

            return clone
        }

        fun mappedHoursWeekly(): HashMap<ERateKey, Double> {
            val clone = outgoing.clone() as HashMap<ERateKey, Double>
            ERateKey.getAllElectric().forEach {
                val tmp = clone[it]
                clone[it] = tmp!! / (60 * 24 * 7)
            }

            return clone
        }

        fun mappedHoursYearly(): HashMap<ERateKey, Double> {
            val clone = outgoing.clone() as HashMap<ERateKey, Double>
            ERateKey.getAllElectric().forEach {
                val tmp = clone[it]
                clone[it] = tmp!! / (60 * 24 * 7 * 365)
            }

            return clone
        }

        companion object {

            private val dateFormatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            private const val TAG = "PeakHourMapper"

            private fun getTime(time: String) = dateFormatter.parse(time)

            private fun inBetween(now: Date, start: Date, end: Date): Boolean {

                val a = now.time
                val b = start.time
                val c = end.time

                return (a >= b) && (a < c)

            }

            private fun isSummerPeak(now: Date) = inBetween(now, getTime("12:00"), getTime("18:00"))

            private fun isSummerPartialPeak(now: Date) = inBetween(now, getTime("08:30"), getTime("12:00")) ||
                    inBetween(now, getTime("18:00"), getTime("21:30"))

            private fun isSummerOffPeak(now: Date) = inBetween(now, getTime("21:30"), getTime("23:59")) ||
                    inBetween(now, getTime("00:00"), getTime("08:30"))

            private fun isWinterPartialPeak(now: Date) = inBetween(now, getTime("08:30"), getTime("21:30"))

            private fun isWinterOffPeak(now: Date) = inBetween(now, getTime("21:30"), getTime("23:59")) ||
                    inBetween(now, getTime("00:00"), getTime("08:30"))

        }
    }

}
