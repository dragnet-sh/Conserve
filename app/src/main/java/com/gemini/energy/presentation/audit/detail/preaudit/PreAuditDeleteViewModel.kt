package com.gemini.energy.presentation.audit.detail.preaudit

import android.app.Application
import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.interactor.FeatureDeleteUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class PreAuditDeleteViewModel(context: Context, private val featureDeleteUseCase: FeatureDeleteUseCase)
    : BaseAndroidViewModel(context.applicationContext as Application) {

    private val _result = SingleLiveData<Boolean>()
    val result = _result

    private val _error = SingleLiveData<String>()
    val error = _error

    fun deleteFeature(feature: List<Feature>) = addDisposable(delete(feature))

    private fun delete(feature: List<Feature>): Disposable {
        return featureDeleteUseCase.execute(feature)
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
        private const val TAG = "PreAuditDeleteViewModel"
    }
}