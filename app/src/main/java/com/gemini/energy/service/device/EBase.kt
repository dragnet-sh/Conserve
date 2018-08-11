package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.internal.AppSchedulers
import com.gemini.energy.presentation.util.EDay
import com.gemini.energy.service.*
import com.gemini.energy.service.crunch.*
import com.gemini.energy.service.type.Electricity
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.gemini.energy.service.type.Gas
import com.google.gson.JsonArray
import io.reactivex.Observable
import io.reactivex.functions.Function
import org.json.JSONObject

abstract class EBase(private val computable: Computable<*>,
                     private val utilityRateGas: UtilityRate,
                     private val utilityRateElectricity: UtilityRate,
                     val operatingHours: UsageHours,
                     val outgoingRows: OutgoingRows) {

    lateinit var schedulers: Schedulers
    private lateinit var gasUtilityRate: UtilityRate
    lateinit var electricityUtilityRate: UtilityRate
    lateinit var preconditions: Preconditions

    private lateinit var powerTimeChange: PowerTimeChange

    var preAudit: Map<String, Any> = mapOf()
    var featureData: Map<String, Any> = mapOf()
    private var electricRateStructure: String = RATE

    val energyUsageBusiness = UsageHours()
    private val energyUsageSpecific = UsageHours()

    private fun initialize() {
        val base = this

        Log.d(TAG, "<< COMPUTE :: ${identifier()} >> [Start] - (${thread()})")
        Log.d(TAG, computable.toString())

        base.schedulers = AppSchedulers()
        base.featureData = computable.mappedFeatureAuditScope()
        base.preAudit = computable.mappedFeaturePreAudit()

        base.gasUtilityRate = utilityRateGas.initUtility(Gas()).build()
        base.electricRateStructure = preAudit["Electric Rate Structure"] as String

        Log.d(TAG, "%%%%%%% RATE STRUCTURE CHECKER %%%%%%%")
        Log.d(TAG, electricRateStructure)

        base.electricityUtilityRate = utilityRateElectricity.initUtility(
                Electricity(electricRateStructure)).build()

        Log.d(TAG, "%%%%%%% OBJECT CHECKER %%%%%%%")
        Log.d(TAG, gasUtilityRate.toString())
        Log.d(TAG, electricityUtilityRate.toString())

        base.energyUsageBusiness.initUsage(mappedBusinessHours()).build()
        base.energyUsageSpecific.initUsage(mappedSpecificHours()).build()

        base.outgoingRows.computable = computable
        base.outgoingRows.dataHolder = mutableListOf()
        base.preconditions = Preconditions()

        base.powerTimeChange = PowerTimeChange()
        base.powerTimeChange.usageHoursSpecific = base.energyUsageSpecific

        /**
         * If the Post UsageHours Hours is Empty (Specific) - Post UsageHours Equals to Pre UsageHours Hours (Business)
         * */
        base.powerTimeChange.usageHoursBusiness = if (usageHoursSpecific()) base.energyUsageBusiness
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
     * Pre State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPreState(extra: (param: String) -> Unit): Observable<DataHolder> {
        val energyPreState = EnergyPreState()
        energyPreState.featureData = featureData
        energyPreState.featureDataFields = featureDataFields()
        energyPreState.computable = computable

        return energyPreState.getObservable {
            cost(it)
        }
    }

    /**
     * Post State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPostState(extra: (param: String) -> Unit): Observable<DataHolder> {
        val mapper = EnergyPostState.Mapper()
        mapper.computable = computable
        mapper.postStateFields = postStateFields()
        mapper.cost = {
            cost(it)
        }

        return starValidator(queryEnergyStar())
                .flatMap {
                    if (it && efficientLookup()) {
                        efficientAlternative(queryEfficientFilter()).map(mapper)
                    } else {
                        Observable.just(DataHolder())
                    }
                }
    }

    /**
     * Energy Saving - Calculates the Energy Saved via examining the 3 cases
     * Power Change | Time Change | Both Power Time Change
     * Via PowerTimeChange (helper class)
     * */
    private fun calculateEnergySavings(extra: (param: String) -> Unit): Observable<DataHolder> {
        val mapper = EnergySavings.Mapper()
        mapper.computable = computable
        mapper.powerTimeChange = powerTimeChange

        return Observable.just(Unit).map(mapper)
    }

    /**
     * Energy Cost Saving
     * */
    private fun calculateCostSavings(extra: (param: String) -> Unit): Observable<DataHolder> {

        val mapper = CostSavings.Mapper()
        mapper.computable = computable
        mapper.usageHoursSpecific = energyUsageSpecific
        mapper.usageHoursBusiness = energyUsageBusiness
        mapper.electricRateStructure = electricRateStructure
        mapper.electricityUtilityRate = electricityUtilityRate
        mapper.gasUtilityRate = gasUtilityRate
        mapper.featureData = featureData
        mapper.powerTimeChange = powerTimeChange

        fun prerequisite() = laborCost(queryLaborCost())
                .flatMap { response ->
                    val jsonElements = response.map { it.asJsonObject.get("data") }
                    if (jsonElements.count() > 0) {
                        val cost = jsonElements[0].asJsonObject.get("cost").asDouble
                        computable.laborCost = cost
                    }
                    Observable.just(Unit)
                }

        return prerequisite().map(mapper)
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

    /**
     * Energy Cost Queries
     * */
    private fun queryEnergyStar() = JSONObject()
            .put("data.company", featureData["Company"])
            .put("data.model_number", featureData["Model Number"])
            .toString()

    private fun queryLaborCost() = JSONObject()
            .put("data.zipcode", featureData["ZipCode"] ?: 0)
            .put("data.profession", featureData["Profession"] ?: "none")
            .toString()

    /**
     * Get the Specific Query Result from the Parse API
     * */
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
     * UsageHours Hours
     * 1. Pre - Business Hours (Found at PreAudit)
     * 2. Post - Specific Hours (Applicable for the individual Appliances)
     * */
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

    /**
     * Computes the Electric Cost
     * */
    fun costElectricity(powerUsed: Double, usageHours: UsageHours, utilityRate: UtilityRate): Double {
        val costElectric = CostElectric(usageHours, utilityRate)
        costElectric.structure = electricRateStructure
        costElectric.power = powerUsed

        return costElectric.cost()
    }

    /**
     * Parse API Service Call
     * */
    private val parseAPIService by lazy { ParseAPI.create() }

}
