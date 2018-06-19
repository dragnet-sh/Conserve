package com.gemini.energy.presentation.form

import com.gemini.energy.presentation.form.model.GElements
import com.thejuki.kformmaster.model.BaseFormElement
import com.thejuki.kformmaster.model.FormHeader


class FormBuilder {

    fun build(section: String, elements: List<GElements>): MutableList<BaseFormElement<*>> {

        val collectRows: MutableList<BaseFormElement<*>> = mutableListOf()

        collectRows.add(FormHeader.createInstance(section))
        for (item in elements) {
            val type = BaseRowType.get(item.dataType!!)
            collectRows.add(instantiate(type!!).create(item))
        }

        return collectRows

    }

    companion object {
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

    enum class BaseRowType(val value: String) {

        TextRow("textrow"),
        IntRow("introw"),
        DecimalRow("decimalrow"),
        PickerInputRow("pickerinputrow"),
        PhoneRow("phonerow"),
        EmailRow("emailrow"),
        TextAreaRow("textarearow");

        companion object {
            private val map = BaseRowType.values().associateBy(BaseRowType::value)
            fun get(type: String) = map[type]
        }

    }

}

