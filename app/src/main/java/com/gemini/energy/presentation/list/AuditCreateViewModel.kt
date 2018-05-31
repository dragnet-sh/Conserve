package com.gemini.energy.presentation.list

import android.app.Application
import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.interactor.AuditSaveUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class AuditCreateViewModel(context: Context, private val auditSaveUseCase: AuditSaveUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val _result = SingleLiveData<Boolean>()
    val result = _result

    private val _error = SingleLiveData<String>()
    val error = _error

    fun createAudit(id: Int, tag: String) {
        val date = Date()
        addDisposable(save(Audit(id, tag, date, date)))
    }

    private fun save(audit: Audit): Disposable {
        return auditSaveUseCase.execute(audit)
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
        private const val TAG = "AuditCreateViewModel"
    }
}