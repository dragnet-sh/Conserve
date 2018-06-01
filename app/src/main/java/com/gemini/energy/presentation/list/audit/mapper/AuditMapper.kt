package com.gemini.energy.presentation.list.audit.mapper

import android.content.Context
import com.gemini.energy.presentation.list.audit.model.AuditModel
import com.gemini.energy.domain.entity.Audit

class AuditMapper(private val context: Context) {

    fun toModel(audit: List<Audit>): List<AuditModel> {
        return audit.map { AuditModel(it.id, it.name) }
    }

}