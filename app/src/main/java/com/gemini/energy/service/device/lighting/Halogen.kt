package com.gemini.energy.service.device.lighting

import android.content.Context
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.form.FormMapper
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import com.gemini.energy.service.device.EBase
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import com.google.gson.JsonElement
import io.reactivex.Observable
import timber.log.Timber

class Halogen (computable: Computable<*>, utilityRateGas: UtilityRate, utilityRateElectricity: UtilityRate,
           usageHours: UsageHours, outgoingRows: OutgoingRows, private val context: Context) :
        EBase(computable, utilityRateGas, utilityRateElectricity, usageHours, outgoingRows), IComputable {

    /**
     * Entry Point
     * */
    override fun compute(): Observable<Computable<*>> {
        return super.compute(extra = ({ Timber.d(it) }))
    }

    override fun setup() {}

    /**
     * Energy Cost Calculation Formula ToDo: Remove this later
     * */
    override fun cost(vararg params: Any) = 0.0

    /**
     * Cost - Pre State
     * */
    override fun costPreState(): Double = 0.0

    /**
     * Cost - Post State
     * */
    override fun costPostState(element: JsonElement): Double = 0.0

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
     * Pre and Post are the same for Refrigerator - 24 hrs
     * */
    override fun usageHoursPre(): Double = usageHoursBusiness.yearly()
    override fun usageHoursPost(): Double = usageHoursBusiness.yearly()

    /**
     * PowerTimeChange >> Energy Efficiency Calculations
     * */
    override fun energyPowerChange(): Double = 0.0
    override fun energyTimeChange(): Double = 0.0
    override fun energyPowerTimeChange(): Double = 0.0

    /**
     * Energy Efficiency Lookup Query Definition
     * */
    override fun efficientLookup() = false
    override fun queryEfficientFilter() = ""

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
    override fun postStateFields() = mutableListOf("")

    override fun computedFields() = mutableListOf("__daily_operating_hours", "__weekly_operating_hours",
            "__yearly_operating_hours", "__electric_cost")

    private fun getFormMapper() = FormMapper(context, R.raw.halogen)
    private fun getModel() = getFormMapper().decodeJSON()
    private fun getGFormElements() = getFormMapper().mapIdToElements(getModel())

}