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
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate


abstract class ComputableFactory {
    abstract fun build(): IComputable

    companion object {
        lateinit var computable: Computable<*>
        inline fun createFactory(computable: Computable<*>, utilityRateGas: UtilityRate,
                                 utilityRateElectricity: UtilityRate,
                                 usageHours: UsageHours, outgoingRows: OutgoingRows): ComputableFactory {
            this.computable = computable
            return when (computable.auditScopeType as EZoneType) {

                EZoneType.Plugload                  -> PlugloadFactory(utilityRateGas,
                        utilityRateElectricity, usageHours, outgoingRows)

                EZoneType.HVAC                      -> HvacFactory()
                EZoneType.Lighting                  -> LightingFactory()
                EZoneType.Motors                    -> MotorFactory()
                EZoneType.Others                    -> GeneralFactory()
            }
       }
    }
}

class PlugloadFactory(private val utilityRateGas: UtilityRate,
                      private val utilityRateElectricity: UtilityRate,
                      private val usageHours: UsageHours,
                      private val outgoingRows: OutgoingRows) : ComputableFactory() {

    override fun build(): IComputable {
        return when(computable.auditScopeSubType as EApplianceType) {
            EApplianceType.CombinationOven          -> CombinationOven(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows)
            EApplianceType.ConvectionOven           -> ConvectionOven()
            EApplianceType.ConveyorOven             -> ConveyorOven()
            EApplianceType.Fryer                    -> Fryer()
            EApplianceType.IceMaker                 -> IceMaker()
            EApplianceType.RackOven                 -> RackOven()

            EApplianceType.Refrigerator             -> Refrigerator(computable,
                    utilityRateGas, utilityRateElectricity, usageHours, outgoingRows)

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