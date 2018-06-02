package com.gemini.energy.presentation.audit.detail.zone.list

import android.app.Application
import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.domain.interactor.ZoneGetAllUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.presentation.audit.detail.zone.list.mapper.ZoneMapper
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class ZoneListViewModel(context: Context, private val zoneGetAllUseCase: ZoneGetAllUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val mapper = ZoneMapper(context)

    val loading = ObservableBoolean()
    val result = ObservableArrayList<ZoneModel>()
    val empty = ObservableBoolean()
    val error = ObservableField<String>()

    fun loadZoneList(auditId: Int) = addDisposable(getAll(auditId))

    private fun getAll(auditId: Int): Disposable {
        return zoneGetAllUseCase.execute(auditId)
                .subscribeWith(object : DisposableObserver<List<Zone>>() {

                    override fun onStart() {
                        loading.set(true)
                    }

                    override fun onNext(t: List<Zone>) {
                        loading.set(false)
                        result.clear()
                        result.addAll(mapper.toModel(t))
                        empty.set(t.isEmpty())

                        Log.d(TAG, t.toString())
                    }

                    override fun onError(t: Throwable) {
                        loading.set(false)
                        error.set(t.localizedMessage ?: t.message
                        ?: context.getString(R.string.unknown_error))
                    }

                    override fun onComplete() {}

                })
    }

    companion object {
        private const val TAG = "ZoneListViewModel"
    }

}