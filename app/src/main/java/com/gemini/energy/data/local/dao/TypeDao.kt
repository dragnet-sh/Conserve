package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.TypeLocalModel
import io.reactivex.Maybe

@Dao
interface TypeDao {

    @Query("SELECT * FROM AuditZoneType WHERE zone_id = :id AND type = :type")
    fun getAllTypeByZone(id: Int, type: String): Maybe<List<TypeLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(auditScope: TypeLocalModel)

}