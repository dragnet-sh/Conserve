package com.gemini.energy.data.local.dao

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.gemini.energy.data.local.model.AuditLocalModel
import io.reactivex.Maybe

@Dao
interface AuditDao {

    @Query("SELECT * FROM Audit")
    fun getAll(): Maybe<List<AuditLocalModel>>

    @Query("SELECT * FROM Audit WHERE id = :id")
    fun get(id: Long): Maybe<AuditLocalModel>

    @Query("SELECT * FROM Audit WHERE usn = :usn")
    fun getAllWithUsn(usn: Int): Maybe<List<AuditLocalModel>>

    @Insert(onConflict = REPLACE)
    fun insert(audit: AuditLocalModel)

    @Update
    fun update(audit: AuditLocalModel)

    @Query("DELETE FROM Audit WHERE id = :id")
    fun delete(id: Long)

}