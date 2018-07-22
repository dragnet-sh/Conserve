package com.gemini.energy.service.device.lighting

import android.util.Log
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class LinearFluorescent : IComputable {

    override fun compute(): Flowable<Boolean> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(true)
    }
}