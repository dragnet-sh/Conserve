package com.gemini.energy.domain.entity
import java.util.*

data class Audit(
        val id: Int,
        var name: String,
        val createdAt: Date,
        val updatedAt: Date
)
