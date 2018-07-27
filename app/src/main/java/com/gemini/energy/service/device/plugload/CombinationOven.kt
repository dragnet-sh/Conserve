package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable
import io.reactivex.Observable

class CombinationOven : IComputable {

    override fun compute(): Observable<Computable<*>> {
        Log.d(TAG, "<< CombinationOven :: COMPUTE >> [Start] - (${Thread.currentThread().name})")
        return Observable.just(Computable(null))
    }

    companion object {
        private const val TAG = "CombinationOven"
    }
}
