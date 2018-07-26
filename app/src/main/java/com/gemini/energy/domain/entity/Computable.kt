package com.gemini.energy.domain.entity

import android.util.Log
import com.gemini.energy.presentation.util.BaseRowType
import com.gemini.energy.presentation.util.EZoneType
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
        var efficientAlternative: List<JsonElement>?) {

    constructor(): this(
            NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            null, null, null, false, null)

    constructor(auditScopeSubType: SubType) : this(NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            auditScopeSubType, null, null, false, null)

    fun mappedFeatureAuditScope(): HashMap<String, Any> = featureMapper(featureAuditScope)
    fun mappedFeaturePreAudit(): HashMap<String, Any> = featureMapper(featurePreAudit)

    private fun featureMapper(features: List<Feature>?): HashMap<String, Any> {
        val outgoing = hashMapOf<String, Any>()
        features?.let { featureList ->
            featureList
                    .filter { discard(it.valueString.isNullOrEmpty()) }
                    .forEach { feature ->
                        val key = feature.key!!
                        val value = typeMapper(feature.valueString, feature.dataType)
                        outgoing[key] = value
                    }
        }

        Log.d(this.javaClass.simpleName, outgoing.toString())
        return outgoing
    }

    private fun typeMapper(value: String?, type: String?) = when (type) {
        BaseRowType.IntRow.value        -> value.toString().toInt()
        BaseRowType.DecimalRow.value    -> value.toString().toDouble()
        else                            -> value.toString()
    }

    companion object {
        private const val EMPTY = ""
        private const val NONE = -1
        private fun discard(boolean: Boolean) = !boolean
    }

    override fun toString(): String {
        return  "Audit [$auditId - $auditName] | Zone [$zoneId - $zoneName]\n" +
                "Scope [$auditScopeId - $auditScopeName] | Type [${auditScopeType?.value} - ${auditScopeSubType?.toString()}]\n" +
                "Feature Pre-Audit Count [${featurePreAudit?.count()}]\n" +
                "Feature Audit Scope Count [${featureAuditScope?.count()}]"
    }

}
