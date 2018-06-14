package com.gemini.energy

import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.EZoneType
import org.junit.Test

class SampleUnitTest {

    @Test
    fun testEApplianceType() {
        println(EApplianceType.options())
        println("***************")
        EApplianceType.options().forEach {
            println(EApplianceType.get(it))
        }
    }


    @Test
    fun testEZoneType() {

        val mMap = EZoneType.values().associateBy { it.ordinal }
        println(mMap)

    }

}