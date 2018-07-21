package com.gemini.energy.service

interface ICost {
    fun cost(energyUsed: Double): Double
}

open class EnergyCost(energyUsage: EnergyUsage, utilityCharge: EnergyUtility) {

}


class CostGas(energyUsage: EnergyUsage, utilityCharge: EnergyUtility) :
        EnergyCost(energyUsage, utilityCharge), ICost {

    override fun cost(energyUsed: Double): Double {
        return 0.0
    }

}


class CostElectric(energyUsage: EnergyUsage, utilityCharge: EnergyUtility) :
        EnergyCost(energyUsage, utilityCharge), ICost {

    override fun cost(energyUsed: Double): Double {
        return 0.0
    }

}