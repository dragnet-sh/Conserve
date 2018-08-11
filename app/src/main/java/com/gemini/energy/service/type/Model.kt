package com.gemini.energy.service.type

interface IUsageType

data class TOU(
        var summerOn: Double,
        var summerPart: Double,
        var summerOff: Double,
        var winterPart: Double,
        var winterOff: Double
) : IUsageType {
    override fun toString(): String {
        return ">>> Summer On : $summerOn \n" +
                ">>> Summer Part : $summerPart \n" +
                ">>> Summer Off : $summerOff \n" +
                ">>> Winter Part : $winterPart \n" +
                ">>> Winter Off : $winterOff"
    }
}

data class TOUNone(
        var summerNone: Double,
        var winterNone: Double
) : IUsageType {
    override fun toString(): String {
        return ">>> Summer None : $summerNone \n" +
                ">>> Winter None : $winterNone \n"
    }
}
