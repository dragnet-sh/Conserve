package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.device.plugload.Refrigerator
import io.reactivex.Flowable

class Hvac : IComputable {

    override fun compute(): Flowable<Boolean> {
        Log.d(TAG, "<< HVAC :: COMPUTE >> [Start] - (${Thread.currentThread().name})")
        return Flowable.just(false)
    }

    companion object {
        private const val TAG = "HVAC"
    }
}
