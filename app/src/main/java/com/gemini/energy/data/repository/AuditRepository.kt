package com.gemini.energy.data.repository

import com.gemini.energy.data.local.AuditLocalDataSource
import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.remote.AuditRemoteDataSource
import com.gemini.energy.data.repository.mapper.AuditMapper
import com.gemini.energy.domain.entity.Audit
import io.reactivex.Observable

class AuditRepository(
        private val auditLocalDataSource: AuditLocalDataSource,
        private val auditRemoteDataSource: AuditRemoteDataSource,
        private val auditMapper: AuditMapper) {

    fun get(auditId: Int): Observable<AuditLocalModel> = auditLocalDataSource.get(auditId)
    fun getAll(): Observable<List<AuditLocalModel>> {
        return auditLocalDataSource.getAll()
                .filter { !it.isEmpty() }

    }

    fun save(audit: Audit): Observable<Unit> = auditLocalDataSource.save(auditMapper.toLocal(audit))
    fun update(audit: Audit): Observable<Unit> = auditLocalDataSource.update(auditMapper.toLocal(audit))

    fun delete(auditId: Int): Observable<Unit> = auditLocalDataSource.delete(auditId)

}