package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "AuditZoneType")
data class TypeLocalModel(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var auditParentId: Int?,
        var name: String?,
        var type: String?,
        var subType: String?,
        var usn: Int,

        @ColumnInfo(name="zone_id")
        var zoneId: Int?,

        @ColumnInfo(name = "audit_id")
        var auditId: Long?,

        @ColumnInfo(name = "created_at")
        var createdAt: Date?,
        @ColumnInfo(name = "updated_at")
        var updatedAt: Date?
)