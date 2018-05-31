package com.gemini.energy.domain.entity
import java.util.*

data class Audit(
        val id: Int,
        val name: String,
        val createdAt: Date,
        val updatedAt: Date
)
