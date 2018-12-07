package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.domain.entity.Audit

class AuditMapper {

    fun toLocal(audit: Audit): AuditLocalModel {
        return AuditLocalModel(audit.id, audit.name, audit.usn, audit.objectId, audit.createdAt, audit.updatedAt)
    }

}