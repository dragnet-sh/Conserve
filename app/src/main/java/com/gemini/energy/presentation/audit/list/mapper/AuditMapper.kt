package com.gemini.energy.presentation.audit.list.mapper

import android.content.Context
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.presentation.audit.list.model.AuditModel

class AuditMapper(private val context: Context) {

    fun toModel(audit: List<Audit>): List<AuditModel> {
        return audit.map { AuditModel(it.id, it.name) }
    }

}