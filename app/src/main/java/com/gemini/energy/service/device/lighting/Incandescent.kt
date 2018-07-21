package com.gemini.energy.service.device.lighting

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRow
import io.reactivex.Flowable

class Incandescent : IComputable {

    override fun compute(): Flowable<List<OutgoingRow>> {
        return Flowable.just(listOf())
    }

}