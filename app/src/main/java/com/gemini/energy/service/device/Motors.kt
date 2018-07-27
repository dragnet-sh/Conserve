package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable
import io.reactivex.Observable

class Motors : IComputable {

    override fun compute(): Observable<Computable<*>> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Observable.just(Computable(null))
    }
}
