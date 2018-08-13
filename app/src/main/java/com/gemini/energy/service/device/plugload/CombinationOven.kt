package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
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

    companion object {

        /**
         * It takes 15 min to Pre Heat the Oven but the Energy Value Provided is for an Hour
         * Thus need to multiply by 1/4
         * */
        private const val ADJUSTMENT_PRE_HEAT = 0.25
        private const val ADJUSTMENT_GAS = 3412

        /**
         * The water charge is .015 only for San Francisco area.
         * Later we will need to make a sheet that will be used to determine the water charge based on city
         * */
        private const val WATER_CHARGE = 0.015

    }

    private var isElectric = false
    private var isGas = false

    /**
     * This is going to be the Business Hours (Pre Audit Operation Hours)
     * - the number of hours the client is open for business over a year
     * - each client could have a unique value for pre run hours as they work different hours
     * */
    private var daysInOperation = 0.0

    private var preIdleEnergyRateConvection = 0.0
    private var preIdleEnergyRateSteam = 0.0

    /**
     * The Adjustment Factor is taken care within the setup()
     * */
    private var preHeatEnergy = 0.0

    //ToDo - Populate these from the Feature Data
    private var preFanEnergyRate = 0.0
    private var postFanEnergyRate = 0.0

    //ToDo - Populate these from the Feature Data
    private var waterUseConvection = 0.0
    private var waterUseSteam = 0.0

    /**
     * To be used for Efficient Query Filter
     * */
    private var steamPanSize = -99.99


    /**
     * Will be called once before anything to initialize the commonly used parameters
     * */
    override fun setup() {

        fun adjustPreHeat(value: Double) = value * ADJUSTMENT_PRE_HEAT

        try {
            val fuelType = featureData["Fuel Type"]!! as String
            isElectric = (fuelType == "Electric")
            isGas = (fuelType == "Gas")

            //ToDo: DaysInOperation
            //This does not work. See bottom of yesterdays detailed email for clarification.
            //Also Days in operation does not change between pre and post so you can just call the variable DaysInOperation...
            daysInOperation = usageHoursPre() / 24

            preIdleEnergyRateConvection = featureData["Convection Idle Rate"]!! as Double
            preIdleEnergyRateSteam = featureData["Steam Idle Rate"]!! as Double

            val rawPreHeatEnergy = featureData["Preheat Energy"]!! as Double
            preHeatEnergy = adjustPreHeat(rawPreHeatEnergy)

            steamPanSize = featureData["Size (Steam Pans)"]!! as Double

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

        val adjustment = if (isGas) ADJUSTMENT_GAS else 1
        val averageIdleRate = (preIdleEnergyRateConvection + preIdleEnergyRateSteam) / 2

        val yearlyIdleEnergy = averageIdleRate * usageHoursPre()
        val yearlyPreHeatEnergy = preHeatEnergy * daysInOperation // Adjusted preheat energy is per day over here
        val yearlyFanEnergy = (preFanEnergyRate - postFanEnergyRate) * usageHoursPre() * adjustment

        val energyUsed= yearlyIdleEnergy + yearlyPreHeatEnergy + yearlyFanEnergy
        val powerUsed = averageIdleRate + preFanEnergyRate // Needed to identify PreEnergy Usage thus PreFanEnergy

        // >>> 1. Cost Electricity
        if (isElectric) {

            //@Anthony - What happens when the Audit Rate Structure is TOU ??
            // - Now costElectricity is using A1-TOU
            // - costToPreHeat should be using A1 | A10 .. ??
            // - Should we just do an average of whatever the rate structure is TOU or Non TOU ??
            val rate = if (isTOU()) electricityRate.timeOfUse() else electricityRate.nonTimeOfUse()

            //@Anthony - Do you think we need to multiply by 365 as we have already multiplied by yearly daysInOperation
            val costToPreHeat = yearlyPreHeatEnergy * 365 * rate.weightedAverage()

            costElectricity = costElectricity(powerUsed, usageHoursBusiness, electricityRate)
            costElectricity += costToPreHeat
        }

        // >>> 2. Cost Gas
        if (isGas) {
            costGas = costGas(energyUsed)
        }

        // >>> 3. Cost Water
        val averageWaterUsed = (waterUseConvection + waterUseSteam) / 2
        val costWater = usageHoursPre() * WATER_CHARGE * averageWaterUsed

        return (costElectricity + costGas + costWater)

    }

    /**
     * Cost - Post State
     * */
    // The post state cost or energy calculation?? for energy I provided the location for that in the last email.
    // Cost is the same process as the pre
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
     * */
    override fun usageHoursPre(): Double = usageHoursBusiness.yearly()
    override fun usageHoursPost(): Double = usageHoursSpecific.yearly()

    /**
     * PowerTimeChange >> Energy Efficiency Calculations
     * For Ovens there is no Time Change nor Power Time Change
     * */
    override fun energyPowerChange(): Double = 0.0
    override fun energyTimeChange(): Double = 0.0
    override fun energyPowerTimeChange(): Double = 0.0

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = JSONObject()
            .put("data.size", steamPanSize)
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



