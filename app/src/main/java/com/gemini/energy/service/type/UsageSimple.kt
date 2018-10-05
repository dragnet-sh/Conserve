package com.gemini.energy.service.type

class UsageSimple(private val peakHours: Int,
                  private val partPeakHours: Int, private val offPeakHours: Int) : UsageHours() {

    private var summerOn = 0.0
    private var summerPart = 0.0
    private var summerOff = 0.0
    private var winterPart = 0.0
    private var winterOff = 0.0

    private var summerNone = 0.0
    private var winterNone = 0.0

    init {

        summerOn = peakHours.toDouble() / 2
        summerPart = partPeakHours.toDouble() / 2
        summerOff = offPeakHours.toDouble() / 2
        winterPart = (partPeakHours.toDouble() / 2) + (peakHours.toDouble() / 2)
        winterOff = offPeakHours.toDouble() / 2

        summerNone = offPeakHours.toDouble() / 2
        winterNone = offPeakHours.toDouble() / 2

    }

    override fun timeOfUse() = TOU(summerOn, summerPart, summerOff, winterPart, winterOff)
    override fun nonTimeOfUse() = TOUNone(summerNone, winterNone)
    override fun yearly(): Double = peakHours.toDouble() + partPeakHours.toDouble() + offPeakHours.toDouble()

}