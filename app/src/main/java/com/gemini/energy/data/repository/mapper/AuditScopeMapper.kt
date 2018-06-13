package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.AuditScopeChildLocalModel
import com.gemini.energy.data.local.model.AuditScopeParentLocalModel
import com.gemini.energy.domain.entity.AuditScopeChild
import com.gemini.energy.domain.entity.AuditScopeParent

class AuditScopeMapper {

    fun toLocal(auditScope: AuditScopeParent): AuditScopeParentLocalModel {
        return AuditScopeParentLocalModel(
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


    fun toLocal(auditScope: AuditScopeChild): AuditScopeChildLocalModel {
        return AuditScopeChildLocalModel(
                auditScope.id,
                auditScope.name,
                auditScope.type,

                auditScope.parentId,
                auditScope.zoneId,
                auditScope.auditId,

                auditScope.createdAt,
                auditScope.updatedAt
        )
    }
}