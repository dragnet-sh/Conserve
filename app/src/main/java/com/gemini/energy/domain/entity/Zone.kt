package com.gemini.energy.domain.entity

import java.util.*

data class Zone(
    val id: Int?,
    var name: String,
    val type: String,
    var usn: Int,

    val auditId: Long,

    val createdAt: Date,
    var updatedAt: Date
)
