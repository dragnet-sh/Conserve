package com.gemini.energy.data.local

import android.util.Log
import com.gemini.energy.data.local.dao.TypeDao
import com.gemini.energy.data.local.model.TypeLocalModel
import io.reactivex.Observable

class TypeLocalDataSource(
        private val auditZoneTypeDao: TypeDao) {

    fun getAllTypeByZone(id: Int, type: String): Observable<List<TypeLocalModel>> = auditZoneTypeDao.getAllTypeByZone(id, type).toObservable()

    fun save(auditZoneType: TypeLocalModel): Observable<Unit> {
        return Observable.fromCallable {
            Log.d("Zone Local Data Source", "SAVE - ZONE -- ZONE DAO")
            auditZoneTypeDao.insert(auditZoneType)
        }
    }

}