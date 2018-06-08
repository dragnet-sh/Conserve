package com.gemini.energy.presentation.zone.list

import android.app.Application
import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.AuditScopeParent
import com.gemini.energy.domain.interactor.ZoneTypeGetAllUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.presentation.zone.list.mapper.TypeMapper
import com.gemini.energy.presentation.zone.list.model.TypeModel
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class TypeListViewModel(context: Context, private val zoneTypeGetAllUseCase: ZoneTypeGetAllUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val mapper = TypeMapper(context)

    val loading = ObservableBoolean()
    val result = ObservableArrayList<TypeModel>()
    val empty = ObservableBoolean()
    val error = ObservableField<String>()

    fun loadZoneTypeList(zoneId: Int, zoneType: String) = addDisposable(getAll(zoneId, zoneType))

    private fun getAll(zoneId: Int, zoneType: String): Disposable {
        return zoneTypeGetAllUseCase.execute(listOf(zoneId, zoneType))
                .subscribeWith(object : DisposableObserver<List<AuditScopeParent>>() {

                    override fun onStart() {
                        loading.set(true)
                    }

                    override fun onNext(t: List<AuditScopeParent>) {
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
        private const val TAG = "TypeListViewModel"
    }

}