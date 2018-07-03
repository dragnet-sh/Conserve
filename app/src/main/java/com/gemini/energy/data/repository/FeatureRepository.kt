package com.gemini.energy.data.repository

import com.gemini.energy.data.local.FeatureLocalDataSource
import com.gemini.energy.data.local.model.FeatureLocalModel
import com.gemini.energy.data.remote.FeatureRemoteDataSource
import com.gemini.energy.data.repository.mapper.FeatureMapper
import com.gemini.energy.domain.entity.Feature
import io.reactivex.Observable

class FeatureRepository(
        private val featureLocalDataSource: FeatureLocalDataSource,
        private val featureRemoteDataSource: FeatureRemoteDataSource,
        private val featureMapper: FeatureMapper) {

    fun getAllByAudit(id: Int): Observable<List<FeatureLocalModel>> {
        return featureLocalDataSource.getAllByAudit(id)
    }

    fun save(feature: List<Feature>): Observable<Unit> {
        return featureLocalDataSource.save(featureMapper.toLocal(feature))
    }
}