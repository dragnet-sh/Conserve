package com.gemini.energy.service.device.plugload

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRow
import io.reactivex.Flowable

class SteamCooker : IComputable {

    override fun compute(): Flowable<List<OutgoingRow>> {
        return Flowable.just(listOf())
    }

}