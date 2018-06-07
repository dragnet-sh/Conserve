package com.gemini.energy.presentation.util

/*
* Zone - Type
* */
enum class EZoneType(val value: String) {

    plugload("Plugload"),
    hvac("HVAC"),
    motors("Motors"),
    lighting("Lighting"),
    others("Others");

    companion object {
        private val map = EZoneType.values().associateBy(EZoneType::value)
        fun get(type: String) = map[type]
        fun count() = map.size
    }

}