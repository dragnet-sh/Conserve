package com.gemini.energy.data.repository

import com.gemini.energy.data.local.PreAuditLocalDataSource
import com.gemini.energy.data.local.model.PreAuditLocalModel
import com.gemini.energy.data.repository.mapper.PreAuditMapper
import com.gemini.energy.data.remote.PreAuditRemoteDataSource
import com.gemini.energy.domain.entity.PreAudit
import io.reactivex.Observable

class PreAuditRepository(
        private val preAuditLocalDataSource: PreAuditLocalDataSource,
        private val preAuditRemoteDataSource: PreAuditRemoteDataSource,
        private val preAuditMapper: PreAuditMapper) {

    fun getAllByAudit(id: Int): Observable<List<PreAuditLocalModel>> {
        return preAuditLocalDataSource.getAllByAudit(id)
    }

    fun save(preAudit: List<PreAudit>): Observable<Unit> {
        return preAuditLocalDataSource.save(preAuditMapper.toLocal(preAudit))
    }

}