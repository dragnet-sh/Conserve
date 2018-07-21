package com.gemini.energy.domain.entity

import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.service.IComputable

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
        var energyEquivalent: List<IComputable>?) {

    constructor(): this(
            NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            null, null, null, false, null)

    constructor(auditScopeSubType: SubType) : this(NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            auditScopeSubType, null, null, false, null)

    fun mappedFeatureAuditScope() = featureAuditScope?.associateBy { it.key }
    fun mappedFeaturePreAudit() = featurePreAudit?.associateBy { it.key }

    companion object {
        private const val EMPTY = ""
        private const val NONE = -1
    }

}
