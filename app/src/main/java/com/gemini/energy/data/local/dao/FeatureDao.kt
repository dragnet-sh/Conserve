package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.FeatureLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import io.reactivex.Maybe

@Dao
interface FeatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feature: List<FeatureLocalModel>)

    @Query("SELECT * FROM feature WHERE audit_id = :id")
    fun getAllByAudit(id: Int): Maybe<List<FeatureLocalModel>>

}