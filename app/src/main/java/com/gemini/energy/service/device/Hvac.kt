package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Observable

class Hvac : IComputable {

    override fun compute(): Observable<Computable<*>> {
        Log.d(TAG, "<< HVAC :: COMPUTE >> [Start] - (${Thread.currentThread().name})")
        return Observable.just(Computable(null))
    }

    companion object {
        private const val TAG = "HVAC"
    }
}
