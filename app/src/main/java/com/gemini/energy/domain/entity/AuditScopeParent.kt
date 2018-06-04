package com.gemini.energy.domain.entity

import java.util.*

data class AuditScopeParent(
    val id: Int?,
    val name: String?,
    val type: String?,

    val zoneId: Int?,
    val auditId: Int?,

    val createdAt: Date?,
    val updatedAt: Date?
)
