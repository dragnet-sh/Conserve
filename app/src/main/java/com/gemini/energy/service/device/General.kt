package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.service.IComputable
import io.reactivex.Flowable

class General : IComputable {

    override fun compute(): Flowable<Boolean> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(true)
    }

}
