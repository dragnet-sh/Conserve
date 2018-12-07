package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.FeatureDao
import com.gemini.energy.data.local.model.FeatureLocalModel
import io.reactivex.Observable

class FeatureLocalDataSource(private val featureDao: FeatureDao) {

    fun getAllByAudit(id: Long): Observable<List<FeatureLocalModel>> = featureDao.getAllByAudit(id).toObservable()
    fun getAllByType(id: Int): Observable<List<FeatureLocalModel>> = featureDao.getAllByType(id).toObservable()

    fun save(feature: List<FeatureLocalModel>): Observable<Unit> = Observable.fromCallable {
        featureDao.insert(feature)
    }

    fun delete(feature: List<FeatureLocalModel>): Observable<Unit> = Observable.fromCallable {
        featureDao.deleteByType(feature)
    }

    fun deleteByTypeId(id: Int): Observable<Unit> = Observable.fromCallable {
        featureDao.deleteByTypeId(id)
    }

    fun deleteByAuditId(id: Long): Observable<Unit> = Observable.fromCallable {
        featureDao.deleteByAuditId(id)
    }

    fun deleteByZoneId(id: Int): Observable<Unit> = Observable.fromCallable {
        featureDao.deleteByZoneId(id)
    }

}