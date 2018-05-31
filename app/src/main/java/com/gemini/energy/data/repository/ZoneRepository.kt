package com.gemini.energy.data.repository

import com.gemini.energy.data.local.ZoneLocalDataSource
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.data.repository.mapper.ZoneMapper
import com.gemini.energy.data.remote.ZoneRemoteDataSource
import com.gemini.energy.domain.entity.Zone
import io.reactivex.Observable

class ZoneRepository(
        private val zoneLocalDataSource: ZoneLocalDataSource,
        private val zoneRemoteDataSource: ZoneRemoteDataSource,
        private val zoneMapper: ZoneMapper) {

    fun getAllByAudit(id: Int): Observable<List<ZoneLocalModel>> {
        return zoneLocalDataSource.getAllByAudit(id)
    }

    fun save(zone: Zone): Observable<Unit> {
        return zoneLocalDataSource.save(zoneMapper.toLocal(zone))
    }

}