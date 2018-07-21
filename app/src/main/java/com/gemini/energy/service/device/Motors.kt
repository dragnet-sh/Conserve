package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows
import io.reactivex.Flowable

class Motors : IComputable {

    override fun compute(): Flowable<List<OutgoingRows>> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(listOf())
    }
}
