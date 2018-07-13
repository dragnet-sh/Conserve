package com.gemini.energy.data.local

import com.gemini.energy.data.local.dao.ComputableDao
import com.gemini.energy.data.local.model.ComputableLocalModel
import io.reactivex.Observable

class ComputableLocalDataSource(private val computableDao: ComputableDao) {

    fun getAllComputable(): Observable<List<ComputableLocalModel>> = computableDao.getAllComputable().toObservable()

}