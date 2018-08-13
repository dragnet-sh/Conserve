package com.gemini.energy.service.device.plugload

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class CombinationOven(private val computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
                      usageHours: UsageHours, outgoingRows: OutgoingRows) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    companion object {
        private const val PRE_RUN_HOURS = 10.0 //@Anthony - Is this going to be a constant ?? pre_run_hours is use.yearly()
    }

    private var isElectric = false
    private var isGas = false

    //@Anthony - These will be populated from the Feature Data
    private var preDaysInOperation = 0.0
    private var preIdleEnergyRate1 = 0.0
    private var preIdleEnergyRate2 = 0.0
    private var preHeatEnergy = 0.0

    //@Anthony - These values are 0 at the moment. Not sure where to call these from ?? they will be populated from featured data as well
    private var preFanEnergyRate = 0.0
    private var postFanEnergyRate = 0.0

    /**
     * Will be called once before anything to initialize the commonly used parameters
     * */
    override fun setupDevice() {

        try {
            val fuelType = featureData["Fuel Type"]!! as String
            isElectric = (fuelType == "Electric")
            isGas = (fuelType == "Gas")

            preDaysInOperation = PRE_RUN_HOURS / 24
            preIdleEnergyRate1 = featureData["Convection Idle Rate"]!! as Double
            preIdleEnergyRate2 = featureData["Steam Idle Rate"]!! as Double
            preHeatEnergy = featureData["Preheat Energy"]!! as Double

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Energy Cost Calculation Formula ToDo: Remove this later
     * */
    override fun cost(vararg params: Any) = 0.0

    /**
     * Cost - Pre State
     * */
    override fun costPreState(): Double {
        var costGas = 0.0
        var costElectricity = 0.0

        val adjustment = if (isGas) 3412 else 1
        val averageIdleRate = (preIdleEnergyRate1 + preIdleEnergyRate2) / 2

        //@Anthony - I have broken down the equation for code maintainability and readability
        //@Anthony - The difference between Gas and Electric Energy was just the adjustment factor - please confirm. 
                      //- confirmed
        //@Anthony - Also we are using the Pre Audit Weekly Usage Hours i.e (energyUsageBusiness) to do Energy Calculations in the Pre Sate. 
                    //yes the weekly hours provided in the pre-audit - I thought that was usage.yearly()??
        val idleEnergy = averageIdleRate * energyUsageBusiness.yearly()

        //@Anthony - Original PreHeatEnergy with just some Adjustment Factor ?? Is It ?? 
                 //correct
      //what is the adjustment value?? should be 3412 for gas and 1 for electricity
        val preHeatEnergy = (preHeatEnergy / 4) * preDaysInOperation
        val fanEnergy = (preFanEnergyRate - postFanEnergyRate) * energyUsageBusiness.yearly() * adjustment
      

        //@Anthony - This energyUsed component is only being used only for Gas - for electricity we are using powerUsed - please confirm ??
               //confirmed
        val energyUsed= idleEnergy + preHeatEnergy + fanEnergy

        //should be just: powerUsed = ((idlePower1 + idlePower2) / 2) + fanPower
        val powerUsed = averageIdleRate + preFanEnergyRate //@Antony - BTW we have Pre and Post Fan Energy Rate - i have used the pre ??

        if (isElectric) {
            val rate = energyUsageBusiness.nonTimeOfUse()

            // electric cost equation is:
            // preHeatEnergy * .25 * summerenergyprice * 365 * .504 + preHeatEnergy * .25 * winterenergyprice * 365 * .496
            // + the equation you have below for computing the electric cost

            val costToPreHeat = (preHeatEnergy / 4) * 365 * (rate.summerNone() * 0.504 + rate.winterNone() * 0.496)
            costElectricity = costElectricity(powerUsed, super.energyUsageBusiness, super.electricityUtilityRate)
            costElectricity += costToPreHeat
        }

        if (isGas) {
            val winterRate = super.gasUtilityRate.structure[ERateKey.GasWinter.value]!![0].toDouble()
            val summerRate = super.gasUtilityRate.structure[ERateKey.GasSummer.value]!![0].toDouble()
            costGas = (energyUsed / 99976.1) * ((winterRate + summerRate) / 2)
        }

        //@Anthony - Where are we getting the water usage value from ?? The input form parameters does not have these ??
        val waterUseConvection = 0.0
        val waterUseSteam = 0.0
        val waterCharge = 0.015

        val costWater = energyUsageBusiness.yearly() * waterUseConvection * waterCharge *
                (waterUseConvection + waterUseSteam) / 2

        return costElectricity + costGas + costWater

    }

    /**
     * Cost - Post State
     * */
    //@Anthony - What about the Post State Energy Calculation - I guess it's no different than the costPreState ??
    //@Anthony - Btw based on the Post Sate we will choose the Most Efficient Alternative.
    override fun costPostState(element: JsonElement): Double = 0.0

    /**
     * PowerTimeChange >> Hourly Energy Use - Pre
     * */
    override fun hourlyEnergyUsagePre(): List<Double> = listOf()

    /**
     * PowerTimeChange >> Hourly Energy Use - Post
     * */
    override fun hourlyEnergyUsagePost(element: JsonElement): List<Double> = listOf()

    /**
     * PowerTimeChange >> Yearly Usage Hours - [Pre | Post]
     * Pre and Post are the same for Refrigerator - 24 hrs
     * */
    override fun usageHoursPre(): Double = energyUsageBusiness.yearly()
    override fun usageHoursPost(): Double = energyUsageSpecific.yearly()

    /**
     * PowerTimeChange >> Energy Efficiency Calculations
     * */
    //@Anthony - Will be implementing the Power Change Next !!
    override fun energyPowerChange(): Double = 0.0
    override fun energyTimeChange(): Double = 0.0
    override fun energyPowerTimeChange(): Double = 0.0

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = JSONObject()
            .put("data.size", featureData["Size (Steam Pans)"])
            .toString()

    /**
     * State if the Equipment has a Post UsageHours Hours (Specific) ie. A separate set of
     * Weekly UsageHours Hours apart from the PreAudit
     * */
    override fun usageHoursSpecific() = true

    /**
     * Define all the fields here - These would be used to Generate the Outgoing Rows or perform the Energy Calculation
     * */
    override fun preAuditFields() = mutableListOf("Number of Vacation days")
    override fun featureDataFields() = mutableListOf("Size (Steam Pans)", "Fuel Type", "Model Number",
            "Company", "Used During Peak", "Age", "Control", "Preheat Energy",
            "Convection Idle Energy", "Steam Idle Rate")

    override fun preStateFields() = mutableListOf<String>()
    override fun postStateFields() = mutableListOf("company", "model_number", "size",
            "fuel_type", "preheat_energy", "convection_idle_rate", "convection_energy_efficiency", "convection_production_capacity",
            "convection_cooking_water_use", "steam_idle_rate", "steam_energy_efficiency", "steam_production_capacity", "steam_cooking_water_use",
            "rebate", "pgne_measure_code", "purchase_price_per_unit", "vendor")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")

}



