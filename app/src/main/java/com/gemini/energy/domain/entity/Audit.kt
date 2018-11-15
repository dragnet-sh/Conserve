package com.gemini.energy.domain.entity
import java.util.*

data class Audit(
        val id: Int,
        var name: String,
        var createdAt: Date,
        var updatedAt: Date
)
