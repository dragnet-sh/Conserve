package com.gemini.energy.domain.entity

import java.util.*

data class Type(
    val id: Int?,
    var name: String?,
    val type: String?,
    var subType: String?,

    val zoneId: Int?,
    val auditId: Long?,

    val createdAt: Date?,
    val updatedAt: Date?
)
