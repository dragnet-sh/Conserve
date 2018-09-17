package com.gemini.energy.data.local.dao

import android.arch.persistence.room.*
import com.gemini.energy.data.local.model.ZoneLocalModel
import io.reactivex.Maybe

@Dao
interface ZoneDao {

    @Query("SELECT * FROM Zone WHERE id = :id")
    fun get(id: Int): Maybe<ZoneLocalModel>

    @Query("SELECT * FROM Zone WHERE audit_id = :id")
    fun getAllByAudit(id: Int): Maybe<List<ZoneLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(zone: ZoneLocalModel)

    @Update
    fun update(zone: ZoneLocalModel)

    @Query("DELETE FROM Zone WHERE id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM Zone WHERE audit_id = :id")
    fun deleteByAuditId(id: Int)

}