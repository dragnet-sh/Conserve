package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.AuditLocalModel
import io.reactivex.Maybe

@Dao
interface AuditDao {

    @Query("SELECT * FROM Audit")
    fun getAll(): Maybe<List<AuditLocalModel>>

    @Insert(onConflict = REPLACE)
    fun insert(audit: AuditLocalModel)

}