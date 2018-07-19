package com.gemini.energy

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.gemini.energy.service.EnergyUtility
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnergyUtilityTest {

    @Test
    fun readUtilityData() {
        val context = InstrumentationRegistry.getTargetContext()
        val utility = EnergyUtility(context)
                .initRate("A-1 TOU")
                .build()

        val structure = utility.structure

        assertEquals(5, structure.count())
        assertEquals(listOf("0.19956", "0.23020"), structure["winter-off-peak"])
        assertEquals(listOf("0.22047", "0.23020"), structure["winter-part-peak"])
        assertEquals(listOf("0.21197", "0.23020"), structure["summer-off-peak"])
        assertEquals(listOf("0.23933", "0.23020"), structure["summer-part-peak"])
        assertEquals(listOf("0.26298", "0.23020"), structure["summer-on-peak"])
    }

    companion object {
        private const val TAG = "EnergyUtilityTest"
    }

}