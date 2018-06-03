package com.gemini.energy.presentation.audit.detail.zone.dialog

import android.app.Application
import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.domain.interactor.ZoneSaveUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class ZoneCreateViewModel(context: Context, private val zoneCreateUseCase: ZoneSaveUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val _result = SingleLiveData<Boolean>()
    val result = _result

    private val _error = SingleLiveData<String>()
    val error = _error

    fun createZone(auditId: Int, zoneTag: String) {
        val date = Date()
        addDisposable(save(Zone(null, zoneTag, "Sample Zone", auditId, date, date)))
    }

    private fun save(zone: Zone): Disposable {
        return zoneCreateUseCase.execute(zone)
                .subscribeWith(object : DisposableObserver<Unit>() {

                    override fun onNext(t: Unit) {
                        Log.d(TAG, "ON-NEXT !!! \\m/")
                        result.value = true
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage ?: e.message ?:
                                context.getString(R.string.unknown_error)
                    }

                    override fun onComplete() {}
                })
    }

    companion object {
        private const val TAG = "ZoneCreateViewModel"
    }

}