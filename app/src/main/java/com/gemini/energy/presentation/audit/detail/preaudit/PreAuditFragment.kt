package com.gemini.energy.presentation.audit.detail.preaudit

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.base.BaseFormFragment
import com.gemini.energy.presentation.form.model.GElements
import com.thejuki.kformmaster.model.BaseFormElement
import java.util.*
import javax.inject.Inject

class PreAuditFragment : BaseFormFragment() {

    private var auditModel: AuditModel? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val featureSaveViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(PreAuditCreateViewModel::class.java)
    }

    private val featureListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(PreAuditGetViewModel::class.java)
    }

    /**
     * UtilityRate Methods for the Base Class
     * */
    override fun resourceId(): Int? {
        return R.raw.preaudit
    }

    override fun getAuditId(): Long? {
        return auditModel?.id
    }

    override fun getZoneId(): Int? {
        return null
    }

    /**
     * These methods are executed on the Base Class
     * */
    override fun executeListeners() {
        featureListViewModel.result
                .observe(this, Observer {
                    super.refreshFormData(it)
                })
    }

    override fun loadFeatureData() {
        auditModel?.let {
            featureListViewModel.loadFeature(it.id)
        }
    }

    override fun createFeatureData(formData: MutableList<Feature>) {
        auditModel?.let {
            featureSaveViewModel.createFeature(formData, it.id)
        }
    }

    /**
     * Feature Builder - ToDo: If this gets complex create a Feature Factory Builder
     * */
    override fun buildFeature(gElement: GElements, gFormElement: BaseFormElement<*>): Feature? {
        var feature: Feature? = null
        val date = Date()
        auditModel?.let {
            feature = Feature(null, gElement.id, BELONGS_TO, gElement.dataType, -1,
                    it.id, null, null, gElement.param, gFormElement.valueAsString,
                    null, null, date, date)
        }

        return feature
    }

    /**
     * Audit Model gets Set Via Audit Activity
     * */
    fun setAuditModel(auditModel: AuditModel) {
        this.auditModel = auditModel
        super.loadForm()
    }

    companion object {
        fun newInstance(): PreAuditFragment {
            return PreAuditFragment()
        }

        private const val TAG = "PreAuditFragment"
        private const val BELONGS_TO = "preaudit"
    }

}