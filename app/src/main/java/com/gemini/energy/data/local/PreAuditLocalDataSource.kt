package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.PreAuditDao
import com.gemini.energy.data.local.model.PreAuditLocalModel
import io.reactivex.Observable


class PreAuditLocalDataSource(private val preAuditDao: PreAuditDao) {

    fun getAllByAudit(id: Int): Observable<List<PreAuditLocalModel>> = preAuditDao.getAllByAudit(id).toObservable()

    fun save(preAudit: List<PreAuditLocalModel>): Observable<Unit> = Observable.fromCallable {
        preAuditDao.insertAll(preAudit)
    }

}