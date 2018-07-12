package com.gemini.energy.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.gemini.energy.data.local.model.ComputableLocalModel
import io.reactivex.Maybe

@Dao
interface ComputableDao {

    @Query("SELECT " +

            "audit.id                   AS auditId, "       +
            "audit.name                 AS auditName, "     +

            "zone.id                    AS zoneId, "        +
            "zone.name                  AS zoneName,"       +

            "auditzonetype.id           AS auditScopeId, "        +
            "auditzonetype.name         AS auditScopeName, "      +
            "auditzonetype.type         AS auditScopeType, "      +
            "auditzonetype.subtype      AS auditScopeSubType "    +

            "FROM audit \n" +

            "INNER JOIN zone ON audit.id = zone.audit_id \n" +
            "INNER JOIN auditzonetype ON zone.id = auditzonetype.zone_id;")
    fun getAllComputable(): Maybe<List<ComputableLocalModel>>

}