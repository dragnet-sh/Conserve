package com.gemini.energy.domain.entity

import java.util.*

data class Feature (
        val id: Int?,
        val formId: Int?,
        val belongsTo: String?,
        val dataType: String?,

        val auditId: Int?,
        val zoneId: Int?,
        val typeId: Int?,

        val key: String?,

        val valueString: String?,
        val valueInt: Int?,
        val valueDouble: Double?,

        val createdAt: Date,
        val updatedAt: Date
)
