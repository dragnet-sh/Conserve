package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.GravesDao
import com.gemini.energy.data.local.model.GraveLocalModel
import io.reactivex.Observable

class GravesLocalDataSource(private val gravesDao: GravesDao) {

    fun save(grave: GraveLocalModel): Observable<Unit> = Observable.fromCallable {
        gravesDao.insert(grave)
    }

    fun update(oid: Int, usn: Int): Observable<Unit> = Observable.fromCallable {
        gravesDao.update(oid, usn)
    }

    fun delete(oid: Int): Observable<Unit> = Observable.fromCallable {
        gravesDao.delete(oid)
    }

}