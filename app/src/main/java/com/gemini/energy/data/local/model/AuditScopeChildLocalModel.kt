package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "AuditScopeChild", foreignKeys = [(ForeignKey(
        entity = AuditScopeParentLocalModel::class,
        parentColumns = ["id"],
        childColumns = ["parent_id"]))])

data class AuditScopeChildLocalModel(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var auditChildId: Int?,
        var name: String?,
        var type: String?,

        @ColumnInfo(name="parent_id")
        var auditParentId: Int?,

        @ColumnInfo(name="zone_id")
        var zoneId: Int?,

        @ColumnInfo(name = "audit_id")
        var auditId: Int?,

        @ColumnInfo(name = "created_at")
        var createdAt: Date?,
        @ColumnInfo(name = "updated_at")
        var updatedAt: Date?
)