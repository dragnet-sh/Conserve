package com.gemini.energy

import com.gemini.energy.service.AuditGatewayMock
import com.gemini.energy.service.ComputableFactory
import com.gemini.energy.service.EnergyService
import org.junit.Test

class EnergyCalculationsTest {

    @Test
    fun testEnergyGateway() {

        val energyService = EnergyService(ComputableFactory(), AuditGatewayMock())
        
    }

}