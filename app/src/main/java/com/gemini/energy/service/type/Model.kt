package com.gemini.energy.service.type

/**
 * Common interface methods to access the Rates | Hours
 * */
interface IUsageType {
    fun summerOn(): Double
    fun summerPart(): Double
    fun summerOff(): Double
    fun winterPart(): Double
    fun winterOff(): Double

    fun summerNone(): Double
    fun winterNone(): Double
}

/**
 * Time Of Use - Scheme [Rate Plus Hours]
 * */
data class TOU(
        private var summerOn: Double,
        private var summerPart: Double,
        private var summerOff: Double,
        private var winterPart: Double,
        private var winterOff: Double
) : IUsageType {

    constructor(): this(0.0, 0.0, 0.0, 0.0, 0.0)

    override fun summerOn() = summerOn
    override fun summerPart() = summerPart
    override fun summerOff() = summerOff
    override fun winterPart() = winterPart
    override fun winterOff() = winterOff

    override fun summerNone() = 0.0
    override fun winterNone() = 0.0

    override fun toString(): String {
        return ">>> Summer On : $summerOn \n" +
                ">>> Summer Part : $summerPart \n" +
                ">>> Summer Off : $summerOff \n" +
                ">>> Winter Part : $winterPart \n" +
                ">>> Winter Off : $winterOff"
    }

}

/**
 * No Time Of Use - Scheme [Rate Plus Hours]
 * */
data class TOUNone(
        private var summerNone: Double,
        private var winterNone: Double
) : IUsageType {

    override fun summerOn() = 0.0
    override fun summerPart() = 0.0
    override fun summerOff() = 0.0
    override fun winterPart() = 0.0
    override fun winterOff() = 0.0

    override fun summerNone() = summerNone
    override fun winterNone() = winterNone

    override fun toString(): String {
        return ">>> Summer None : $summerNone \n" +
                ">>> Winter None : $winterNone \n"
    }

}
