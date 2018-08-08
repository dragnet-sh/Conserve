package com.gemini.energy

import com.gemini.energy.presentation.util.EDay
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.EnergyUsage
import org.junit.Assert.assertEquals
import org.junit.Test


class EnergyUsageTest {

    @Test
    fun testPeakHourMapper() {

        var usage1 = hashMapOf(EDay.Mon to "8:30 12:30")
        var usage2 = hashMapOf(
                EDay.Mon to "8:30 12:30,14:50 20:00",
                EDay.Tue to "12:00 1:00,8:00 12:30",
                EDay.Fri to "7:30 11:40")

        // *** Test Case 1 *** //

        val energyUsageOne = EnergyUsage()
                .initUsage(usage1)
                .build()

        assertEquals(0.5714, energyUsageOne.daily(), 0.001)
        assertEquals(4.0, energyUsageOne.weekly(), 0.01)
        assertEquals(208.5714, energyUsageOne.yearly(), 0.01)

        val mappedDaily = energyUsageOne.mappedPeakHourDaily()
        assertEquals(0.0714, mappedDaily[ERateKey.SummerOn].toString().toDouble(), 0.01)
        assertEquals(0.5, mappedDaily[ERateKey.SummerPart].toString().toDouble(), 0.01)
        assertEquals(0.571, mappedDaily[ERateKey.WinterPart].toString().toDouble(), 0.01)

        val mappedWeeklyOne = energyUsageOne.mappedPeakHourWeekly()
        assertEquals(0.5, mappedWeeklyOne[ERateKey.SummerOn].toString().toDouble(), 0.01)
        assertEquals(3.5, mappedWeeklyOne[ERateKey.SummerPart].toString().toDouble(), 0.01)
        assertEquals(4.0, mappedWeeklyOne[ERateKey.WinterPart].toString().toDouble(), 0.01)

        val mappedYearly = energyUsageOne.mappedPeakHourYearly()
        assertEquals(26.07, mappedYearly[ERateKey.SummerOn].toString().toDouble(), 0.01)
        assertEquals(182.5, mappedYearly[ERateKey.SummerPart].toString().toDouble(), 0.01)
        assertEquals(208.57, mappedYearly[ERateKey.WinterPart].toString().toDouble(), 0.01)

        // *** Test Case 2 *** //

        val energyUsageTwo = EnergyUsage()
                .initUsage(usage2)
                .build()

        assertEquals(2.528, energyUsageTwo.daily(), 0.02)
        assertEquals(17.7, energyUsageTwo.weekly(), 0.15)
        assertEquals(929.880, energyUsageTwo.yearly(), 0.01)

        val mappedWeeklyTwo = energyUsageTwo.mappedPeakHourWeekly()
        assertEquals(4.166, mappedWeeklyTwo[ERateKey.SummerOn].toString().toDouble(), 0.01)
        assertEquals(1.5, mappedWeeklyTwo[ERateKey.SummerOff].toString().toDouble(), 0.01)
        assertEquals(12.166, mappedWeeklyTwo[ERateKey.SummerPart].toString().toDouble(), 0.01)
        assertEquals(1.5, mappedWeeklyTwo[ERateKey.WinterOff].toString().toDouble(), 0.01)
        assertEquals(16.333, mappedWeeklyTwo[ERateKey.WinterPart].toString().toDouble(), 0.01)

    }


}
