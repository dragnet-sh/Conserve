package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.internal.AppSchedulers
import com.gemini.energy.presentation.util.EDay
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

abstract class EBase(private val computable: Computable<*>,
                     private val energyUtilityGas: EnergyUtility,
                     private val energyUtilityElectricity: EnergyUtility,
                     val operatingHours: EnergyUsage,
                     val outgoingRows: OutgoingRows) {

    lateinit var schedulers: Schedulers
    private lateinit var gasUtility: EnergyUtility
    lateinit var electricityUtility: EnergyUtility
    lateinit var preconditions: Preconditions

    lateinit var powerTimeChange: PowerTimeChange

    var preAudit: Map<String, Any> = mapOf()
    var featureData: Map<String, Any> = mapOf()
    private var electricRateStructure: String = RATE

    private val energyUsageBusiness = EnergyUsage()
    private val energyUsageSpecific = EnergyUsage()

    private fun initialize() {
        val base = this

        Log.d(TAG, "<< COMPUTE :: ${identifier()} >> [Start] - (${thread()})")
        Log.d(TAG, computable.toString())

        base.schedulers = AppSchedulers()
        base.featureData = computable.mappedFeatureAuditScope()
        base.preAudit = computable.mappedFeaturePreAudit()

        base.gasUtility = energyUtilityGas.initUtility(Gas()).build()
        base.electricRateStructure = preAudit["Electric Rate Structure"] as String

        Log.d(TAG, "%%%%%%% RATE STRUCTURE CHECKER %%%%%%%")
        Log.d(TAG, electricRateStructure)

        base.electricityUtility = energyUtilityElectricity.initUtility(
                Electricity(electricRateStructure)).build()

        Log.d(TAG, "%%%%%%% OBJECT CHECKER %%%%%%%")
        Log.d(TAG, gasUtility.toString())
        Log.d(TAG, electricityUtility.toString())

        base.operatingHours.initUsage(mappedUsageHours()).build()

        base.energyUsageBusiness.initUsage(mappedBusinessHours()).build()
        base.energyUsageSpecific.initUsage(mappedSpecificHours()).build()

        base.outgoingRows.computable = computable
        base.outgoingRows.dataHolder = mutableListOf()
        base.preconditions = Preconditions()

        base.powerTimeChange = PowerTimeChange()
        base.powerTimeChange.energyUsageSpecific = base.energyUsageSpecific

        /**
         * If the Post Usage Hours is Empty (Specific) - Post Usage Equals to Pre Usage Hours (Business)
         * */
        base.powerTimeChange.energyUsageBusiness = if (usageHoursSpecific()) base.energyUsageBusiness
        else base.energyUsageSpecific

        base.powerTimeChange.featureData = base.featureData

    }

    private fun thread() = Thread.currentThread().name
    private fun identifier() = "${computable.auditScopeType} - ${computable.auditScopeSubType}"


    /**
     * Collect the Various Energy Calculation - Concat them
     * Write the result to the CSV - Emit back Computable
     * */
    fun compute(extra: (param: String) -> Unit): Observable<Computable<*>> {
        initialize()
        validatePreConditions()

        class Mapper : Function<DataHolder, Computable<*>> {
            override fun apply(dataHolder: DataHolder): Computable<*> {
                synchronized(outgoingRows.dataHolder) {
                    if (dataHolder.path.isNotEmpty()) {
                        outgoingRows.dataHolder.add(dataHolder)
                    }
                }
                computable.outgoingRows = outgoingRows
                return computable
            }
        }

        return Observable.concat(calculateEnergyPreState(extra), calculateEnergyPostState(extra),
                calculateEnergySavings(extra), calculateCostSavings(extra))
                .map(Mapper()).doOnComplete {
                    Log.d(TAG, "$$$$$$$ SUPER.COMPUTE.CONCAT.COMPLETE $$$$$$$")
                    outgoingRows.save()
                }

    }

    private fun validatePreConditions() = preconditions.validate()


    /**
     * Compares the following Parameters between Pre | Post
     *
     * 1. Power Change
     * 2. Time Change
     * 3. Power Time Change
     * */
    class PowerTimeChange {

        lateinit var energyUsageSpecific: EnergyUsage
        lateinit var energyUsageBusiness: EnergyUsage
        lateinit var featureData: Map<String, Any>

        var checkPowerChange = false
        var checkTimeChange = false
        var checkPowerTimeChange = false

        var energyPowerChange = 0.0
        var energyTimeChange = 0.0
        var energyPowerTimeChange = 0.0

        fun delta(computable: Computable<*>): PowerTimeChange {

            /**
             * Usage Hours
             * */
            val preRunHours = energyUsageSpecific.yearly()
            val postRunHours = energyUsageBusiness.yearly()

            /**
             * Energy Use
             * */
            val preHourlyEnergyUse = featureData["Daily Energy Used (kWh)"] as Double
            var postHourlyEnergyUse = 0.0

            if (computable.energyPostStateLeastCost.count() > 0) {
                val energyEfficientAlternative = computable.energyPostStateLeastCost[0]
                val energyUse = energyEfficientAlternative.getValue("daily_energy_use")
                postHourlyEnergyUse = energyUse.toDouble()
            }

            /**
             * Compute Power
             * */
            val prePower = preHourlyEnergyUse / 24
            val postPower = postHourlyEnergyUse / 24

            /**
             * Compute Power Time Change Delta
             * If there is no input for the Post Run Hours - Post Run Hours Should Equal to Pre Run Hours
             * */
            energyPowerChange = preRunHours * (prePower - postPower)
            energyTimeChange = (preRunHours - postRunHours) * prePower
            energyPowerTimeChange = (preRunHours - postRunHours) * (prePower - postPower)

            /**
             * Validate Power Time Change - Set the appropriate Flag
             * */
            checkPowerChange = energyPowerChange != 0.0
            checkTimeChange = energyTimeChange != 0.0
            checkPowerTimeChange = energyPowerChange != 0.0 && energyTimeChange != 0.0

            Log.d(TAG, "----::::---- Pre Run Hours : ($preRunHours) ----::::----")
            Log.d(TAG, "----::::---- Post Run Hours : ($postRunHours) ----::::----")
            Log.d(TAG, "----::::---- Pre Power : ($prePower) ----::::----")
            Log.d(TAG, "----::::---- Post Power : ($postPower) ----::::----")

            Log.d(TAG, "----::::---- Energy Power Change : ($energyPowerChange) ----::::----")
            Log.d(TAG, "----::::---- Energy Time Change : ($energyTimeChange) ----::::----")
            Log.d(TAG, "----::::---- Energy Power Time Change : ($energyPowerTimeChange) ----::::----")

            Log.d(TAG, "----::::---- Check Power Change : ($checkPowerChange) ----::::----")
            Log.d(TAG, "----::::---- Check Time Change : ($checkTimeChange) ----::::----")
            Log.d(TAG, "----::::---- Check Power Time Change : ($checkPowerTimeChange) ----::::----")

            return this

        }

        //ToDo : Verify if only one of the following is supposed to be true or we could have more than one true values
        fun energySaving() = when {
            checkPowerChange            -> energyPowerChange
            checkTimeChange             -> energyTimeChange
            checkPowerTimeChange        -> energyPowerTimeChange
            else -> 0.0
        }

    }


    /**
     * Energy Saving - Calculates the Energy Saved via examining the 3 cases
     * Power Change | Time Change | Both Power Time Change
     * Via PowerTimeChange (helper class)
     * */
    private fun calculateEnergySavings(extra: (param: String) -> Unit): Observable<DataHolder> {

        val energySavingHeader = listOf("__check_power_change", "__check_time_change",
                "__check_power_time_change", "__energy_power_change", "__energy_time_change",
                "__energy_power_time_change", "__energy_saving")

        fun initDataHolder(): DataHolder {
            val dataHolderPostState = DataHolder()
            dataHolderPostState.header?.addAll(energySavingHeader)

            dataHolderPostState.computable = computable
            dataHolderPostState.fileName = "${Date().time}_energy_savings.csv"

            return dataHolderPostState
        }

        class Mapper : Function<DataHolder, DataHolder> {
            override fun apply(dataHolder: DataHolder): DataHolder {

                /**
                 * The Power Time Change class takes in the computable and calculates the power delta
                 * */
                val ptc = powerTimeChange.delta(computable)

                Log.d(TAG, "%^%^% Energy Savings Calculation - (${thread()}) %^%^%")
                Log.d(TAG, "Energy Post State [Item Count] : (${computable.energyPostState?.count()})")

                /**
                 * Final Energy Saving
                 * */
                val energySaving = ptc.energySaving()
                Log.d(TAG, "Energy Saving : ($energySaving)")

                /**
                 * Prepare the Outgoing Rows
                 * */
                dataHolder.rows?.add(mapOf(
                        "__check_power_change" to ptc.checkPowerChange.toString(),
                        "__check_time_change" to ptc.checkTimeChange.toString(),
                        "__check_power_time_change" to ptc.checkPowerTimeChange.toString(),
                        "__energy_power_change" to ptc.energyPowerChange.toString(),
                        "__energy_time_change" to ptc.energyTimeChange.toString(),
                        "__energy_power_time_change" to ptc.energyPowerTimeChange.toString(),
                        "__energy_saving" to energySaving.toString()
                ))

                return dataHolder

            }
        }

        return Observable.just(initDataHolder())
                .map(Mapper())
    }


    /**
     * Energy Cost Saving
     * */
    private fun calculateCostSavings(extra: (param: String) -> Unit): Observable<DataHolder> {

        val energyCostSavingHeader = listOf("__energy_cost_saving", "__demand_cost_saving",
                "__implementation_cost", "__total_cost_saved", "__payback_period_months", "__payback_period_years")

        fun initDataHolder(): DataHolder {
            val dataHolderPostState = DataHolder()
            dataHolderPostState.header?.addAll(energyCostSavingHeader)

            dataHolderPostState.computable = computable
            dataHolderPostState.fileName = "${Date().time}_energy_cost_savings.csv"

            return dataHolderPostState
        }

        class Mapper : Function<DataHolder, DataHolder> {

            override fun apply(dataHolder: DataHolder): DataHolder {

                Log.d(TAG, "----::::---- $computable ----::::----")

                /**
                 * Pre Usage Hours - Mapped Peak Hours (Specific)
                 * */
                val preUsageByPeak = energyUsageSpecific.mappedPeakHourYearly()
                val preHoursOnPeakPricing = preUsageByPeak[ERateKey.SummerOn]!! * .504
                val preHoursOnPartPeakPricing = preUsageByPeak[ERateKey.SummerPart]!! * .504 +
                        preUsageByPeak[ERateKey.WinterPart]!! * .496 + preUsageByPeak[ERateKey.SummerOn]!! * .496
                val preHoursOnOffPeakPricing = preUsageByPeak[ERateKey.SummerOff]!! * .504 +
                        preUsageByPeak[ERateKey.WinterOff]!! * .496

                Log.d(TAG, "----::::---- Pre Usage By Peak ($preUsageByPeak) ----::::----")
                Log.d(TAG, "----::::---- Pre Hours On Peak Pricing ($preHoursOnPeakPricing) ----::::----")
                Log.d(TAG, "----::::---- Pre Hours On Part Peak Pricing ($preHoursOnPartPeakPricing) ----::::----")
                Log.d(TAG, "----::::---- Pre Hours On Off Peak Pricing ($preHoursOnOffPeakPricing) ----::::----")

                /**
                 * Post Usage Hours - Mapped Peak Hours (Business)
                 * */
                val postUsageByPeak = energyUsageBusiness.mappedPeakHourYearly()
                val postHoursOnPeakPricing = postUsageByPeak[ERateKey.SummerOn]!! * .504
                val postHoursOnPartPeakPricing = postUsageByPeak[ERateKey.SummerPart]!! * .504 +
                        postUsageByPeak[ERateKey.WinterPart]!! * .496 + postUsageByPeak[ERateKey.SummerOn]!! * .496
                val postHoursOnOffPeakPricing = postUsageByPeak[ERateKey.SummerOff]!! * .504 +
                        postUsageByPeak[ERateKey.WinterOff]!! * .496

                Log.d(TAG, "----::::---- Post Usage By Peak ($postUsageByPeak) ----::::----")
                Log.d(TAG, "----::::---- Post Hours On Peak Pricing ($postHoursOnPeakPricing) ----::::----")
                Log.d(TAG, "----::::---- Post Hours On Part Peak Pricing ($postHoursOnPartPeakPricing) ----::::----")
                Log.d(TAG, "----::::---- Post Hours On Off Peak Pricing ($postHoursOnOffPeakPricing) ----::::----")

                /**
                 * Utility Rate - Electricity
                 * ToDo - Verify if dividing by 2 is correct to find the average between the Summer | Winter Rate
                 * */

                var peakPrice = 0.0
                var partPeakPrice = 0.0
                var offPeakPrice = 0.0

                if (electricRateStructure.matches("^.*TOU$".toRegex())) {

                    peakPrice = electricityUtility.structure[ERateKey.SummerOn.value]!![0].toDouble()
                    partPeakPrice =
                            (electricityUtility.structure[ERateKey.SummerPart.value]!![0].toDouble()
                                    + electricityUtility.structure[ERateKey.WinterPart.value]!![0].toDouble()) / 2
                    offPeakPrice =
                            (electricityUtility.structure[ERateKey.SummerOff.value]!![0].toDouble()
                                    + electricityUtility.structure[ERateKey.WinterOff.value]!![0].toDouble()) / 2
                }

                Log.d(TAG, "----::::---- Electricity Peak Price ($peakPrice) ----::::----")
                Log.d(TAG, "----::::---- Electricity Part Peak Price ($partPeakPrice) ----::::----")
                Log.d(TAG, "----::::---- Electricity Off Peak Price ($offPeakPrice) ----::::----")

                /**
                 * Utility Rate - Gas
                 * */
                val winterRate = gasUtility.structure[ERateKey.GasWinter.value]!![0].toDouble()
                val summerRate = gasUtility.structure[ERateKey.GasSummer.value]!![0].toDouble()

                Log.d(TAG, "----::::---- Gas Winter Rate ($winterRate) ----::::----")
                Log.d(TAG, "----::::---- Gas Summer Rate ($summerRate) ----::::----")

                /**
                 * Parse API Energy Efficient Database - materialCost
                 * Parse API Labor Cost - laborCost
                 * */
                fun purchasePricePerUnit(): Double {
                    if ((computable.energyPostStateLeastCost.count() > 0) &&
                            computable.energyPostStateLeastCost[0].containsKey("purchase_price_per_unit")) {

                        try {
                            return computable.energyPostStateLeastCost[0].getValue("purchase_price_per_unit").toDouble()
                        } catch (exception: Exception) {
                            Log.e(TAG, "Exception @ Purchase Price Unit - toDouble")
                        }

                    }

                    return 0.0
                }

                val materialCost = purchasePricePerUnit()
                Log.d(TAG, "----::::---- Material Cost ($materialCost) ----::::----")

                val laborCost = computable.laborCost
                Log.d(TAG, "----::::---- Labor Cost ($laborCost) ----::::----")

                /**
                 * ToDo - Where do we get these values from ??
                 * */
                val maintenanceCostSavings = 0.0
                val otherEquipmentSavings = 0.0

                Log.d(TAG, "----::::---- Maintenance Cost Savings ($maintenanceCostSavings) ----::::----")
                Log.d(TAG, "----::::---- Other Equipment Savings ($otherEquipmentSavings) ----::::----")

                /**
                 * Parse API Energy Efficient Database - Rebate
                 * */
                fun rebate(): Double {
                    if ((computable.energyPostStateLeastCost.count() > 0) &&
                            computable.energyPostStateLeastCost[0].containsKey("rebate")) {

                        try {
                            return computable.energyPostStateLeastCost[0].getValue("rebate").toDouble()
                        } catch (exception: Exception) {
                            Log.e(TAG, "Exception @ Rebate - toDouble")
                        }

                    }

                    return 0.0
                }
                val incentives = rebate()

                Log.d(TAG, "----::::---- Incentive ($incentives) ----::::----")


                /**
                 * Fetch these from the Utility Rate Structure
                 * ToDo - Verify if A1 | A10 | E19 qualify as Non TOU
                 * ToDo - Verify if dividing by 2 is correct to find the average between the Summer | Winter Rate
                 * ToDo - Demand Charge can be Empty - Right now i have added 0 in the CSV for the empty ones - need to fix this
                 * */
              //We can not use blended energy rate because winter and summer energy are different. Therefore we need two
              //distinct rates for summer and winter [Kinslow]
                var blendedEnergyRate = 0.0
                if (!electricRateStructure.matches("^.*TOU$".toRegex())) {
                    blendedEnergyRate = (electricityUtility.structure[ERateKey.SummerNone.value]!![0].toDouble() +
                            electricityUtility.structure[ERateKey.WinterNone.value]!![0].toDouble()) / 2
                }


                var blendedEnergyRateSummer = 0.0
                var blendedEnergyRateWinter = 0.0
                if (!electricRateStructure.matches("^.*TOU$".toRegex())) {
                    blendedEnergyRateSummer = electricityUtility.structure[ERateKey.SummerNone.value]!![0].toDouble()
                    blendedEnergyRateWinter = electricityUtility.structure[ERateKey.WinterNone.value]!![0].toDouble()
                }

                Log.d(TAG, "----::::---- Blended Energy Rate ($blendedEnergyRate) ----::::----")

                val blendedDemandRate = if (!electricRateStructure.matches("^.*TOU$".toRegex())) {
                    (electricityUtility.structure[ERateKey.SummerNone.value]!![2].toDouble() +
                            electricityUtility.structure[ERateKey.WinterNone.value]!![2].toDouble()) / 2
                } else {
                    (electricityUtility.structure[ERateKey.SummerOff.value]!![2].toDouble() +
                            electricityUtility.structure[ERateKey.WinterOff.value]!![2].toDouble()) / 2
                }

                Log.d(TAG, "----::::---- Blended Demand Rate ($blendedDemandRate) ----::::----")

                /**
                 * Utility Rate Structure
                 * */
                val getRateSchedule = electricRateStructure

                Log.d(TAG, "----::::---- Rate Schedule ($getRateSchedule) ----::::----")

                /**
                 * Flag to denote a gas based equipment
                 * ToDo - Create input and get it from the feature data
                 * */
                val checkForGas = false

                Log.d(TAG, "----::::---- Check For Gas ($checkForGas) ----::::----")

                /**
                 * Energy Usage - Efficient Alternative (Post State)
                 * */
                fun energyUse() = if (computable.energyPostStateLeastCost.count() > 0) {
                    val energyEfficientAlternative = computable.energyPostStateLeastCost[0]
                    energyEfficientAlternative.getValue("daily_energy_use").toDouble()
                } else {
                    0.0
                }

                val energyUse = energyUse()

                Log.d(TAG, "----::::---- Energy Use ($energyUse) ----::::----")

                /**
                 * Power, Time Change or Both
                 * */
                val ptc = powerTimeChange.delta(computable)
                val powerChangeCheck = ptc.checkPowerChange
                val timeChangeCheck = ptc.checkTimeChange
                val powerTimeChangeCheck = ptc.checkPowerTimeChange

                Log.d(TAG, "----::::---- Power Change Check ($powerChangeCheck) ----::::----")
                Log.d(TAG, "----::::---- Time Change Check ($timeChangeCheck) ----::::----")
                Log.d(TAG, "----::::---- Power Time Change Check ($powerTimeChangeCheck) ----::::----")


                /**
                 * Applicable to Post State having multiple Energy Column
                 * It's false for most of the devices except - Oven (Need to verify which other devices' are applicable)
                 * ToDo - Need to set this dynamically - This is true for cases with multiple Power Values i.e Oven at the moment
                 * */
                val multiplePowerCheck = false
                val multipleTimeCheck = false
                val multiplePowerTimeCheck = false

                Log.d(TAG, "----::::---- Multiple Power Check ($multiplePowerCheck) ----::::----")
                Log.d(TAG, "----::::---- Multiple Time Check ($multipleTimeCheck) ----::::----")
                Log.d(TAG, "----::::---- Multiple Power Time Check ($multiplePowerTimeCheck) ----::::----")

                /**
                 * Power Value
                 * Case 1 : Multiple Power | Time Check -- @powerValues
                 * Case 2 : Single Power | Time -- @powerValue
                 * ToDo - Need to Populate these from the Post State - [Kinslow] Will do it when implementing any device with two energy e.g., idle and preheat
                 * */
                val powerValues = listOf(0.0, 0.0)
                val powerValue = 0.0

                Log.d(TAG, "----::::---- Power Values ($powerValues) ----::::----")
                Log.d(TAG, "----::::---- Power Value ($powerValue) ----::::----")


                /**
                 * <<< Energy Cost Saving >>>
                 * */
                fun energyCostSaving(): Double {

                    /**
                     * Energy Cost Savings Calculations - [TOU Based Savings]
                     *
                     * Multiple Power - Time
                     * Single Power - Time
                     * */
                  //PowerValue represents the change
                    fun electricityCostsCalcMultiplePowerChange(powerValues: List<Double>): Double {
                        var cost = 0.0
                        powerValues.forEach { powerValue ->
                            cost +=
                                    preHoursOnPeakPricing * powerValue * peakPrice +
                                    preHoursOnPartPeakPricing * powerValue * partPeakPrice +
                                    preHoursOnOffPeakPricing * powerValue * offPeakPrice
                        }

                        return cost
                    }

                    fun electricityCostsCalcMultipleTimeChange(powerValues: List<Double>): Double {
                        var cost = 0.0
                        val deltaPeak = preHoursOnPeakPricing - postHoursOnPeakPricing
                        val deltaPartPeak = preHoursOnPartPeakPricing - postHoursOnPartPeakPricing
                        val deltaOffPeak = preHoursOnOffPeakPricing - postHoursOnOffPeakPricing
                        powerValues.forEach { powerValue ->
                            cost +=
                                    deltaPeak * powerValue * peakPrice +
                                    deltaPartPeak * powerValue * partPeakPrice +
                                    deltaOffPeak * powerValue * offPeakPrice
                        }

                        return cost
                    }

                    fun electricityCostsCalcPowerChange(powerValue: Double) =
                            electricityCostsCalcMultiplePowerChange(listOf(powerValue))

                    fun electricityCostsCalcTimeChange(powerValue: Double) =
                            electricityCostsCalcMultipleTimeChange(listOf(powerValue))

                    /**
                     * Energy Cost Savings - Case 1 : TOU Based
                     * */
                  //Return whatever is true [Kinslow]
                  //If powerchange return powerchange & if timechange return timechange & if powertimechange return powertimechange [Kinslow]
                    fun findTimeOfUseCostSavings() = when {

                        multiplePowerCheck -> electricityCostsCalcMultiplePowerChange(powerValues)
                        multipleTimeCheck || multiplePowerTimeCheck -> electricityCostsCalcMultipleTimeChange(powerValues)

                        powerChangeCheck -> electricityCostsCalcPowerChange(powerValue)
                        timeChangeCheck || powerTimeChangeCheck -> electricityCostsCalcTimeChange(powerValue)

                        else -> 0.0

                    }

                    /**
                     * Energy Cost Savings - Case 2 : Non TOU Based
                     * */
                  //Need to remove blendedEnergyRate for Summer and Winter and energyUse should be seperated too [Kinslow]
                    fun findNonTimeOfUseCostSavings(energyUse: Double) = energyUse * blendedEnergyRate

                    /**
                     * Energy Cost Savings - Case 3 : Gas Based
                     * */
                    fun gasCostSavings(energyUse: Double) =
                            (energyUse / 99976.1) * ((winterRate + summerRate)) / 2

                    /**
                     * Main Block
                     * */
                    val matchTimeOfUse = getRateSchedule.matches("^.*TOU$".toRegex())

                    fun negate(flag: Boolean) = !flag
                    var energyCostSavings = 0.0
                    energyCostSavings = when {

                        matchTimeOfUse -> findTimeOfUseCostSavings()
                        negate(matchTimeOfUse) -> findNonTimeOfUseCostSavings(energyUse)
                        checkForGas -> gasCostSavings(energyUse)
                        else -> 0.0

                    }

                    return energyCostSavings

                }


                /**
                 * <<< Demand Cost Saving >>>
                 * */
                fun demandCostSaving(): Double {

                    fun demandCostSavingsYearCalc(power: Double) = blendedDemandRate * power * 12

                    var demandCostSavings = 0.0
                    demandCostSavings = when {
                        powerValue > 0 && powerValues.count() == 0 -> demandCostSavingsYearCalc(powerValue)
                        powerValue <= 0 && powerValues.count() > 0 -> demandCostSavingsYearCalc(powerValues[0])
                        else -> 0.0
                    }

                    return demandCostSavings

                }


                /**
                 * <<< Implementation Cost >>>
                 * */
                fun implementationCost() = (materialCost + laborCost) - incentives


                /**
                 * <<< Total Cost Saved >>>
                 * */
                fun totalCostSaved() = energyCostSaving() + maintenanceCostSavings + otherEquipmentSavings + demandCostSaving()


                /**
                 * <<< Payback Period - Months >>>
                 * */
                fun paybackPeriodMonths() = (implementationCost() / totalCostSaved()) / 12


                /**
                 * <<< Payback Period - Years >>>
                 * */
                fun paybackPeriodYears() = (implementationCost() / totalCostSaved())


                /**
                 * Prepare the Outgoing Rows
                 * */
                dataHolder.rows?.add(mapOf(
                        "__energy_cost_saving" to energyCostSaving().toString(),
                        "__demand_cost_saving" to demandCostSaving().toString(),
                        "__implementation_cost" to implementationCost().toString(),
                        "__total_cost_saved" to totalCostSaved().toString(),
                        "__payback_period_months" to paybackPeriodMonths().toString(),
                        "__payback_period_years" to paybackPeriodYears().toString()
                ))

                return dataHolder
            }
        }

        fun prerequisite() = laborCost(queryLaborCost())
                .flatMap { response ->
                    val jsonElements = response.map { it.asJsonObject.get("data") }
                    if (jsonElements.count() > 0) {
                        val cost = jsonElements[0].asJsonObject.get("cost").asDouble
                        computable.laborCost = cost
                    }
                    Observable.just(initDataHolder())
                }

        return prerequisite().map(Mapper())
    }

    /**
     * Pre State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPreState(extra: (param: String) -> Unit): Observable<DataHolder> {

        fun initDataHolder(): DataHolder {
            val dataHolderPreState = DataHolder()

            dataHolderPreState.header?.addAll(featureDataFields())
            dataHolderPreState.computable = computable
            dataHolderPreState.fileName = "${Date().time}_pre_state.csv"

            return dataHolderPreState
        }

        Log.d(TAG, "%^%^% Pre-State Energy Calculation - (${thread()}) %^%^%")
        val dataHolderPreState = initDataHolder()
        val preRow = mutableMapOf<String, String>()
        featureDataFields().forEach { field ->
            preRow[field] = if (featureData.containsKey(field)) featureData[field].toString() else ""
        }

        val dailyEnergyUsed = featureData["Daily Energy Used (kWh)"]
        dailyEnergyUsed?.let {
            val cost = cost(it)
            dataHolderPreState.header?.add("__electric_cost")
            preRow["__electric_cost"] = cost.toString()
        }

        dataHolderPreState.rows?.add(preRow)
        computable.energyPreState = preRow

        Log.d(TAG, "## Data Holder - PRE STATE - (${thread()}) ##")
        Log.d(TAG, dataHolderPreState.toString())

        return Observable.just(dataHolderPreState)

    }


    /**
     * Post State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPostState(extra: (param: String) -> Unit): Observable<DataHolder> {

        class Mapper : Function<JsonArray, DataHolder> {

            override fun apply(response: JsonArray): DataHolder {

                Log.d(TAG, "%^%^% Post-State Energy Calculation - (${thread()}) %^%^%")
                Log.d(TAG, "### Efficient Alternate Count - [${response.count()}] - ###")

                val jsonElements = response.map { it.asJsonObject.get("data") }
                val dataHolderPostState = initDataHolder()
                val costCollector = mutableListOf<Double>()

                jsonElements.forEach { element ->
                    val postRow = mutableMapOf<String, String>()
                    postStateFields().forEach { key ->
                        var value = ""
                        if (element.asJsonObject.has(key)) {
                            value = element.asJsonObject.get(key).asString
                        }
                        postRow[key] = value
                    }

                    val postDailyEnergyUsed = element.asJsonObject.get("daily_energy_use").asDouble
                    val cost = cost(postDailyEnergyUsed)
                    postRow["__electric_cost"] = cost.toString()

                    costCollector.add(cost)
                    dataHolderPostState.rows?.add(postRow)
                    computable.energyPostState?.add(postRow)
                }

                Log.d(TAG, "## Data Holder - POST STATE  - (${thread()}) ##")
                Log.d(TAG, dataHolderPostState.toString())

                val costMinimum = costCollector.min()
                val efficientAlternative = dataHolderPostState.rows?.filter {
                    it.getValue("__electric_cost").toDouble() == costMinimum
                }

                computable.energyPostStateLeastCost = efficientAlternative ?: mutableListOf()
                Log.d(TAG, "Minimum Cost : [$costMinimum]")
                Log.d(TAG, "Efficient Alternative : ${computable.energyPostStateLeastCost}")

                return dataHolderPostState
            }

            private fun initDataHolder(): DataHolder {
                val dataHolderPostState = DataHolder()
                dataHolderPostState.header = postStateFields()
                dataHolderPostState.header?.add("__electric_cost")

                dataHolderPostState.computable = computable
                dataHolderPostState.fileName = "${Date().time}_post_state.csv"

                return dataHolderPostState
            }

        }

        return starValidator(queryEnergyStar())
                .flatMap {
                    if (it && efficientLookup()) {
                        efficientAlternative(queryEfficientFilter()).map(Mapper())
                    } else {
                        Observable.just(DataHolder())
                    }
                }
    }

    companion object {
        private const val TAG = "EBase"
        private const val RATE = "A-1 TOU"
    }

    abstract fun queryEfficientFilter(): String
    abstract fun efficientLookup(): Boolean
    abstract fun usageHoursSpecific(): Boolean

    abstract fun preAuditFields(): MutableList<String>
    abstract fun featureDataFields(): MutableList<String>
    abstract fun preStateFields(): MutableList<String>
    abstract fun postStateFields(): MutableList<String>
    abstract fun computedFields(): MutableList<String>

    abstract fun cost(vararg params: Any): Double

    private fun queryEnergyStar() = JSONObject()
            .put("data.company", featureData["Company"])
            .put("data.model_number", featureData["Model Number"])
            .toString()

    //ToDo: Verify where the Labor Cost Parameters are supposed to go.
    private fun queryLaborCost() = JSONObject()
            .put("data.zipcode", featureData["ZipCode"] ?: 0)
            .put("data.profession", featureData["Profession"] ?: "none")
            .toString()

    private fun starValidator(query: String): Observable<Boolean> {
        return parseAPIService.fetchPlugload(query)
                .map { it.getAsJsonArray("results").count() == 0 }
                .toObservable()
    }

    private fun efficientAlternative(query: String): Observable<JsonArray> {
        return parseAPIService.fetchPlugload(query)
                .map { it.getAsJsonArray("results") }
                .toObservable()
    }

    private fun laborCost(query: String): Observable<JsonArray> {
        return parseAPIService.fetchLaborCost(query)
                .map { it.getAsJsonArray("results") }
                .toObservable()
    }


    /**
     * Get the Weekly Hours Map Ready
     * This should decide where to look - [PreAudit or Individual]
     * */
    private fun mappedUsageHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (preAudit.containsKey(eDay.value)) {
                usage.add(preAudit[eDay.value] as String)
            } else {
                usage.add("")
            }
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    private fun mappedBusinessHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (preAudit.containsKey(eDay.value)) {
                usage.add(preAudit[eDay.value] as String)
            } else {
                usage.add("")
            }
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    private fun mappedSpecificHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (featureData.containsKey(eDay.value)) {
                usage.add(featureData[eDay.value] as String)
            } else {
                usage.add("")
            }
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    //ToDo - ReWrite this later !!
    fun costElectricity(powerUsed: Double, usage: EnergyUsage, utility: EnergyUtility): Double {
        val regex = "^.*TOU$".toRegex()
        val usageByPeak = usage.mappedPeakHourYearly()
        val usageByYear = usage.yearly()

        if (electricRateStructure.matches(regex)) {

            var summer = usageByPeak[ERateKey.SummerOn]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOn.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerPart]!! * .504 * powerUsed * utility.structure[ERateKey.SummerPart.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerOff]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOff.value]!![0].toDouble()

            var winter = usageByPeak[ERateKey.WinterPart]!! * .496 * powerUsed * utility.structure[ERateKey.WinterPart.value]!![0].toDouble()
            winter += usageByPeak[ERateKey.WinterOff]!! * .496 * powerUsed * utility.structure[ERateKey.WinterOff.value]!![0].toDouble()

            return (summer + winter)

        } else {

            Log.d(TAG, "##### Non TOU - COST Calculation #####")
            Log.d(TAG, ">>> Usage By Year : $usageByYear")
            Log.d(TAG, ">>> Power Used : $powerUsed")
            Log.d(TAG, ">>> Rate Summer : ${utility.structure[ERateKey.SummerNone.value]!![0]}")
            Log.d(TAG, ">>> Rate Winter : ${utility.structure[ERateKey.WinterNone.value]!![0]}")

            val summer = usageByYear * .504 * powerUsed * utility.structure[ERateKey.SummerNone.value]!![0].toDouble()
            val winter = usageByYear * .496 * powerUsed * utility.structure[ERateKey.WinterNone.value]!![0].toDouble()

            Log.d(TAG, ">>> Total Cost : ${summer + winter}")

            return (summer + winter)

        }

    }

    /**
     * Parse API Service ToDo: Move this to the Network Layer !!
     * */
    private val parseAPIService by lazy { ParseAPI.create() }

    class ParseAPI {

        interface ParseAPIService {
            @GET("classes/PlugLoad")
            fun fetchPlugload(@Query("where") where: String): Single<JsonObject>

            @GET("classes/LaborCost")
            fun fetchLaborCost(@Query("where") where: String): Single<JsonObject>
        }

        companion object {
            private const val applicationId = "47f916f7005d19ddd78a6be6b4bdba3ca49615a0"
            private const val masterKey = "NLI214vDqkoFTJSTtIE2xLqMme6Evd0kA1BbJ20S"

            private val okHttpClient = OkHttpClient()
                    .newBuilder()
                    .addInterceptor {
                        val original = it.request()
                        val request = original.newBuilder()
                                .header("User-Agent", "OkHttp Headers.java")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("X-Parse-Application-Id", applicationId)
                                .addHeader("X-Parse-REST-API-Key", masterKey)
                                .build()
                        it.proceed(request)
                    }

            fun create(): ParseAPIService {
                val retrofit = Retrofit.Builder()
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("http://ec2-18-220-200-115.us-east-2.compute.amazonaws.com:80/parse/")
                        .client(okHttpClient.build())
                        .build()

                return retrofit.create(ParseAPIService::class.java)
            }

        }
    }
}
