package com.gemini.energy.internal.util

import com.gemini.energy.presentation.form.PickerInputRow
import com.gemini.energy.presentation.util.BaseRowType
import com.thejuki.kformmaster.helper.FormBuildHelper
import com.thejuki.kformmaster.model.*

fun <T> lazyThreadSafetyNone(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun getFormElement(formBuilder: FormBuildHelper, eBaseRowType: BaseRowType, id: Int): BaseFormElement<*> =
        when (eBaseRowType) {
            BaseRowType.TextRow -> formBuilder.getFormElement<FormSingleLineEditTextElement>(id)
            BaseRowType.DecimalRow -> formBuilder.getFormElement<FormNumberEditTextElement>(id)
            BaseRowType.IntRow -> formBuilder.getFormElement<FormNumberEditTextElement>(id)
            BaseRowType.EmailRow -> formBuilder.getFormElement<FormEmailEditTextElement>(id)
            BaseRowType.PhoneRow -> formBuilder.getFormElement<FormPhoneEditTextElement>(id)
            BaseRowType.PickerInputRow -> formBuilder.getFormElement<FormPickerDropDownElement<PickerInputRow.ListItem>>(id)
            BaseRowType.TextAreaRow -> formBuilder.getFormElement<FormSingleLineEditTextElement>(id)
        }