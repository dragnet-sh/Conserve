package com.gemini.energy.data.local

import android.util.Log
import com.gemini.energy.data.local.dao.AuditScopeChildDao
import com.gemini.energy.data.local.dao.AuditScopeParentDao
import com.gemini.energy.data.local.model.AuditScopeChildLocalModel
import com.gemini.energy.data.local.model.AuditScopeParentLocalModel
import io.reactivex.Observable

class AuditScopeLocalDataSource(
        private val auditScopeParentDao: AuditScopeParentDao,
        private val auditScopeChildDao: AuditScopeChildDao) {

    fun getAllParentByZone(id: Int): Observable<List<AuditScopeParentLocalModel>> = auditScopeParentDao.getAllByZone(id).toObservable()
    fun getAllChildByParent(id: Int): Observable<List<AuditScopeChildLocalModel>> = auditScopeChildDao.getAllByParent(id).toObservable()

    fun save(scopeParent: AuditScopeParentLocalModel): Observable<Unit> {
        return Observable.fromCallable {
            Log.d("Zone Local Data Source", "SAVE - ZONE -- ZONE DAO")
            auditScopeParentDao.insert(scopeParent)
        }
    }

    fun save(scopeChild: AuditScopeChildLocalModel): Observable<Unit> {
        return Observable.fromCallable {
            auditScopeChildDao.insert(scopeChild)
        }
    }
}