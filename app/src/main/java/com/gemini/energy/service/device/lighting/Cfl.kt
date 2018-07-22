package com.gemini.energy.service.device.lighting

import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class Cfl : IComputable {

    override fun compute(): Flowable<Boolean> {
        return Flowable.just(true)
    }
}