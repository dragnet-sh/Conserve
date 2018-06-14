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
import kotlinx.android.synthetic.main.fragment_preaudit.*

open abstract class BaseFormFragment : DaggerFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preaudit, container, false)
    }

    abstract fun resourceId(): Int
}