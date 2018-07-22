package com.gemini.energy.service.device.plugload

import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class ConveyorOven : IComputable {

    override fun compute(): Flowable<Boolean> {
        return Flowable.just(true)
    }
}