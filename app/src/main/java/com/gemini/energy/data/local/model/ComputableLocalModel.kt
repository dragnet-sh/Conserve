package com.gemini.energy.data.local.model

data class ComputableLocalModel(
        var auditId: Int,
        var auditName: String,
        var zoneId: Int,
        var zoneName: String,
        var auditScopeId: Int,
        var auditScopeName: String,
        var auditScopeType: String,
        var auditScopeSubType: String
)
