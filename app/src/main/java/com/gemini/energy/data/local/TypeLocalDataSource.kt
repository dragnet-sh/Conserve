package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.TypeDao
import com.gemini.energy.data.local.model.TypeLocalModel
import io.reactivex.Observable

class TypeLocalDataSource(
        private val auditZoneTypeDao: TypeDao) {

    fun getAllTypeByZone(id: Int, type: String): Observable<List<TypeLocalModel>> =
            auditZoneTypeDao.getAllTypeByZone(id, type).toObservable()

    fun getAllTypeByAudit(id: Int): Observable<List<TypeLocalModel>> =
            auditZoneTypeDao.getAllTypeByAudit(id).toObservable()

    fun save(auditZoneType: TypeLocalModel): Observable<Unit> = Observable.fromCallable {
        auditZoneTypeDao.insert(auditZoneType)
    }

    fun delete(id: Int): Observable<Unit> = Observable.fromCallable {
        auditZoneTypeDao.delete(id)
    }

    fun deleteByZoneId(id: Int): Observable<Unit> = Observable.fromCallable {
        auditZoneTypeDao.deleteByZoneId(id)
    }

    fun deleteByAuditId(id: Int): Observable<Unit> = Observable.fromCallable {
        auditZoneTypeDao.deleteByAuditId(id)
    }

}