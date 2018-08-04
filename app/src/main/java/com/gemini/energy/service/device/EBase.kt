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
                     private val energyUtility: EnergyUtility,
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

        base.gasUtility = energyUtility.initUtility(Gas()).build()
        base.electricRateStructure = preAudit["Electric Rate Structure"] as String
        base.electricityUtility = energyUtility.initUtility(
                Electricity(electricRateStructure)).build()

        base.operatingHours.initUsage(mappedUsageHours()).build()

        base.energyUsageBusiness.initUsage(mappedBusinessHours()).build()
        base.energyUsageSpecific.initUsage(mappedSpecificHours()).build()

        base.outgoingRows.computable = computable
        base.outgoingRows.dataHolder = mutableListOf()
        base.preconditions = Preconditions()

        base.powerTimeChange = PowerTimeChange()
        base.powerTimeChange.energyUsageSpecific = base.energyUsageSpecific
        base.powerTimeChange.energyUsageBusiness = base.energyUsageBusiness
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
             * */
            energyPowerChange = preRunHours * (prePower - postPower)
            energyTimeChange = (preRunHours - postRunHours) * prePower
            energyPowerTimeChange = (preRunHours - postRunHours) * (prePower - postPower)

            /**
             * Validate Power Time Change - Set the appropriate Flag
             * */
            checkPowerChange = energyPowerChange != 0.0 && energyTimeChange == 0.0
            checkTimeChange = energyPowerChange == 0.0 && energyTimeChange != 0.0
            checkPowerTimeChange = energyPowerChange != 0.0 && energyTimeChange != 0.0

            Log.d(TAG, "Energy Power Change : ($energyPowerChange)")
            Log.d(TAG, "Energy Time Change : ($energyTimeChange)")
            Log.d(TAG, "Energy Power Time Change : ($energyPowerTimeChange)")

            Log.d(TAG, "Check Power Change : ($checkPowerChange)")
            Log.d(TAG, "Check Time Change : ($checkTimeChange)")
            Log.d(TAG, "Check Power Time Change : ($checkPowerTimeChange)")

            return this

        }

        fun energySaving() = when {
            checkPowerChange -> energyPowerChange
            checkTimeChange -> energyTimeChange
            checkPowerTimeChange -> energyPowerTimeChange
            else -> 0.0
        }

    }


    /**
     * Energy Cost Saving - Calculates the Energy Saved via examining the 3 cases
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

                /**
                 * Pre Usage Hours - Mapped Peak Hours (Specific)
                 * */
                val preUsageByPeak = energyUsageSpecific.mappedPeakHourYearly()
                val preHoursOnPeakPricing = preUsageByPeak[ERateKey.SummerOn]!! * .504
                val preHoursOnPartPeakPricing = preUsageByPeak[ERateKey.SummerPart]!! * .504 + preUsageByPeak[ERateKey.WinterPart]!! * .496
                val preHoursOnOffPeakPricing = preUsageByPeak[ERateKey.SummerOff]!! * .504 + preUsageByPeak[ERateKey.WinterOff]!! * .496

                /**
                 * Post Usage Hours - Mapped Peak Hours (Business)
                 * */
                val postUsageByPeak = energyUsageBusiness.mappedPeakHourYearly()
                val postHoursOnPeakPricing = postUsageByPeak[ERateKey.SummerOn]!! * .504
                val postHoursOnPartPeakPricing = postUsageByPeak[ERateKey.SummerPart]!! * .504 + postUsageByPeak[ERateKey.WinterPart]!! * .496
                val postHoursOnOffPeakPricing = postUsageByPeak[ERateKey.SummerOff]!! * .504 + postUsageByPeak[ERateKey.WinterOff]!! * .496

                /**
                 * Utility Rate - Electricity
                 * */
                val peakPrice = electricityUtility.structure[ERateKey.SummerOn.value]!![0].toDouble()
                val partPeakPrice =
                        (electricityUtility.structure[ERateKey.SummerPart.value]!![0].toDouble()
                                + electricityUtility.structure[ERateKey.WinterPart.value]!![0].toDouble()) / 2
                val offPeakPrice =
                        (electricityUtility.structure[ERateKey.SummerOff.value]!![0].toDouble()
                                + electricityUtility.structure[ERateKey.WinterOff.value]!![0].toDouble()) / 2

                /**
                 * Utility Rate - Gas // Need to wire this up !!
                 * */
                val winterRate = 0.0
                val summerRate = 0.0

                /**
                 * Parse API Energy Efficient Database - materialCost
                 * Parse API Labor Cost - laborCost
                 * */
                val materialCost = 0.0 // The Post State - Energy Efficient Database does'nt have this - Need to add a column
                val laborCost = 0.0 // Already have the API in place

                /**
                 * ToDo - Where do we get these values from ??
                 * */
                val maintenanceCostSavings = 0.0
                val otherEquipmentSavings = 0.0

                /**
                 * Parse API Energy Efficient Database - Rebate
                 * */
                val incentives = 0.0

                /**
                 * Fetch these from the Utility Rate Structure
                 * */
                val blendedEnergyRate = 0.0
                val blendedDemandRate = 0.0

                /**
                 * Utility Rate Structure
                 * */
                val getRateSchedule = electricRateStructure

                /**
                 * Flag to denote a gas based equipment
                 * */
                val checkForGas = false

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

                /**
                 * Power, Time Change or Both
                 * */
                val ptc = powerTimeChange.delta(computable)
                val powerChangeCheck = ptc.checkPowerChange
                val timeChangeCheck = ptc.checkTimeChange
                val powerTimeChangeCheck = ptc.checkPowerTimeChange

                /**
                 * Applicable to Post State having multiple Energy Column
                 * It's false for most of the devices except - Oven (Need to verify which other devices' are applicable)
                 * */
                val multiplePowerCheck = false
                val multipleTimeCheck = false
                val multiplePowerTimeCheck = false

                /**
                 * Power Value
                 * Case 1 : Multiple Power | Time Check -- @powerValues
                 * Case 2 : Single Power | Time -- @powerValue
                 * */
                val powerValues = listOf(0.0, 0.0)
                val powerValue = 0.0


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

        return Observable.just(initDataHolder())
                .map(Mapper())
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
                        val value = element.asJsonObject.get(key)
                        postRow[key] = value.asString
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

    abstract fun preAuditFields(): MutableList<String>
    abstract fun featureDataFields(): MutableList<String>
    abstract fun preStateFields(): MutableList<String>
    abstract fun postStateFields(): MutableList<String>
    abstract fun computedFields(): MutableList<String>

    abstract fun cost(vararg params: Any): Double

    private fun queryEnergyStar() = JSONObject()
            .put("data.company_name", featureData["Company"])
            .put("data.model_number", featureData["Model Number"])
            .toString()

    private fun queryLaborCost() = JSONObject()
            .put("data.zipcode", preAudit["ZipCode"])
            .put("data.profession", preAudit["Profession"])
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
        val usageByYear = usage.yearly() //ToDo - Figure out a way to adjust the Usage Hours by negating the Vacation Days

        if (electricRateStructure.matches(regex)) {

            var summer = usageByPeak[ERateKey.SummerOn]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOn.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerPart]!! * .504 * powerUsed * utility.structure[ERateKey.SummerPart.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerOff]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOff.value]!![0].toDouble()

            var winter = usageByPeak[ERateKey.WinterPart]!! * .496 * powerUsed * utility.structure[ERateKey.WinterPart.value]!![0].toDouble()
            winter += usageByPeak[ERateKey.WinterOff]!! * .496 * powerUsed * utility.structure[ERateKey.WinterOff.value]!![0].toDouble()

            return (summer + winter)

        } else {

            val summer = usageByYear * .504 * powerUsed * utility.structure[ERateKey.SummerNone.value]!![0].toDouble()
            val winter = usageByYear * .496 * powerUsed * utility.structure[ERateKey.WinterNone.value]!![0].toDouble()

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