package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRow
import io.reactivex.Flowable

class General : IComputable {

    override fun compute(): Flowable<List<OutgoingRow>> {
        Log.d(this.javaClass.simpleName, "COMPUTE")
        return Flowable.just(listOf())
    }

}
