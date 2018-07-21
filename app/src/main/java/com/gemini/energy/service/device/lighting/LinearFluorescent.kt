package com.gemini.energy.service.device.lighting

import android.util.Log
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRow
import io.reactivex.Flowable

class LinearFluorescent : IComputable {

    override fun compute(): Flowable<List<OutgoingRow>> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(listOf())
    }
}