package com.gemini.energy

import com.gemini.energy.presentation.form.model.GElements
import com.gemini.energy.presentation.form.model.GEnergyFormModel
import com.squareup.moshi.Moshi
import com.thejuki.kformmaster.model.*
import org.junit.Test
import java.io.Serializable

//TextRow, PhoneRow, EmailRow, TextAreaRow,
//IntRow, DecimalRow, PickerInputRow,
//ButtonRow


enum class BaseRowType(val value: String) {

    TextRow("textrow"),
    IntRow("introw"),
    PickerInputRow("pickerinputrow");

    companion object {
        private val map = BaseRowType.values().associateBy(BaseRowType::value)
        fun get(type: String) = map[type]
    }

}

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
            return when(type) {
                BaseRowType.IntRow -> FactoryIntRow()
                BaseRowType.TextRow -> FactoryTextRow()
                BaseRowType.PickerInputRow -> FactoryPickerInputRow()
            }
        }
    }

}


class FormBuilderTest {

    @Test
    fun testBuild() {

        val formModel = FormElementMapper.decodeJSON()
        var builder = FormBuilder()
        val elements: MutableList<BaseFormElement<*>> = mutableListOf()

        formModel?.let {
            var gFormElements = FormElementMapper.mapSectionIdsToElements(model = formModel)
            var gFormSections = FormElementMapper.mapSectionIdsToName(model = formModel)

            for ((key, value) in gFormElements!!) {
                elements.addAll(builder.build(gFormSections!!.getValue(key), value))
            }

        }

        print(elements)

    }
}






interface IFormElement {
    fun create(gElement: GElements): BaseFormElement<*>
}

class FactoryTextRow : IFormElement {
    override fun create(gElement: GElements): BaseFormElement<*> {
        return FormTextViewElement(gElement.id!!).apply {
            this.title = gElement.param
            this.hint = gElement.defaultValues
        }
    }
}

class FactoryIntRow : IFormElement {
    override fun create(gElement: GElements): BaseFormElement<*> {
        return FormNumberEditTextElement(gElement.id!!).apply {
            this.title = gElement.param
            this.hint = gElement.defaultValues
        }
    }
}

class FactoryPickerInputRow : IFormElement {
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


class FormElementMapper {

    companion object {

        fun decodeJSON(): GEnergyFormModel? {
            val json = this.javaClass.getResourceAsStream("preaudit.json")
                    .bufferedReader().use { it.readText() }

            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter<GEnergyFormModel>(GEnergyFormModel::class.java)
            var formModel: GEnergyFormModel? = null

            try {
                formModel = jsonAdapter.fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return formModel

        }

        fun mapSectionIdsToElements(model: GEnergyFormModel) : HashMap<Int, List<GElements>>? =
                Mapper(model = model).mapSIdToGElements

        fun mapSectionIdsToName(model: GEnergyFormModel) : HashMap<Int, String>? =
                Mapper(model = model).mapSIdToSName

        fun mapIdToElements(model: GEnergyFormModel) : HashMap<Int, GElements>? =
                Mapper(model = model).mapEIdToGElements

        fun sortedElementIds(model: GEnergyFormModel) : List<Int>? =
                Sorter(mapIndex = Mapper(model = model).mapIndexSID).sortedIds

        fun sortedFormElementIds(model: GEnergyFormModel) : List<Int>? =
                Sorter(mapIndex = Mapper(model = model).mapIndexEID).sortedIds

    }

}



private class Sorter(private val mapIndex: HashMap<Int, Int>) {

    var sortedIds: List<Int> = listOf()

    private fun sortMap() {
        val sortedKeys = mapIndex.keys.sorted()
        sortedIds = sortedKeys.map {
            mapIndex.getValue(it)
        }
    }

    init { sortMap() }

}

private class Mapper(val model: GEnergyFormModel) {
    var mapSIdToSName: HashMap<Int, String> = hashMapOf()
    var mapSIdToGElements: HashMap<Int, List<GElements>>  = hashMapOf()
    var mapEIdToGElements: HashMap<Int, GElements> = hashMapOf()
    var mapIndexSID: HashMap<Int, Int> = hashMapOf()
    var mapIndexEID: HashMap<Int, Int> = hashMapOf()

    private fun mapSId() {
        val form = model.geminiForm
        if (form == null) {
            print("Empty DTO Form")
            return
        }
        form.forEach { block ->
            val sectionId = block.id
            val index = block.index
            val gElements = block.elements
            val sectionName = block.section
            if (sectionId != null && index != null && gElements != null && sectionName != null) {
                this.mapSIdToGElements[sectionId] = gElements
                this.mapSIdToSName[sectionId] = sectionName
                this.mapIndexSID[index] = sectionId
                gElements.forEach { gElements  ->
                    val elementId = gElements.id
                    val index = gElements.index
                    if (elementId != null && index != null) {
                        this.mapEIdToGElements[elementId] = gElements
                        this.mapIndexEID[index] = elementId
                    }
                }
            }
        }
    }

    init {
        mapSId()
    }

}


class MapperTest {

    @Test
    fun testMapSid() {
        val formModel = FormElementMapper.decodeJSON()
        formModel?.let {
            val result = Mapper(it)

            println(result.mapEIdToGElements)
            println(result.mapSIdToSName)
            println(result.mapEIdToGElements)
            println(result.mapIndexSID)
            println(result.mapIndexEID)

        }
    }
}





class FormElementMapperTest {

    @Test
    fun testDecodeJSON() {
        val formModel = FormElementMapper.decodeJSON()

        formModel?.let {
            it.geminiForm?.forEach {
                println(it.section)
                println()

                it.elements?.forEach {
                    println(it.id)
                    println(it.param)
                    println(it.dataType)
                    println()
                }
            }
        }

    }
}


class BaseRowTypeTest {

    @Test
    fun testReverseLookup() {
        val type = BaseRowType.get("textrow")
        print(type.toString())
    }

    @Test
    fun testSplitString() {
//        val sample = "01:Item1,02:Item2,03:Item3,Item4"
//        val optionItems = sample.split(",")
//        var listOptionItem: MutableList<ListItem> = mutableListOf()
//
//        optionItems.forEach {
//            val optionItem= it.split(":")
//            if (optionItem.count() == 2) {
//                listOptionItem.add(ListItem(optionItem[0].toLong(), optionItem[1]))
//            }
//        }
//
//        print(listOptionItem)

    }
}



