package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.AuditZoneTypeLocalModel
import io.reactivex.Maybe

@Dao
interface AuditZoneTypeDao {

    @Query("SELECT * FROM AuditZoneType WHERE zone_id = :id AND type = :type")
    fun getAllTypeByZone(id: Int, type: String): Maybe<List<AuditZoneTypeLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(auditScope: AuditZoneTypeLocalModel)

}