package com.gemini.energy.presentation.util

/*
* Zone - Type
* */
enum class EZoneType(val value: String) {

    Plugload("Plugload"),
    HVAC("HVAC"),
    Motors("Motors"),
    Lighting("Lighting"),
    Others("Others");

    companion object {
        private val map = EZoneType.values().associateBy(EZoneType::value)
        fun get(type: String) = map[type]
        fun count() = map.size
    }

}


/*
* Stack Operation for Type Counter
* */
enum class EAction(val value: String) {

    Push("push"),
    Pop("pop");

}


/*
* Plugload - Appliance Type
* */
enum class EApplianceType(val value: String) {

    CombinationOven("CombinationOven"),
    ConvectionOven("ConvectionOven"),
    ConveyorOven("ConveyorOven"),
    Fryer("Fryer"),
    IceMaker("IceMaker"),
    RackOven("RackOven"),
    Refrigerator("Refrigerator"),
    SteamCooker("SteamCooker");

    companion object {
        private val map = EApplianceType.values().associateBy(EApplianceType::value)
        fun get(type: String) = map[type]
        fun count() = map.size
    }
}
