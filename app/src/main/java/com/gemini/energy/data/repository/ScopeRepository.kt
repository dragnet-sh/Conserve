package com.gemini.energy.data.repository

import com.gemini.energy.data.local.AuditScopeLocalDataSource
import com.gemini.energy.data.local.model.AuditScopeChildLocalModel
import com.gemini.energy.data.local.model.AuditScopeParentLocalModel
import com.gemini.energy.data.remote.AuditScopeRemoteDataSource
import com.gemini.energy.data.repository.mapper.AuditScopeMapper
import com.gemini.energy.domain.entity.AuditScopeChild
import com.gemini.energy.domain.entity.AuditScopeParent
import io.reactivex.Observable

class ScopeRepository(
        private val auditScopeLocalDataSource: AuditScopeLocalDataSource,
        private val auditScopeRemoteDataSource: AuditScopeRemoteDataSource,
        private val auditScopeMapper: AuditScopeMapper) {

    fun getAllParentByZone(id: Int, type: String): Observable<List<AuditScopeParentLocalModel>> {
        return auditScopeLocalDataSource.getAllParentByZone(id, type)
    }

    fun getAllChildByParent(id: Int): Observable<List<AuditScopeChildLocalModel>> {
        return auditScopeLocalDataSource.getAllChildByParent(id)
    }

    fun saveParent(auditScope: AuditScopeParent): Observable<Unit> {
        return auditScopeLocalDataSource.save(auditScopeMapper.toLocal(auditScope))
    }

    fun saveChild(auditScope: AuditScopeChild): Observable<Unit> {
        return auditScopeLocalDataSource.save(auditScopeMapper.toLocal(auditScope))
    }

}