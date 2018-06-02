package com.gemini.energy.presentation.audit.detail.zone.list.mapper

import android.content.Context
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel

class ZoneMapper(private val context: Context) {

    fun toModel(zone: List<Zone>): List<ZoneModel> {
        return zone.map { ZoneModel(it.id, it.name) }
    }

}