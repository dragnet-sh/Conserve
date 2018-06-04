package com.gemini.energy.domain.entity

import java.util.*

data class AuditScopeChild(
    val id: Int?,
    val name: String?,
    val type: String?,

    val parentId: Int?,
    val zoneId: Int?,
    val auditId: Int?,

    val createdAt: Date?,
    val updatedAt: Date?
)
