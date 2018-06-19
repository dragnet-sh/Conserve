package com.gemini.energy.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.presentation.form.FormBuilder
import com.gemini.energy.presentation.form.FormMapper
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.BaseFormElement
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_form.*

abstract class BaseFormFragment : DaggerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_form, container, false)
    }

    fun loadForm() {
        context?.let {
            val formBuilder = FormBuildHelper(it, cacheForm = true)
            formBuilder.attachRecyclerView(it, recyclerView, autoMeasureEnabled = true)

            val elements: MutableList<BaseFormElement<*>> = mutableListOf()
            val mapper = FormMapper(it, resourceId())
            val model = mapper.decodeJSON()

            val gFormBuilder = FormBuilder()
            val gForm = mapper.mapSectionIdsToElements(model)
            val gMapSId = mapper.mapSectionIdsToName(model)

            for((sectionId, gElements) in gForm) {
                gMapSId[sectionId]?.let {
                    elements.addAll(gFormBuilder.build(it, gElements))
                }
            }

            formBuilder.addFormElements(elements)
        }
    }

    abstract fun resourceId(): Int
}