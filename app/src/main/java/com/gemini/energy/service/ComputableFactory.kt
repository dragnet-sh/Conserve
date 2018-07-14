package com.gemini.energy.service

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.ELightingType
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.service.device.General
import com.gemini.energy.service.device.Hvac
import com.gemini.energy.service.device.Motors
import com.gemini.energy.service.device.lighting.Cfl
import com.gemini.energy.service.device.lighting.Halogen
import com.gemini.energy.service.device.lighting.Incandescent
import com.gemini.energy.service.device.lighting.LinearFluorescent
import com.gemini.energy.service.device.plugload.*

// ** Computable Generator Factory gives me a list of IComputable ** //

// ** Type 1 Service - Emits :: Single Row of Computable Data
// ** Type 2 Service - Emits :: List of Computable Data

abstract class ComputableFactory {
    abstract fun build(computable: Computable<*>): IComputable

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

class PlugloadFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): IComputable {
        return when(computable.auditScopeSubType as EApplianceType) {
            EApplianceType.CombinationOven          -> CombinationOven()
            EApplianceType.ConvectionOven           -> ConvectionOven()
            EApplianceType.ConveyorOven             -> ConveyorOven()
            EApplianceType.Fryer                    -> Fryer()
            EApplianceType.IceMaker                 -> IceMaker()
            EApplianceType.RackOven                 -> RackOven()
            EApplianceType.Refrigerator             -> Refrigerator()
            EApplianceType.SteamCooker              -> SteamCooker()
        }
    }
}

class LightingFactory : ComputableFactory() {
    override fun build(computable: Computable<*>): IComputable {
        return when(computable.auditScopeSubType as ELightingType) {
            ELightingType.CFL                       -> Cfl()
            ELightingType.Halogen                   -> Halogen()
            ELightingType.Incandescent              -> Incandescent()
            ELightingType.LinearFluorescent         -> LinearFluorescent()
        }
    }
}

class HvacFactory : ComputableFactory() {
    override fun build(computable: Computable<*>) = Hvac()
}

class MotorFactory : ComputableFactory() {
    override fun build(computable: Computable<*>) = Motors()
}

class GeneralFactory : ComputableFactory() {
    override fun build(computable: Computable<*>) = General()
}