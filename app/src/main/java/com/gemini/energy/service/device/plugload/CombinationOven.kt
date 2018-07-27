package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class CombinationOven : IComputable {

    override fun compute(): Flowable<Boolean> {
        Log.d(TAG, "<< CombinationOven :: COMPUTE >> [Start] - (${Thread.currentThread().name})")
        return Flowable.just(false)
    }

    companion object {
        private const val TAG = "CombinationOven"
    }
}
