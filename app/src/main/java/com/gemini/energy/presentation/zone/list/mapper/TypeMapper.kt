package com.gemini.energy.presentation.zone.list.mapper

import android.content.Context
import com.gemini.energy.domain.entity.AuditScopeParent
import com.gemini.energy.presentation.zone.list.model.TypeModel

class TypeMapper(private val context: Context) {

    fun toModel(type: List<AuditScopeParent>): List<TypeModel> {
        return type.map { TypeModel(it.id, it.name, it.type, it.zoneId, it.auditId) }
    }

}
