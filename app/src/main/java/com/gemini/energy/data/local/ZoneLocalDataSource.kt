package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.local.model.ZoneLocalModel
import io.reactivex.Observable

class ZoneLocalDataSource(private val zoneDao: ZoneDao) {

    fun getAllByAudit(id: Int): Observable<List<ZoneLocalModel>> = zoneDao.getAllByAudit(id).toObservable()

    fun save(zone: ZoneLocalModel): Observable<Unit> {
        return Observable.fromCallable {
            zoneDao.insert(zone)
        }
    }
}