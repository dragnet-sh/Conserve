package com.gemini.energy.presentation.type.list.mapper

import android.content.Context
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.presentation.type.list.model.TypeModel

class TypeMapper(private val context: Context) {

    fun toModel(type: List<Type>): List<TypeModel> {
        return type.map { TypeModel(it.id, it.name, it.type, it.subType, it.zoneId, it.auditId) }
    }

}
