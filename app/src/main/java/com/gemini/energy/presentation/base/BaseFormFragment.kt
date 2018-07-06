package com.gemini.energy.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.presentation.form.FormBuilder
import com.gemini.energy.presentation.form.FormMapper
import com.gemini.energy.presentation.form.PickerInputRow
import com.gemini.energy.presentation.form.model.GElements
import com.gemini.energy.presentation.util.BaseRowType
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_form.*
import java.util.*

abstract class BaseFormFragment : DaggerFragment() {

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

        val elements: MutableList<BaseFormElement<*>> = mutableListOf()
        val mapper = FormMapper(context!!, resourceId())
        val model = mapper.decodeJSON()

        val gFormBuilder = FormBuilder()
        val gForm = mapper.mapSectionIdsToElements(model)
        val gMapSId = mapper.mapSectionIdsToName(model)
        val indexSortedSection = mapper.sortedElementIds(model)

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
        val mapper = FormMapper(context!!, resourceId())
        val model = mapper.decodeJSON()
        val formIds = mapper.sortedFormElementIds(model)
        val gFormElements = mapper.mapIdToElements(model)

        formIds.forEach { id ->
            val gElement = gFormElements[id] as GElements
            val eBaseRowType = BaseRowType.get(gElement.dataType!!)

            if (mappedFeatureById?.containsKey(id) == true) {
                eBaseRowType?.let {getFormElement(it, id).setValue(
                        mappedFeatureById.getValue(id).valueString
                )}
            }
        }

    }

    private fun saveForm() {

        val mapper = FormMapper(context!!, resourceId())
        val model = mapper.decodeJSON()
        val formIds = mapper.sortedFormElementIds(model)
        val gFormElements = mapper.mapIdToElements(model)
        val formData: MutableList<Feature> = mutableListOf()

        formIds.forEach { id ->
            val gElement = gFormElements[id] as GElements
            val eBaseRowType = BaseRowType.get(gElement.dataType!!)

            eBaseRowType?.let { type ->
                val gFormElement = getFormElement(type, id)

                //ToDo: Make this better !!
                val date = Date()
                getAuditId()?.let {
                    val feature = Feature(null, gElement.id, "preaudit", gElement.dataType,
                            it, null, null, gElement.param, gFormElement.valueAsString,
                            null, null, date, date)

                    formData.add(feature)
                }
            }

        }

        createFeatureData(formData)
    }

    private fun btnSave(): FormButtonElement {
        val button = FormButtonElement()
        button.value = "SAVE"
        button.enabled = true
        button.valueObservers.add { _, _ ->
            saveForm()
        }

        return button
    }

    //ToDo: Move this to utility *** Will be required during Energy Calculations ***
    private fun getFormElement(eBaseRowType: BaseRowType, id: Int): BaseFormElement<*> =
            when (eBaseRowType) {
                BaseRowType.TextRow -> formBuilder.getFormElement<FormSingleLineEditTextElement>(id)
                BaseRowType.DecimalRow -> formBuilder.getFormElement<FormNumberEditTextElement>(id)
                BaseRowType.IntRow -> formBuilder.getFormElement<FormNumberEditTextElement>(id)
                BaseRowType.EmailRow -> formBuilder.getFormElement<FormEmailEditTextElement>(id)
                BaseRowType.PhoneRow -> formBuilder.getFormElement<FormPhoneEditTextElement>(id)
                BaseRowType.PickerInputRow -> formBuilder.getFormElement<FormPickerDropDownElement<PickerInputRow.ListItem>>(id)
                BaseRowType.TextAreaRow -> formBuilder.getFormElement<FormSingleLineEditTextElement>(id)
            }

    abstract fun resourceId(): Int
    abstract fun getAuditId(): Int?
    abstract fun getZoneId(): Int?

    abstract fun loadFeatureData()
    abstract fun executeListeners()
    abstract fun createFeatureData(formData: MutableList<Feature>)

    companion object {
        private const val TAG = "BaseFormFragment"
    }
}