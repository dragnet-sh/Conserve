package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.GraveLocalModel
import io.reactivex.Maybe

@Dao
interface GravesDao {

    @Insert(onConflict = REPLACE)
    fun insert(grave: GraveLocalModel)

    @Query("SELECT * FROM Graves WHERE type = :type")
    fun get(type: Int): Maybe<GraveLocalModel>

    @Query("DELETE FROM Graves WHERE oid = :oid")
    fun delete(oid: Int)

    @Query("UPDATE Graves SET usn = :usn WHERE oid = :oid")
    fun update(oid: Int, usn: Int)

}