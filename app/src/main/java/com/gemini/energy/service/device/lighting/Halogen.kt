package com.gemini.energy.service.device.lighting

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable
import io.reactivex.Observable

class Halogen : IComputable {

    override fun compute(): Observable<Computable<*>> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Observable.just(Computable(null))
    }
}