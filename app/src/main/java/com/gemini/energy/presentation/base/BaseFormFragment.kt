package com.gemini.energy.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.internal.util.getFormElement
import com.gemini.energy.presentation.form.FormBuilder
import com.gemini.energy.presentation.form.FormMapper
import com.gemini.energy.presentation.form.model.GElements
import com.gemini.energy.presentation.util.BaseRowType
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.BaseFormElement
import com.thejuki.kformmaster.model.FormButtonElement
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_form.*

abstract class BaseFormFragment : DaggerFragment() {

    //ToDo: Try injecting Context !!
    private lateinit var formBuilder: FormBuildHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.formBuilder = FormBuildHelper(context!!, cacheForm = true)
        this.formBuilder.attachRecyclerView(context!!, recyclerView, autoMeasureEnabled = true)

        loadForm()
        executeListeners()
    }

    fun loadForm() {

        if (resourceId() == -1) return

        val elements: MutableList<BaseFormElement<*>> = mutableListOf()
        val gFormBuilder = FormBuilder()
        val gForm = getFormMapper().mapSectionIdsToElements(getModel())
        val gMapSId = getFormMapper().mapSectionIdsToName(getModel())
        val indexSortedSection = getFormMapper().sortedElementIds(getModel())

        for (sectionId in indexSortedSection) {
            elements.addAll(gFormBuilder.build(gMapSId[sectionId]!!, gForm[sectionId]!!))
        }

        elements.add(btnSave())
        formBuilder.refresh()
        formBuilder.addFormElements(elements)

        loadFeatureData()
    }

    protected fun refreshFormData(feature: List<Feature>?) {
        val mappedFeatureById = feature?.associateBy { it.formId }
        getFormIds().forEach { id ->
            val gElement = getGFormElements()[id] as GElements
            val eBaseRowType = BaseRowType.get(gElement.dataType!!)

            if (mappedFeatureById?.containsKey(id) == true) {
                eBaseRowType?.let {
                    getFormElement(formBuilder, it, id)
                            .setValue(mappedFeatureById.getValue(id).valueString)
                }
            }
        }
    }

    private fun saveForm() {
        val formData: MutableList<Feature> = mutableListOf()
        var isValid = true
        getFormIds().forEach { id ->
            val gElement = getGFormElements()[id] as GElements
            val eBaseRowType = BaseRowType.get(gElement.dataType!!)

            eBaseRowType?.let { type ->
                val gFormElement = getFormElement(formBuilder, type, id)

                if (!gFormElement.isValid) {
                    // 1. Check if the Form is Valid
                    // 2. If Valid create feature data
                    // 3. If InValid give error message
                    gFormElement.setError("Cannot be Empty !!")
                    isValid = false
                }

                buildFeature(gElement, gFormElement)?.let {
                    formData.add(it)
                }
            }
        }

        if (isValid) { createFeatureData(formData) }
    }

    private fun btnSave(): FormButtonElement {
        val button = FormButtonElement()
        button.value = SAVE
        button.enabled = true
        button.valueObservers.add {_, _ -> saveForm()}

        return button
    }

    private fun getFormMapper() = FormMapper(context!!, resourceId())
    private fun getModel() = getFormMapper().decodeJSON()
    private fun getFormIds() = getFormMapper().sortedFormElementIds(getModel())
    private fun getGFormElements() = getFormMapper().mapIdToElements(getModel())

    abstract fun resourceId(): Int?
    abstract fun getAuditId(): Int?
    abstract fun getZoneId(): Int?

    abstract fun buildFeature(gElement: GElements, gFormElement: BaseFormElement<*>): Feature?
    abstract fun loadFeatureData()
    abstract fun executeListeners()
    abstract fun createFeatureData(formData: MutableList<Feature>)

    companion object {
        private const val TAG = "BaseFormFragment"
        private const val SAVE = "SAVE"
    }
}