package com.gemini.energy.presentation.feature

import com.gemini.energy.presentation.feature.model.GElements
import com.thejuki.kformmaster.model.*
import java.io.Serializable

interface IFormElement {
    fun create(gElements: GElements): BaseFormElement<*>
}

abstract class FormFactory : IFormElement

class TextRow : FormFactory() {
    override fun create(gElement: GElements): BaseFormElement<*> {
        return FormSingleLineEditTextElement(gElement.id!!).apply {
            this.title = gElement.param
            this.hint = gElement.defaultValues
        }
    }
}

class IntRow : FormFactory() {
    override fun create(gElement: GElements): BaseFormElement<*> {
        return FormNumberEditTextElement(gElement.id!!).apply {
            this.title = gElement.param
            this.hint = gElement.defaultValues
        }
    }
}

class PickerInputRow : FormFactory() {
    override fun create(gElement: GElements): BaseFormElement<*> {
        return FormPickerDropDownElement<ListItem>(gElement.id!!).apply {

            val sample = gElement.defaultValues
            val optionItems = sample?.split(",")
            var listOptionItem: MutableList<ListItem> = mutableListOf()

            optionItems?.forEach {
                val optionItem= it.split(":")
                if (optionItem.count() == 2) {
                    listOptionItem.add(ListItem(optionItem[0].toLong(), optionItem[1]))
                }
            }

            println(listOptionItem)

            this.options = listOptionItem
            this.title = gElement.param
        }
    }

    data class ListItem(val id: Long? = null,
                        val name: String? = null
    ): Serializable {
        override fun toString(): String {
            return name.orEmpty()
        }
    }
}



