package com.gemini.energy.service.device

import android.content.Context
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.form.FormMapper
import com.gemini.energy.service.DataHolder
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.type.UsageHVAC
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class Hvac(private val computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
           usageHours: UsageHours, outgoingRows: OutgoingRows, private val context: Context) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    companion object {

        /**
         * Conversion Factor from Watts to Kilo Watts
         * */
        private const val KW_CONVERSION = 0.001

        private const val HVAC_EER = "hvac_eer"
        private const val HVAC_COOLING_HOURS = "cooling_hours"
        private const val HVAC_EFFICIENCY = "hvac_efficiency"

        /**
         * Fetches the EER based on the specific Match Criteria via the Parse API
         * */
        fun extractEER(elements: List<JsonElement?>): Double {
            elements.forEach {
                it?.let {
                    if (it.asJsonObject.has("eer")) {
                        return it.asJsonObject.get("eer").asDouble
                    }
                }
            }
            return 0.0
        }

        /**
         * Fetches the Hours based on the City
         * @Anthony - Verify where we are using the Extracted Hours ??
         * This is used to calculate the Energy
         * */
        fun extractHours(elements: List<JsonElement?>): Int {
            elements.forEach {
                it?.let {
                    if (it.asJsonObject.has("hours")) {
                        return it.asJsonObject.get("hours").asInt
                    }
                }
            }
            return 0
        }

        /**
         * HVAC - Power Consumed
         * There could be a case where the User will input the value in KW - If that happens we need to convert the KW
         * int BTU / hr :: 1KW equals 3412.142
         * */
        fun power(btu: Int, eer: Double) = (btu / eer) * KW_CONVERSION

        /**
         * Year At - Current minus the Age
         * */
        private val dateFormatter = SimpleDateFormat("yyyy", Locale.ENGLISH)
        fun getYear(age: Int): Int {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, "-$age".toInt()) //** Subtracting the Age **
            return dateFormatter.format(calendar.time).toInt()
        }
    }

    /**
     * HVAC - Energy Efficiency Ratio
     * If not available - Build a match criteria at queryHVACEer()
     * 1. Primary Match - [year equals (Current Year minus 20)]
     * 2. Secondary Match - [size_btu_per_hr_min > BTU < size_btu_per_hr_max]
     * */
    private var eer = 0.0
    private var seer = 0.0
    private var alternateSeer = 0.0

    /**
     * HVAC - Age
     * */
    private var age = 0

    /**
     * HVAC - British Thermal Unit
     * */
    private var btu = 0

    /**
     * City | State
     * */
    private var city: String = ""
    private var state: String = ""

    override fun setup() {
        try {
            eer = preAudit["EER"]!! as Double
            seer = preAudit["SEER"]!! as Double
            age = preAudit["Age"]!! as Int
            btu = preAudit["Cooling Capacity (Btu/hr)"]!! as Int

            city = preAudit["City"]!! as String
            state = preAudit["State"]!! as String

            alternateSeer = preAudit["Alternate SEER"]!! as Double

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
    override fun costPreState(elements: List<JsonElement?>): Double {

        if (eer == 0.0) { eer = extractEER(elements) }

        Timber.d("::: PARAM - HVAC :::")
        Timber.d("EER -- $eer")
        Timber.d("AGE -- $age")
        Timber.d("BTU -- $btu")
        Timber.d("YEAR -- ${getYear(age)}")

        Timber.d("::: DATA EXTRACTOR - HVAC :::")
        Timber.d(elements.toString())

        val usageHours = if (alternateSeer == 0.0) {
            UsageHVAC(usageHoursBusiness, isTOU(), extractHours(elements))
        } else { usageHoursBusiness }

        computable.udf1 = usageHours
        Timber.d(usageHours.toString())

        val powerUsedCurrent = power(btu, seer)
        val powerUsedStandard = power(btu, eer)
        val powerUsedReplaced = power(btu, alternateSeer)

        val powerUsed = if (alternateSeer == 0.0) powerUsedStandard else powerUsedCurrent
        Timber.d("HVAC :: Power Used (Current) -- [$powerUsedCurrent]")
        Timber.d("HVAC :: Power Used (Standard) -- [$powerUsedStandard]")
        Timber.d("HVAC :: Power Used (Replaced) -- [$powerUsedReplaced]")

        Timber.d("HVAC :: Pre Power Used -- [$powerUsed]")

        return costElectricity(powerUsed, usageHours, electricityRate)
    }

    /**
     * Cost - Post State
     * */
    override fun costPostState(element: JsonElement, dataHolder: DataHolder): Double {

        Timber.d("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")
        Timber.d("!!! COST POST STATE - HVAC !!!")
        Timber.d("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")

        var postSize = 0
        var postEER = 0.0

        try {
            postSize = element.asJsonObject.get("size_btu_per_hour").asInt
            postEER = element.asJsonObject.get("eer").asDouble
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val postPowerUsed = power(postSize, postEER)
        val postUsageHours = computable.udf1 as UsageHVAC

        return costElectricity(postPowerUsed, postUsageHours, electricityRate)
    }

    /**
     * PowerTimeChange >> Hourly Energy Use - Pre
     * */
    override fun hourlyEnergyUsagePre(): List<Double> = listOf(0.0, 0.0)

    /**
     * PowerTimeChange >> Hourly Energy Use - Post
     * */
    override fun hourlyEnergyUsagePost(element: JsonElement): List<Double> = listOf(0.0, 0.0)

    /**
     * PowerTimeChange >> Yearly Usage Hours - [Pre | Post]
     * */
    override fun usageHoursPre(): Double = 0.0
    override fun usageHoursPost(): Double = 0.0

    /**
     * PowerTimeChange >> Energy Efficiency Calculations
     * */
    override fun energyPowerChange(): Double {
        var eerPS = 0.0

        val element = computable.efficientAlternative
        element?.let {
            try {
                eerPS = it.asJsonObject.get("eer").asDouble
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (eerPS == 0.0) { eerPS = alternateSeer }

        val powerPre = power(btu, eer)
        val powerPost = power(btu, alternateSeer)
        val delta = (powerPre - powerPost) * usageHoursBusiness.yearly()

        Timber.d("HVAC :: Delta -- $delta")
        return delta
    }

    override fun energyTimeChange(): Double = 0.0
    override fun energyPowerTimeChange(): Double = 0.0

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = ""

    override fun queryHVACAlternative() = JSONObject()
            .put("type", HVAC_EFFICIENCY)
            .put("data.size_btu_hr", btu)
            .toString()

    override fun queryHVACEer() = JSONObject()
            .put("type", HVAC_EER)
            .put("data.year", getYear(age))
            .put("data.size_btu_per_hr_min", JSONObject().put("\$lte", btu))
            .put("data.size_btu_per_hr_max", JSONObject().put("\$gte", btu))
            .toString()

    override fun queryHVACCoolingHours() = JSONObject()
            .put("type", HVAC_COOLING_HOURS)
            .put("data.city", city)
            .put("data.state", state)
            .toString()

    /**
     * State if the Equipment has a Post UsageHours Hours (Specific) ie. A separate set of
     * Weekly UsageHours Hours apart from the PreAudit
     * */
    override fun usageHoursSpecific() = false

    /**
     * Define all the fields here - These would be used to Generate the Outgoing Rows or perform the Energy Calculation
     * */
    override fun preAuditFields() = mutableListOf("")
    override fun featureDataFields() = getGFormElements().map { it.value.param!! }.toMutableList()

    override fun preStateFields() = mutableListOf("")
    override fun postStateFields() = mutableListOf("__life_hours", "__maintenance_savings",
            "__cooling_savings", "__energy_savings", "__energy_at_post_state")

    override fun computedFields() = mutableListOf("")

    private fun getFormMapper() = FormMapper(context, R.raw.hvac)
    private fun getModel() = getFormMapper().decodeJSON()
    private fun getGFormElements() = getFormMapper().mapIdToElements(getModel())


}
