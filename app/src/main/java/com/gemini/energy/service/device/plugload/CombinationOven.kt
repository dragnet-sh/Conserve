package com.gemini.energy.service.device.plugload

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class CombinationOven(computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
                      usageHours: UsageHours, outgoingRows: OutgoingRows) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    /**
     * Energy Cost Calculation Formula
     * @Anthony - Please verify the Cost Function in Detail.
     *
     * ToDo - Note :: Need to make some slight changes to the EBase so that Cost function that is being defined here can be
     * ToDo - reused by the Energy Efficient Alternative (It's already been done to a certain extent while implementing refrigerator)
     * */
    override fun cost(vararg params: Any): Double {
        var powerUsed = 0.0
        var energyUsed = 0.0
        var waterUseConvection = 0.0
        var waterUseSteam = 0.0

        var isElectric = false
        var isGas = false

        try {
            val preHeatEnergy = featureData["Pre-Heat Energy"]!! as Double
            val idleEnergy = featureData["Idle Energy"]!! as Double
            val fuelType = featureData["Fuel Type"]!! as String

            isElectric = (fuelType == "Electric")
            isGas = (fuelType == "Gas")

            // @Anthony (I need the formulas !!)
            // 1. How do i use the pre_heat and idle_energy rate to calculate the Cost ?? The iOS one doesn't make sense. (Formula both for Electric and Gas)
            // 2. This Equipment has either Gas or Electric. Need to implement the Gas Calculation over here inside this function.
            // 3. What will be the Usage Time to multiply with the Energy (PreHeat | Idle) ??

            // ** Total Cost for Pre Heat Energy -- ?? the pre heat energy is only for the first 15mins of each day.
            // ** Total Cost for Idle Heat Energy -- ?? - the idle rates for all ovens are given as power values already so you just use them as they are
            
          // electric cost equation is: 
                    // preHeatEnergy * .25 * summerenergyprice * 365 * .504 + preHeatEnergy * .25 * winterenergyprice * 365 * .496
                    // + the equation you have below for computing the electric cost
          
          // gas cost equation is fine as you have it. 
          
          
            energyUsed = preHeatEnergy + idleEnergy // see email for correct energy equations for gas and electricity
            powerUsed = energyUsed / 24 //should be just: powerUsed = ((idlePower1 + idlePower2) / 2) + fanPower
                      //the preHeatEnergy must stay seperate because it is only once a day as opposed to by hour
                      // this powerUsed equation I provided is specifically for electric ovens!!!

            waterUseConvection = featureData["Water Use (Convection)"]!! as Double
            waterUseSteam = featureData["Water Use (Steam)"]!! as Double

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val waterCharge = .015
        val usage = super.energyUsageSpecific
        var costElectricity = 0.0
        var costGas = 0.0

        /**
         * Computes the Electric Cost
         * */
        if (isElectric) {
            costElectricity = costElectricity(powerUsed, super.energyUsageSpecific, electricityUtilityRate)
        }

        /**
         * Computes the Gas Cost
         * */
        if (isGas) {
            val winterRate = super.gasUtilityRate.structure[ERateKey.GasWinter.value]!![0].toDouble()
            val summerRate = super.gasUtilityRate.structure[ERateKey.GasSummer.value]!![0].toDouble()
            costGas = (energyUsed / 99976.1) * ((winterRate + summerRate) / 2) //use the gasenergyUsed to specify the gas equation I provided (see email) 
        }

        /**
         * Computes the Water Cost
         * */
        val costWater = usage.yearly() * waterUseConvection * waterCharge * (waterUseConvection + waterUseSteam) / 2

        /**
         * Total Cost - Gas + Electric + Water (Gas or Electric will be Zero Based on the Fuel Type)
         * */
        return costElectricity + costGas + costWater
    }

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = JSONObject()

            //Exact Match :: Size - Steam Pan
            .put("data.size", featureData["Size (Steam Pans)"])

            //Tie Breaker :: Production Capacity [Convection]
            .put("data.convection_production_capacity", JSONObject()
                    .put("\$gte", featureData["Production Capacity (Convection)"] as Double))

            //Tie Breaker :: Production Capacity [Steam]
            .put("data.steam_production_capacity", JSONObject()
                    .put("\$gte", featureData["Production Capacity (Steam)"] as Double))

            .toString()


    /**
     * State if the Equipment has a Post UsageHours Hours (Specific) ie. A separate set of
     * Weekly UsageHours Hours apart from the PreAudit
     * */
    override fun usageHoursSpecific() = true

    /**
     * Define all the fields here - These would be used to Generate the Outgoing Rows.
     * */
    override fun preAuditFields() = mutableListOf("Number of Vacation days")
    override fun featureDataFields() = mutableListOf("Size (Steam Pans)", "Fuel Type", "Model Number",
            "Company", "Used During Peak", "Age", "Control", "Production Capacity (Convection)",
            "Production Capacity (Steam)", "Pre-Heat Energy", "Idle Energy", "Water Use (Convention)",
            "Water Use (Steam)")

    override fun preStateFields() = mutableListOf<String>()
    override fun postStateFields() = mutableListOf("company", "model_number", "size",
            "fuel_type", "preheat_energy", "convection_idle_rate", "convection_energy_efficiency", "convection_production_capacity",
            "convection_cooking_water_use", "steam_idle_rate", "steam_energy_efficiency", "steam_production_capacity", "steam_cooking_water_use",
            "rebate", "pgne_measure_code", "purchase_price_per_unit", "vendor")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")


}
