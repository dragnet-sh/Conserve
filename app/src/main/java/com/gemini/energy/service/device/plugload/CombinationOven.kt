package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class CombinationOven : IComputable {

    override fun compute(): Flowable<Boolean> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(true)
    }
}