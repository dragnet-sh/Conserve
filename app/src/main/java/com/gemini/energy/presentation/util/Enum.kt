package com.gemini.energy.presentation.util

/*
* Zone - Type
* */
enum class EZoneType(val value: String) {

    Plugload("Plugload"),
    HVAC("HVAC"),
    Lighting("Lighting"),
    Motors("Motors"),
    Others("Others");

    companion object {
        private val valueMap = EZoneType.values().associateBy(EZoneType::value)
        private val ordinalMap = EZoneType.values().associateBy(EZoneType::ordinal)

        fun get(type: String) = valueMap[type]
        fun get(id: Int) = ordinalMap[id]
        fun count() = valueMap.size
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

    CombinationOven("Combination Oven"),
    ConvectionOven("Convection Oven"),
    ConveyorOven("Conveyor Oven"),
    Fryer("Fryer"),
    IceMaker("Ice Maker"),
    RackOven("Rack Oven"),
    Refrigerator("Refrigerator"),
    SteamCooker("Steam Cooker");

    companion object {
        private val map = EApplianceType.values()
                .associateBy(EApplianceType::value)

        fun get(type: String) = map[type]
        fun options() = EApplianceType.values().map { it.value }.sorted()
    }

}


/*
* Plugload - Lighting Type
* */
enum class ELightingType(val value: String) {

    Halogen("Halogen"),
    CFL("CFL"),
    LinearFluorescent("Linear Fluorescent"),
    Incandescent("Incandescent");

    companion object {
        private val map = ELightingType.values()
                .associateBy(ELightingType::value)

        fun get(type: String) = map[type]
        fun options() = ELightingType.values().map { it.value }.sorted()
    }

}


/*
* Base Row Type
* */
enum class BaseRowType(val value: String) {

    TextRow("textrow"),
    IntRow("introw"),
    DecimalRow("decimalrow"),
    PickerInputRow("pickerinputrow"),
    PhoneRow("phonerow"),
    EmailRow("emailrow"),
    TextAreaRow("textarearow");

    companion object {
        private val map = BaseRowType.values().associateBy(BaseRowType::value)
        fun get(type: String) = map[type]
    }

}
