package com.gemini.energy.data.gateway.mapper

import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.model.PreAuditLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.entity.PreAudit
import com.gemini.energy.domain.entity.Zone

class SystemMapper {

    fun toEntity(type: AuditLocalModel) = Audit(
            type.auditId,
            type.name,
            type.createdAt,
            type.updatedAt
    )

    fun toEntity(zone: ZoneLocalModel) = Zone(
            zone.zoneId,
            zone.name,
            zone.type,
            zone.auditId,
            zone.createdAt,
            zone.updatedAt
    )

    fun toEntity(preAudit: PreAuditLocalModel) = PreAudit (
            preAudit.id,
            preAudit.formId,
            preAudit.type,
            preAudit.valueDouble,
            preAudit.valueInt,
            preAudit.valueString,
            preAudit.auditId,
            preAudit.createdAt,
            preAudit.updatedAt
    )

}