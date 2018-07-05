package com.gemini.energy.data.local.dao

import android.arch.persistence.room.*
import com.gemini.energy.data.local.model.FeatureLocalModel
import io.reactivex.Maybe

@Dao
interface FeatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feature: List<FeatureLocalModel>)

    @Query("SELECT * FROM feature WHERE audit_id = :id")
    fun getAllByAudit(id: Int): Maybe<List<FeatureLocalModel>>

    @Delete
    fun deleteByType(feature: List<FeatureLocalModel>)

}