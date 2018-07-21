package com.gemini.energy.service.device.plugload

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import io.reactivex.Flowable

class IceMaker : IComputable {

    override fun compute(): Flowable<List<OutgoingRows>> {
        return Flowable.just(listOf())
    }
}