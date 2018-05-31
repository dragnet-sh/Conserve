package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "PreAudit", foreignKeys = [ForeignKey(
        entity = AuditLocalModel::class,
        parentColumns = ["id"],
        childColumns = ["audit_id"])])

open class PreAuditLocalModel(
        @PrimaryKey(autoGenerate = true)
        var id: Int,

        @ColumnInfo(name = "form_id")
        var formId: String,
        var type: String,

        @ColumnInfo(name = "value_double")
        var valueDouble: Double,
        @ColumnInfo(name = "value_int")
        var valueInt: Int,
        @ColumnInfo(name = "value_string")
        var valueString: String,

        @ColumnInfo(name = "audit_id")
        var auditId: Int,

        @ColumnInfo(name = "created_at")
        var createdAt: Date,
        @ColumnInfo(name = "updated_at")
        var updatedAt: Date
)
