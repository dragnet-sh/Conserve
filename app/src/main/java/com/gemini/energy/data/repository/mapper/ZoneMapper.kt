package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.domain.entity.Zone

class ZoneMapper {

    fun toLocal(zone: Zone): ZoneLocalModel {
        return ZoneLocalModel(
                zone.id,
                zone.name,
                zone.type,
                zone.auditId,
                zone.createdAt,
                zone.updatedAt
        )
    }

}