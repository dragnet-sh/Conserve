package com.gemini.energy.service.device.plugload

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import io.reactivex.Observable

class PreRinseSpray: IComputable {

    override fun compute(): Observable<Computable<*>> {
        return Observable.just(Computable(null))
    }

}