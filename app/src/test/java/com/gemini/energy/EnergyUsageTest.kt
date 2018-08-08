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
        assertEquals(13, mappedYearly[ERateKey.SummerOn].toString().toDouble(), 1)
        assertEquals(91, mappedYearly[ERateKey.SummerPart].toString().toDouble(), 1)
        assertEquals(104, mappedYearly[ERateKey.WinterPart].toString().toDouble(), 1)
        

        // *** Test Case 2 *** //

        val energyUsageTwo = EnergyUsage()
                .initUsage(usage2)
                .build()

        assertEquals(2.62, energyUsageTwo.daily(), 0.5)
        assertEquals(18.334, energyUsageTwo.weekly(), 1)
        assertEquals(953.33, energyUsageTwo.yearly(), 1)

        val mappedYearlyTwo = energyUsageTwo.mappedPeakHourYearly()
        assertEquals(121.33, mappedYearlyTwo[ERateKey.SummerOn].toString().toDouble(), 1)
        assertEquals(39, mappedYearlyTwo[ERateKey.SummerOff].toString().toDouble(), 1)
        assertEquals(316.33, mappedYearlyTwo[ERateKey.SummerPart].toString().toDouble(), 1)
        assertEquals(39, mappedYearlyTwo[ERateKey.WinterOff].toString().toDouble(), 1)
        assertEquals(437.67, mappedYearlyTwo[ERateKey.WinterPart].toString().toDouble(), 1)

    }


}
