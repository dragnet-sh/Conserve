package com.gemini.energy.domain.entity

import java.util.*

data class Zone(
        val id: Int,
        val name: String,
        val type: String,

        val auditId: Int,

        val createdAt: Date,
        val updatedAt: Date
)
