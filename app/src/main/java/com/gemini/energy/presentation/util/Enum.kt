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


/**
 * Operating Hours
 * */
enum class EDay(val value: String) {

    Mon("Monday Operating Hours"),
    Tue("Tuesday Operating Hours"),
    Wed("Wednesday Operating Hours"),
    Thu("Thursday Operating Hours"),
    Fri("Friday Operating Hours"),
    Sat("Saturday Operating Hours"),
    Sun("Sunday Operating Hours");

    companion object {
        private val map = EDay.values().associateBy(EDay::value)
        fun get(day: String) = map[day]
    }

}


/**
 * Utility Rate | Slabs
 * */
enum class ERateKey(val value: String) {
    SummerOff("summer-off-peak"),
    SummerPart("summer-part-peak"),
    SummerOn("summer-on-peak"),
    WinterOff("winter-off-peak"),
    WinterPart("winter-part-peak"),

    SummerNone("summer-none"),
    WinterNone("winter-none"),

    AverageElectric("average"),

    Slab1("0_5.0"),
    Slab2("5.1_16.0"),
    Slab3("16.1_41.0"),
    Slab4("41.1_123.0"),
    Slab5("123.1_n_up"),
    SummerTransport("summer_first_4000_therms"),
    WinterTransport("winter_first_4000_therms"),
    Surcharge("surcharge"),

    GasWinter("gas-winter"),
    GasSummer("gas-summer"),

    None("none");

    companion object {

        private val map = ERateKey.values().associateBy(ERateKey::value)
        fun get(rateKey: String) = map[rateKey]

        fun getAllElectric() = listOf(SummerOff, SummerPart, SummerOn, WinterOff, WinterPart, SummerNone, WinterNone)
        fun getAllSummer() = listOf(SummerOff, SummerPart, SummerOn, SummerNone)
        fun getAllWinter() = listOf(WinterOff, WinterPart, WinterNone)
        fun getAllElectricRaw() = getAllElectric().map { it.value }

        fun getAllGas() = listOf(Slab1, Slab2, Slab3, Slab4, Slab5)
        fun getAllGasRaw() = getAllGas().map { it.value }
    }

}
