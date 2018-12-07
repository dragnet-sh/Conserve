package com.gemini.energy.data.local.model

data class ComputableLocalModel(
        var auditId: Long,
        var auditName: String,
        var zoneId: Int,
        var zoneName: String,
        var auditScopeId: Int,
        var auditScopeName: String,
        var auditScopeType: String,
        var auditScopeSubType: String
)
