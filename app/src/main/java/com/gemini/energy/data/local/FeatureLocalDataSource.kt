package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.FeatureDao
import com.gemini.energy.data.local.model.FeatureLocalModel
import io.reactivex.Observable

class FeatureLocalDataSource(private val featureDao: FeatureDao) {

    fun getAllByAudit(id: Int): Observable<List<FeatureLocalModel>> = featureDao.getAllByAudit(id).toObservable()

    fun save(feature: List<FeatureLocalModel>): Observable<Unit> = Observable.fromCallable {
        featureDao.insert(feature)
    }

    fun delete(feature: List<FeatureLocalModel>): Observable<Unit> = Observable.fromCallable {
        featureDao.deleteByType(feature)
    }

}