package com.gemini.energy.data.repository

import com.gemini.energy.data.local.TypeLocalDataSource
import com.gemini.energy.data.local.model.TypeLocalModel
import com.gemini.energy.data.remote.TypeRemoteDataSource
import com.gemini.energy.data.repository.mapper.TypeMapper
import com.gemini.energy.domain.entity.Type
import io.reactivex.Observable

class TypeRepository(
        private val typeLocalDataSource: TypeLocalDataSource,
        private val typeRemoteDataSource: TypeRemoteDataSource,
        private val auditScopeMapper: TypeMapper) {

    fun getAllTypeByZone(id: Int, type: String): Observable<List<TypeLocalModel>> {
        return typeLocalDataSource.getAllTypeByZone(id, type)
    }

    fun save(auditScope: Type): Observable<Unit> {
        return typeLocalDataSource.save(auditScopeMapper.toLocal(auditScope))
    }

}