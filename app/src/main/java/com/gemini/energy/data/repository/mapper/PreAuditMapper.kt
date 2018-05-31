package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.PreAuditLocalModel
import com.gemini.energy.domain.entity.PreAudit

class PreAuditMapper {

    fun toLocal(preAudit: List<PreAudit>): List<PreAuditLocalModel> {
        return preAudit.map {
            PreAuditLocalModel(
                    it.id,
                    it.formId,
                    it.type,
                    it.valueDouble,
                    it.valueInt,
                    it.valueString,
                    it.auditId,
                    it.createdAt,
                    it.updatedAt
            )
        }
    }

}