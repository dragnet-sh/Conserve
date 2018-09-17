package com.gemini.energy.data.local.dao

import android.arch.persistence.room.*
import com.gemini.energy.data.local.model.TypeLocalModel
import io.reactivex.Maybe
import java.lang.reflect.Type

@Dao
interface TypeDao {

    @Query("SELECT * FROM AuditZoneType WHERE zone_id = :id AND type = :type")
    fun getAllTypeByZone(id: Int, type: String): Maybe<List<TypeLocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(auditScope: TypeLocalModel)

    @Query("DELETE FROM AuditZoneType WHERE zone_id = :id")
    fun deleteByZoneId(id: Int)

    @Query("DELETE FROM AuditZoneType WHERE audit_id = :id")
    fun deleteByAuditId(id: Int)

}