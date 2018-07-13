package com.gemini.energy.data.repository

import com.gemini.energy.data.local.ComputableLocalDataSource
import com.gemini.energy.data.local.model.ComputableLocalModel
import io.reactivex.Observable

class ComputableRepository(
        private val computableLocalDataSource: ComputableLocalDataSource) {

    fun getAllComputable(): Observable<List<ComputableLocalModel>> {
        return computableLocalDataSource.getAllComputable()
    }

}