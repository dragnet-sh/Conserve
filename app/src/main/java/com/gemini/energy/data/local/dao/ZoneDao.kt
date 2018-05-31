package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.ZoneLocalModel
import io.reactivex.Maybe

@Dao
interface ZoneDao {

    @Query("SELECT * FROM Zone WHERE audit_id = :id")
    fun getAllByAudit(id: Int): Maybe<List<ZoneLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(zone: ZoneLocalModel)

}