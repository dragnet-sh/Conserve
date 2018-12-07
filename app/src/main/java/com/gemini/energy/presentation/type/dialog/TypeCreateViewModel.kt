package com.gemini.energy.presentation.type.dialog

import android.app.Application
import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.domain.interactor.ZoneTypeGetUseCase
import com.gemini.energy.domain.interactor.ZoneTypeSaveUseCase
import com.gemini.energy.domain.interactor.ZoneTypeUpdateUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import com.gemini.energy.presentation.type.list.model.TypeModel
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import timber.log.Timber
import java.util.*

class TypeCreateViewModel(context: Context,

                          private val zoneTypeCreateUseCase: ZoneTypeSaveUseCase,
                          private val zoneTypeGetUseCase: ZoneTypeGetUseCase,
                          private val zoneTypeUpdateUseCase: ZoneTypeUpdateUseCase) :

        BaseAndroidViewModel(context.applicationContext as Application) {

    private val _result = SingleLiveData<Boolean>()
    val result = _result

    private val _error = SingleLiveData<String>()
    val error = _error


    fun createZoneType(zoneId: Int, zoneType: String, zoneSubType: String?, zoneTypeTag: String, auditId: Long) {
        val date = Date()
        addDisposable(save(Type(null, zoneTypeTag, zoneType, zoneSubType, -1, zoneId, auditId, date, date)))
    }

    fun updateZoneType(type: TypeModel, scopeName: String) = addDisposable(
            zoneTypeGetUseCase.execute(type.id)
                    .subscribe{
                        it.name = scopeName
                        it.usn = -1
                        it.updatedAt = Date()
                        update(it)
                    }
    )

    private fun save(scope: Type): Disposable {
        return zoneTypeCreateUseCase.execute(scope)
                .subscribeWith(object : DisposableObserver<Unit>() {

                    override fun onNext(t: Unit) {
                        Timber.d("ON-NEXT !!! \\m/")
                        result.value = true
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage ?: e.message ?:
                                context.getString(R.string.unknown_error)
                    }

                    override fun onComplete() {}
                })
    }

    private fun update(type: Type): Disposable {
        return zoneTypeUpdateUseCase.execute(type)
                .subscribeWith(object : DisposableObserver<Unit>() {
                    override fun onNext(t: Unit) {
                        Timber.d("ON-NEXT !!! \\m/ -- [[ Type Update ]]")
                        result.value = true
                    }
                    override fun onError(e: Throwable) { e.printStackTrace() }
                    override fun onComplete() {}
                })
    }

}