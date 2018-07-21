package com.gemini.energy.service.device.lighting

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import io.reactivex.Flowable

class Cfl : IComputable {

    override fun compute(): Flowable<List<OutgoingRows>> {
        return Flowable.just(listOf())
    }
}