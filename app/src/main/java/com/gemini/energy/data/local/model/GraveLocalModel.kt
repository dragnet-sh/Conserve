package com.gemini.energy.data.local.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "Graves")
class GraveLocalModel(
        @PrimaryKey
        var usn: Int,
        var oid: Long,
        var type: Int
)
