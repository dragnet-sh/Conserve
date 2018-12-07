package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.TypeLocalModel
import com.gemini.energy.domain.entity.Type

class TypeMapper {

    fun toLocal(auditScope: Type): TypeLocalModel {
        return TypeLocalModel(
                auditScope.id,
                auditScope.name,
                auditScope.type,
                auditScope.subType,
                auditScope.usn,

                auditScope.zoneId,
                auditScope.auditId,

                auditScope.createdAt,
                auditScope.updatedAt
        )
    }

}