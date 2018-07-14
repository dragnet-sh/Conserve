package com.gemini.energy.domain.entity

import com.gemini.energy.presentation.util.EZoneType

data class Computable<SubType>(
        var auditId: Int,
        var auditName: String,
        var zoneId: Int,
        var zoneName: String,
        var auditScopeId: Int,
        var auditScopeName: String,
        var auditScopeType: EZoneType?,
        var auditScopeSubType: SubType?,
        var featurePreAudit: List<Feature>?,
        var featureAuditScope: List<Feature>?) {

    constructor(): this(
            NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            null, null, null)

    constructor(auditScopeSubType: SubType) : this(NONE, EMPTY, NONE, EMPTY, NONE, EMPTY, null,
            auditScopeSubType, null, null)

    companion object {
        private const val EMPTY = ""
        private const val NONE = -1
    }

}
