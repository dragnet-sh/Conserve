package com.gemini.energy.service.device.plugload

import android.content.Context
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.form.FormMapper
import com.gemini.energy.service.DataHolder
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UsageSimple
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class DishWasher(private val computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
                      usageHours: UsageHours, outgoingRows: OutgoingRows, private val context: Context) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    /**
     * Usage Hours
     * */
    private var peakHours = 0
    private var partPeakHours = 0
    private var offPeakHours = 0
    private var usageHours: UsageSimple? = null

    private var waterConsumption = 0.0
    private var numberOfRacks = 0
    private var cyclesPerDay = 0
    private var daysUsed = 0
    private var waterTemperature = 0
    private var efficiency = 0.0
    private var idleEnergyRate = 0.0
    private var waterHeater = ""

    override fun setup() {

        peakHours = featureData["Peak Hours"]!! as Int
        partPeakHours = featureData["Part Peak Hours"]!! as Int
        offPeakHours = featureData["Off Peak Hours"]!! as Int
        usageHours = UsageSimple(peakHours, partPeakHours, offPeakHours)

        waterConsumption = featureData["Water Consumption"]!! as Double
        numberOfRacks = featureData["Number of Racks"]!! as Int
        cyclesPerDay = featureData["Cycles per Day"]!! as Int
        daysUsed = featureData["Days Used"]!! as Int
        waterTemperature = featureData["Water Temperature (oF)"]!! as Int
        efficiency = featureData["Efficiency"]!! as Double
        idleEnergyRate = featureData["Idle Energy Rate"]!! as Double
        waterHeater = featureData["Water Heater"]!! as String

    }

    /**
     * Cost - Pre State
     * */
    override fun costPreState(elements: List<JsonElement?>): Double {
        val powerUsedElectric = hourlyEnergyUsagePre()[0]
        val thermsUsedGas = hourlyEnergyUsagePre()[1]

        val costElectricity: Double
        costElectricity = costElectricity(powerUsedElectric, usageHours!!, electricityRate)

        val costGas: Double
        costGas = costGas(thermsUsedGas)

        val cost = if (isGas()) costGas else costElectricity
        return cost
    }

    /**
     * Cost - Post State
     * */
    override fun costPostState(element: JsonElement, dataHolder: DataHolder): Double {
        val powerUsedElectric = hourlyEnergyUsagePost(element)[0]
        val thermsUsedGas = hourlyEnergyUsagePost(element)[1]

        val costElectricity: Double
        costElectricity = costElectricity(powerUsedElectric, usageHours!!, electricityRate)

        val costGas: Double
        costGas = costGas(thermsUsedGas)

        val cost = if (isGas()) costGas else costElectricity
        return cost
    }

    /**
     * PowerTimeChange >> Hourly Energy Use - Pre
     * */
    override fun hourlyEnergyUsagePre(): List<Double> {
        var annualEnergyGas = 0.0
        var annualEnergyElectric = 0.0

        try {
            val annualHours = usageHoursPre()
            val alpha = (waterConsumption * numberOfRacks * cyclesPerDay * daysUsed * annualHours * 8.34 * waterTemperature)
            annualEnergyGas = (alpha / (99976.1 * efficiency)) + (idleEnergyRate * 3412.14 * annualHours / 99976.1)
            annualEnergyElectric = (alpha / (efficiency * 3412.14)) + (idleEnergyRate * annualHours)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(annualEnergyElectric, annualEnergyGas)
    }

    /**
     * PowerTimeChange >> Hourly Energy Use - Post
     * */
    override fun hourlyEnergyUsagePost(element: JsonElement): List<Double> {
        var annualEnergyGas = 0.0
        var annualEnergyElectric = 0.0

        try {
            val waterConsumptionPost = element.asJsonObject.get("water_consumption").asDouble
            val numberOfRacksPost = numberOfRacks
            val cyclesPerDayPost = cyclesPerDay
            val daysUsedPost = daysUsed
            val waterTemperaturePost = waterTemperature
            val efficiencyPost = efficiency
            val annualHoursPost = usageHoursPost()
            val idleEnergyRatePost = element.asJsonObject.get("idle_energy_rate").asDouble

            val alpha = (waterConsumptionPost * numberOfRacksPost * cyclesPerDayPost * daysUsedPost *
                    annualHoursPost * 8.34 * waterTemperaturePost)
            annualEnergyGas = (alpha / (99976.1 * efficiencyPost)) + (idleEnergyRatePost * 3412.14 * annualHoursPost / 99976.1)
            annualEnergyElectric = (alpha / (efficiencyPost * 3412.14)) + (idleEnergyRatePost * annualHoursPost)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(annualEnergyElectric, annualEnergyGas)
    }

    /**
     * PowerTimeChange >> Yearly Usage Hours - [Pre | Post]
     * Pre and Post are the same for Refrigerator - 24 hrs
     * */
    //ToDo - @Johnny Verify this
    // Usage Hours Pre is the TOU
    // Usage Hours Post is the Suggested TOU
    override fun usageHoursPre(): Double = usageHours!!.yearly()
    override fun usageHoursPost(): Double = usageHours!!.yearly()

    /**
     * PowerTimeChange >> Energy Efficiency Calculations
     * */
    override fun energyPowerChange(): Double {
        val prePower = hourlyEnergyUsagePre()[0]
        var postPower: Double
        var delta = 0.0

        computable.efficientAlternative?.let {
            postPower = hourlyEnergyUsagePost(it)[0]
            delta = usageHoursPre() * (prePower - postPower)
        }

        return delta
    }

    override fun energyTimeChange(): Double = 0.0
    override fun energyPowerTimeChange(): Double = 0.0

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = true
    override fun queryEfficientFilter() = JSONObject()
            .put("data.idle_energy_rate", idleEnergyRate)
            .put("data.water_consumption", waterConsumption)
            .toString()

    private fun isGas() =  waterHeater == "Gas"
    private fun isElectric() = waterHeater == "Electric"

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
    override fun postStateFields() = mutableListOf("company","model_number","type","sanitation_method",
            "idle_energy_rate","water_consumption","rebate","pgne_measure_code","utility_company","purchase_price_per_unit")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")

    private fun getFormMapper() = FormMapper(context, R.raw.dishwasher)
    private fun getModel() = getFormMapper().decodeJSON()
    private fun getGFormElements() = getFormMapper().mapIdToElements(getModel())

}
