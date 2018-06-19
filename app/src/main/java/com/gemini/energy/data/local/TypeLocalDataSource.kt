package com.gemini.energy.data.local

import android.util.Log
import com.gemini.energy.data.local.dao.AuditZoneTypeDao
import com.gemini.energy.data.local.model.AuditZoneTypeLocalModel
import io.reactivex.Observable

class TypeLocalDataSource(
        private val auditZoneTypeDao: AuditZoneTypeDao) {

    fun getAllTypeByZone(id: Int, type: String): Observable<List<AuditZoneTypeLocalModel>> = auditZoneTypeDao.getAllTypeByZone(id, type).toObservable()

    fun save(auditZoneType: AuditZoneTypeLocalModel): Observable<Unit> {
        return Observable.fromCallable {
            Log.d("Zone Local Data Source", "SAVE - ZONE -- ZONE DAO")
            auditZoneTypeDao.insert(auditZoneType)
        }
    }

}