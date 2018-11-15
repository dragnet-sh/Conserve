package com.gemini.energy.data.repository

import com.gemini.energy.data.local.GravesLocalDataSource
import com.gemini.energy.data.local.model.GraveLocalModel
import io.reactivex.Observable

class GravesRepository(private val gravesLocalDataSource: GravesLocalDataSource) {

    fun save(grave: GraveLocalModel): Observable<Unit> = gravesLocalDataSource.save(grave)
    fun update(oid: Int, usn: Int): Observable<Unit> = gravesLocalDataSource.update(oid, usn)
    fun delete(oid: Int): Observable<Unit> = gravesLocalDataSource.delete(oid)

}