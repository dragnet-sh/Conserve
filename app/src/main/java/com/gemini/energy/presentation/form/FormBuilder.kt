package com.gemini.energy.presentation.form

import com.gemini.energy.presentation.form.model.GElements
import com.gemini.energy.presentation.util.BaseRowType
import com.thejuki.kformmaster.model.BaseFormElement
import com.thejuki.kformmaster.model.FormHeader

class FormBuilder {

    fun build(section: String, elements: List<GElements>): MutableList<BaseFormElement<*>> {

        val collectRows: MutableList<BaseFormElement<*>> = mutableListOf()

        collectRows.add(FormHeader.createInstance(section))
        for (item in elements) {
            val type = BaseRowType.get(item.dataType!!)
            val row = instantiate(type!!).create(item)

            // *** 1. Add Validation :: [Mandatory | Optional] *** //
            // *** 2. Add Hints :: Placeholder Text *** //
            //ToDo: This is the place where you can implement other Types of Form Validation
            if (item.validation == MANDATORY) { row.setRequired(true) }
            row.setHint(item.hint)

            collectRows.add(row)
        }

        return collectRows

    }

    companion object {
        private const val MANDATORY = "mandatory"
        fun instantiate(type: BaseRowType): IFormElement {
            return when (type) {
                BaseRowType.IntRow -> IntRow()
                BaseRowType.TextRow -> TextRow()
                BaseRowType.DecimalRow -> IntRow()
                BaseRowType.PickerInputRow -> PickerInputRow()
                BaseRowType.EmailRow -> EmailRow()
                BaseRowType.PhoneRow -> PhoneRow()
                BaseRowType.TextAreaRow -> TextRow()
            }
        }
    }
}

