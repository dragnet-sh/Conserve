package com.gemini.energy.domain.entity

import java.util.*

data class Feature(
        val id: Int,
        val formId: String,
        val type: String,

        val valueDouble: Double,
        val valueInt: Int,
        val valueString: String,

        val auditId: Int,

        val createdAt: Date,
        val updatedAt: Date
)
