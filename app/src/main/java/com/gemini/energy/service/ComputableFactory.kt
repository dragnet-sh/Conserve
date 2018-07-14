package com.gemini.energy.service

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.EZoneType
import io.reactivex.Flowable

// ** Computable Generator Factory gives me a list of IComputable ** //

// ** Type 1 Service - Emits :: Single Row of Computable Data
// ** Type 2 Service - Emits :: List of Computable Data

abstract class ComputableFactory {
    abstract fun build(computable: Computable<*>): Flowable<List<IComputable>>

    companion object {
        inline fun createFactory(computable: Computable<*>): ComputableFactory =
                when (computable.auditScopeType as EZoneType) {
                    EZoneType.Plugload              -> PlugloadFactory()
                    EZoneType.HVAC                  -> HvacFactory()
                    EZoneType.Lighting              -> LightingFactory()
                    EZoneType.Motors                -> MotorFactory()
                    EZoneType.Others                -> GeneralFactory()
                }
    }
}

abstract class EnergyEquivalent(private val computable: Computable<*>) {
    abstract fun isEnergyStar(): Boolean
    abstract fun findAlternateModel(): List<IComputable>

    // Step 1: Check to see the Parse Database to figure out if the Device is Star Rated Already
    // Step 2: Query for the Alternative Energy Source - This is dependant on the Type of Device

    fun setup() {
        computable.isEnergyStar = isEnergyStar()
        computable.energyEquivalent = findAlternateModel()
    }

}


class PlugloadFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): Flowable<List<IComputable>> {
        return Flowable.just(listOf())
    }
}

class LightingFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): Flowable<List<IComputable>> {
        return Flowable.just(listOf())
    }
}

class HvacFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): Flowable<List<IComputable>> {
        return Flowable.just(listOf())
    }
}

class MotorFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): Flowable<List<IComputable>> {
        return Flowable.just(listOf())
    }
}

class GeneralFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): Flowable<List<IComputable>> {
        return Flowable.just(listOf())
    }
}