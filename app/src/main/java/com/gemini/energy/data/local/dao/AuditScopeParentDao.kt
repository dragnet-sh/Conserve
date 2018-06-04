package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.AuditScopeParentLocalModel
import io.reactivex.Maybe

@Dao
interface AuditScopeParentDao {

    @Query("SELECT * FROM AuditScopeParent WHERE zone_id = :id")
    fun getAllByZone(id: Int): Maybe<List<AuditScopeParentLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(auditScope: AuditScopeParentLocalModel)

}