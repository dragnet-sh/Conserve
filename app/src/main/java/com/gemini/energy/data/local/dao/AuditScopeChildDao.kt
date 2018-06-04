package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.AuditScopeChildLocalModel
import io.reactivex.Maybe

@Dao
interface AuditScopeChildDao {

    @Query("SELECT * FROM AuditScopeChild WHERE parent_id = :id")
    fun getAllByParent(id: Int): Maybe<List<AuditScopeChildLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(auditScope: AuditScopeChildLocalModel)

}