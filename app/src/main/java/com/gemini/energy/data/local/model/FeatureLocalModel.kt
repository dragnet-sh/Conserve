package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "Feature", foreignKeys = [
    ForeignKey(entity = AuditLocalModel::class, parentColumns = ["id"], childColumns = ["audit_id"]),
    ForeignKey(entity = TypeLocalModel::class, parentColumns = ["id"], childColumns = ["type_id"])
])
data class FeatureLocalModel(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var featureId: Int?,


        @ColumnInfo(name = "form_id")
        var formId: Int?,
        @ColumnInfo(name = "belongs_to")
        var belongsTo: String?,
        @ColumnInfo(name = "data_type")
        var dataType: String?,


        @ColumnInfo(name = "audit_id")
        var auditId: Long?,
        @ColumnInfo(name = "zone_id")
        var zoneId: Int?,
        @ColumnInfo(name = "type_id")
        var typeId: Int?,

        var key: String?,
        @ColumnInfo(name = "value_string")
        var valueString: String?,
        @ColumnInfo(name = "value_int")
        var valueInt: Int?,
        @ColumnInfo(name = "value_double")
        var valueDouble: Double?,


        @ColumnInfo(name = "created_at")
        var createdAt: Date,
        @ColumnInfo(name = "updated_at")
        var updatedAt: Date
)
