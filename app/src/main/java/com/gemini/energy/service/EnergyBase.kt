package com.gemini.energy.service

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.parse.IEnergyStar
import com.gemini.energy.service.parse.IQuery

abstract class EnergyBase(
        private val energyStar: IEnergyStar,
        private val alternateEnergyEquivalent: IQuery) {

    lateinit var computable: Computable<*>

    private fun isEnergyStar(modelNumber: String, company: String) = energyStar.check(modelNumber, company)
    private fun findAlternateModel(parameter: HashMap<String, Any>): List<IComputable> =
            alternateEnergyEquivalent.query(parameter)

    abstract fun modelNumber(): String
    abstract fun company(): String
    abstract fun alternateMatchParameter(): HashMap<String, Any>

    // Step 1: Check to see the Parse Database to figure out if the Device is Star Rated Already
    // Step 2: Query for the Alternative Energy Source - This is dependant on the Type of Device

    fun setup() {
        computable.isEnergyStar = isEnergyStar(modelNumber(), company())
        computable.energyEquivalent = findAlternateModel(alternateMatchParameter())
    }

}

