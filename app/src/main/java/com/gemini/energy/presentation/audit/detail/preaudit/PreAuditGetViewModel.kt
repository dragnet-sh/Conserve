package com.gemini.energy.presentation.audit.detail.preaudit

import android.app.Application
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.interactor.FeatureGetAllUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class PreAuditGetViewModel(context: Context, private val featureGetAllUseCase: FeatureGetAllUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val _status= SingleLiveData<Boolean>()
    val status = _status

    val loading = ObservableBoolean()
    var result: List<Feature> = listOf()
    val empty = ObservableBoolean()
    val error = ObservableField<String>()

    fun loadFeature(auditId: Int) = addDisposable(getAll(auditId))

    private fun getAll(auditId: Int): Disposable {
        return featureGetAllUseCase.execute(auditId)
                .subscribeWith(object : DisposableObserver<List<Feature>>() {

                    override fun onStart() {
                        loading.set(true)
                    }

                    override fun onNext(t: List<Feature>) {
                        loading.set(false)
                        result = t
                        empty.set(t.isEmpty())
                        status.value = true

                        Log.d(TAG, "ON-NEXT !!! \\m/")
                    }

                    override fun onError(e: Throwable) {
                        loading.set(false)
                        error.set(e.localizedMessage ?: e.message ?: context.getString(R.string.unknown_error))
                    }

                    override fun onComplete() {}
                })
    }

    companion object {
        private const val TAG = "PreAuditGetViewModel"
    }

}