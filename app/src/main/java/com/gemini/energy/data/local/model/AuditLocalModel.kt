package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "Audit")
data class AuditLocalModel(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var auditId: Long,
        var name: String,
        var usn: Int,

        @ColumnInfo(name = "created_at")
        var createdAt: Date,

        @ColumnInfo(name = "updated_at")
        var updatedAt: Date
)