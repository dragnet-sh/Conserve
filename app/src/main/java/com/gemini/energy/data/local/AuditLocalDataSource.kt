package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.model.AuditLocalModel
import io.reactivex.Observable

class AuditLocalDataSource(private val auditDao: AuditDao) {

    fun getAll(): Observable<List<AuditLocalModel>> = auditDao.getAll().toObservable()

    fun save(audit: AuditLocalModel): Observable<Unit> = Observable.fromCallable {
        auditDao.insert(audit)
    }

}