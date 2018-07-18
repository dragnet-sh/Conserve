package com.gemini.energy.service

open class EnergyUtilityCharge {

}


class UtilityGas : EnergyUtilityCharge() {

    lateinit var rateStructure: String

}

class UtilityElectric : EnergyUtilityCharge() {

    lateinit var rateStructure: String

}