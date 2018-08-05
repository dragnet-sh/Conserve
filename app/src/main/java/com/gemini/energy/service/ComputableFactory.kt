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


abstract class ComputableFactory {
    abstract fun build(): IComputable

    companion object {
        lateinit var computable: Computable<*>
        inline fun createFactory(computable: Computable<*>, energyUtilityGas: EnergyUtility,
                                 energyUtilityElectricity: EnergyUtility,
                                 energyUsage: EnergyUsage, outgoingRows: OutgoingRows): ComputableFactory {
            this.computable = computable
            return when (computable.auditScopeType as EZoneType) {

                EZoneType.Plugload                  -> PlugloadFactory(energyUtilityGas,
                        energyUtilityElectricity, energyUsage, outgoingRows)

                EZoneType.HVAC                      -> HvacFactory()
                EZoneType.Lighting                  -> LightingFactory()
                EZoneType.Motors                    -> MotorFactory()
                EZoneType.Others                    -> GeneralFactory()
            }
       }
    }
}

class PlugloadFactory(private val energyUtilityGas: EnergyUtility,
                      private val energyUtilityElectricity: EnergyUtility,
                      private val energyUsage: EnergyUsage,
                      private val outgoingRows: OutgoingRows) : ComputableFactory() {

    override fun build(): IComputable {
        return when(computable.auditScopeSubType as EApplianceType) {
            EApplianceType.CombinationOven          -> CombinationOven()
            EApplianceType.ConvectionOven           -> ConvectionOven()
            EApplianceType.ConveyorOven             -> ConveyorOven()
            EApplianceType.Fryer                    -> Fryer()
            EApplianceType.IceMaker                 -> IceMaker()
            EApplianceType.RackOven                 -> RackOven()

            EApplianceType.Refrigerator             -> Refrigerator(computable,
                    energyUtilityGas, energyUtilityElectricity, energyUsage, outgoingRows)

            EApplianceType.SteamCooker              -> SteamCooker()
        }
    }

}

class LightingFactory : ComputableFactory() {
    override fun build(): IComputable {
        return when(computable.auditScopeSubType as ELightingType) {
            ELightingType.CFL                       -> Cfl()
            ELightingType.Halogen                   -> Halogen()
            ELightingType.Incandescent              -> Incandescent()
            ELightingType.LinearFluorescent         -> LinearFluorescent()
        }
    }
}

class HvacFactory : ComputableFactory() {
    override fun build() = Hvac()
}

class MotorFactory : ComputableFactory() {
    override fun build() = Motors()
}

class GeneralFactory : ComputableFactory() {
    override fun build() = General()
}