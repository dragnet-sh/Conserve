package com.gemini.energy.presentation.form.model


class GEnergyFormModel {
    val geminiForm: List<GFormBlock>? = null
}

class GFormBlock {
    val id: Int? = null
    val index: Int? = null
    val section: String? = null
    val elements: List<GElements>? = null
}

class GElements {
    val id: Int? = null
    val index: Int? = null
    val param: String? = null
    val dataType: String? = null
    val validation: String? = null
    val defaultValues: String? = null
}
