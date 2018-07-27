package com.gemini.energy.service.device.lighting

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable
import io.reactivex.Observable

class Incandescent : IComputable {

    override fun compute(): Observable<Computable<*>> {
        return Observable.just(Computable(null))
    }

}