package com.gemini.energy.domain.entity
import java.util.*

data class Audit(
        val id: Long,
        var name: String,
        var usn: Int,
        var createdAt: Date,
        var updatedAt: Date
)
