package com.gemini.energy.presentation.audit

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.type.list.model.TypeModel

class SharedViewModel : ViewModel() {

    private var audit: MutableLiveData<AuditModel> = MutableLiveData()
    private var zone: MutableLiveData<ZoneModel> = MutableLiveData()
    private var type: MutableLiveData<TypeModel> = MutableLiveData()
    private var subType: MutableLiveData<TypeModel> = MutableLiveData()

    fun setAudit(audit: AuditModel) { this.audit.value = audit }
    fun getAudit() = this.audit

    fun setZone(zone: ZoneModel) { this.zone.value = zone }
    fun getZone() = this.zone

    fun setType(type: TypeModel) { this.type.value = type }
    fun getType() = this.type

    fun setSubType(type: TypeModel) { this.subType.value = type }
    fun getSubType() = this.subType

}