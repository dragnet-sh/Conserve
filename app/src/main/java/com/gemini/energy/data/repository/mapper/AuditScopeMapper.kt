package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.AuditZoneTypeLocalModel
import com.gemini.energy.domain.entity.Type

class AuditScopeMapper {

    fun toLocal(auditScope: Type): AuditZoneTypeLocalModel {
        return AuditZoneTypeLocalModel(
                auditScope.id,
                auditScope.name,
                auditScope.type,
                auditScope.subType,

                auditScope.zoneId,
                auditScope.auditId,

                auditScope.createdAt,
                auditScope.updatedAt
        )
    }

}