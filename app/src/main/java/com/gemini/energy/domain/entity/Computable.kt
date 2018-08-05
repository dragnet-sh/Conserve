package com.gemini.energy.domain.entity

import android.util.Log
import com.gemini.energy.presentation.util.BaseRowType
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.service.OutgoingRows
import com.google.gson.JsonElement

data class Computable<SubType>(

        /**
         * Audit Section
         * */
        var auditId: Int,
        var auditName: String,

        /**
         * Zone Section
         * */
        var zoneId: Int,
        var zoneName: String,

        /**
         * Categorize Device by Type - Sub Type
         * */
        var auditScopeId: Int,
        var auditScopeName: String,
        var auditScopeType: EZoneType?,
        var auditScopeSubType: SubType?,

        /**
         * These are the Form Data Being Collected from the User
         * */
        var featurePreAudit: List<Feature>?,
        var featureAuditScope: List<Feature>?,

        /**
         * The following parameters are used for Energy Efficient Equivalent
         * */
        var isEnergyStar: Boolean,
        var energyPreState: Map<String, String>?,
        var energyPostState: MutableList<Map<String, String>>?,
        var energyPostStateLeastCost: List<Map<String, String>>,
        var laborCost: Double,

        /**
         * These are the Outgoing Rows to be written to a file
         * */
        var outgoingRows: OutgoingRows?) {

    constructor(): this(
            NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            null, null, null, false,
            null, mutableListOf(), mutableListOf(), 0.0, null)

    constructor(auditScopeSubType: SubType) : this(NONE, EMPTY, NONE, EMPTY, NONE, EMPTY,
            null, auditScopeSubType, null, null,
            false, null, mutableListOf(), mutableListOf(), 0.0, null)


    fun mappedFeatureAuditScope(): HashMap<String, Any> = featureMapper(featureAuditScope)
    fun mappedFeaturePreAudit(): HashMap<String, Any> = featureMapper(featurePreAudit)

    private fun featureMapper(features: List<Feature>?): HashMap<String, Any> {
        val outgoing = hashMapOf<String, Any>()
        features?.let { featureList ->
            featureList
                    .forEach { feature ->
                        val key = feature.key!!
                        val value = typeMapper(feature.valueString, feature.dataType)
                        outgoing[key] = value
                    }
        }

        Log.d(this.javaClass.simpleName, outgoing.toString())
        return outgoing
    }

    private fun typeMapper(value: String?, type: String?) = cleanup(value, type)

    companion object {
        private const val EMPTY = ""
        private const val NONE = -1
        private fun cleanup(value: String?, type: String?) =
                when (type) {
                    BaseRowType.IntRow.value            -> if (!value.isNullOrEmpty()) value.toString().toInt() else 0
                    BaseRowType.DecimalRow.value        -> if (!value.isNullOrEmpty()) value.toString().toDouble() else 0.0
                    else                                -> if (!value.isNullOrEmpty()) value.toString() else EMPTY
                }
    }

    override fun toString(): String {
        return  "audit: [$auditId - $auditName] | zone: [$zoneId - $zoneName]\n" +
                "scope: [$auditScopeId - $auditScopeName] | type: [${auditScopeType?.value} - ${auditScopeSubType?.toString()}]\n" +
                "featurePreAudit: COUNT [${featurePreAudit?.count()}]\n" +
                "featureData: COUNT [${featureAuditScope?.count()}]"
    }

}
