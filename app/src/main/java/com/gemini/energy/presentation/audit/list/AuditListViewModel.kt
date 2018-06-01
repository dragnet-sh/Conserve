package com.gemini.energy.presentation.audit.list

import android.app.Application
import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.presentation.audit.list.mapper.AuditMapper
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.interactor.AuditGetAllUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class AuditListViewModel(context: Context, private val auditGetAllUseCase: AuditGetAllUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val mapper = AuditMapper(context)

    val loading = ObservableBoolean()
    val result = ObservableArrayList<AuditModel>()
    val empty = ObservableBoolean()
    val error = ObservableField<String>()

    fun loadAuditList() = addDisposable(getAll())

    private fun getAll(): Disposable {
        return auditGetAllUseCase.execute()
                .subscribeWith(object : DisposableObserver<List<Audit>>() {

                    override fun onStart() {
                        loading.set(true)
                    }

                    override fun onNext(t: List<Audit>) {
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
        private const val TAG = "AuditListViewModel"
    }
}