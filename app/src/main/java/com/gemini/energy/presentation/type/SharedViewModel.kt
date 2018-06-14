package com.gemini.energy.presentation.type

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.gemini.energy.presentation.type.list.model.TypeModel

class SharedViewModel : ViewModel() {

    private var type: MutableLiveData<TypeModel> = MutableLiveData()

    fun setType(type: TypeModel) { this.type.value = type }
    fun getType(): LiveData<TypeModel> = this.type

}