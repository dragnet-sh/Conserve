package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.PreAuditLocalModel
import io.reactivex.Maybe

@Dao
interface PreAuditDao {

    @Query("SELECT * FROM PreAudit WHERE audit_id = :id")
    fun getAllByAudit(id: Int): Maybe<List<PreAuditLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(preAudit: List<PreAuditLocalModel>)

}