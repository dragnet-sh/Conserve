package com.gemini.energy.service

import android.content.Context
import com.gemini.energy.presentation.util.ERateKey

open class EnergyUtility(private val context: Context, private val utility: IUtility) {

    /**
     * The Rate Structure Gets Setup from the PreAudit
     * */
    lateinit var content: String
    lateinit var structure: HashMap<String, List<String>>

    /**
     * To be set by the Child Class later on
     * */
    private fun getResourcePath() = utility.getResourcePath()
    private fun getSeparator() = utility.getSeparator()
    private fun getRowIdentifier() = utility.getRowIdentifier()

    fun build(): EnergyUtility {
        val inputStream= context.resources.assets.open(getResourcePath())
        val text = inputStream.bufferedReader().use { it.readText() }
        val collection = text.lines()
        val outgoing: HashMap<String, List<String>> = hashMapOf()

        if (collection.count() > 0) {
            val header = collection[0]
            collection.drop(0)
            collection.forEach {
                if (it.matches(getRowIdentifier())) {
                    val result = parseLine(it, getSeparator())
                    utility.getKey(result)
                            .forEachIndexed { index, key ->
                                outgoing[key] = utility.getValue(result, header)[index]
                            }
                }
            }
        }

        this.structure = outgoing
        return this
    }


    companion object {

        private fun parseLine(line: String, separator: Char) : List<String> {
            val result = mutableListOf<String>()
            val builder = StringBuilder()
            var quotes = 0

            for (ch in line) {
                when {
                    ch == '\"' -> {
                        quotes++
                        builder.append(ch)
                    }
                    (ch == '\n') || (ch == '\r') -> { }
                    (ch == separator) && (quotes % 2 == 0) -> {
                        val tmp = quotes % 2
                        result.add(builder.toString().trim())
                        builder.setLength(0)
                    }
                    else -> builder.append(ch)
                }
            }

            return result
        }

    }

}


interface IUtility {
    fun getKey(columns: List<String>): List<String>
    fun getValue(columns: List<String>, header: String): List<List<String>>

    fun getResourcePath(): String
    fun getSeparator(): Char
    fun getRowIdentifier(): Regex
    fun getRate(): String
}

class Electricity(private val rateStructure: String) : IUtility {

    enum class EKey(val index: Int) { Season(1), Peak(4) }
    enum class EValue(val index: Int) { EnergyCharge(5), Average(6) }

    override fun getKey(columns: List<String>) = listOf(columns[EKey.Season.index] + "-" + columns[EKey.Peak.index])
    override fun getValue(columns: List<String>, header: String) = listOf(listOf(columns[EValue.EnergyCharge.index],
            columns[EValue.Average.index]))

    override fun getResourcePath() = "utility/pge_electric.csv"
    override fun getSeparator(): Char = ','
    override fun getRate() = rateStructure
    override fun getRowIdentifier() = "^${getRate()}${getSeparator()}.*".toRegex()

}

class Gas(private val rateStructure: String = "") : IUtility {

    override fun getKey(columns: List<String>) = keys
    override fun getValue(columns: List<String>, header: String): List<List<String>> {
        val outgoing: MutableList<List<String>> = mutableListOf()
        val lookup = header.split(getSeparator())
        keys.forEach {
            outgoing.add(listOf(columns[lookup.indexOf(it)]))
        }

        return outgoing
    }

    override fun getResourcePath() = "utility/pge_gas.csv"
    override fun getSeparator() = ','
    override fun getRowIdentifier(): Regex { return ".*".toRegex() }
    override fun getRate() = rateStructure

    companion object {
        private val keys = listOf(
                ERateKey.Slab1.value, ERateKey.Slab2.value, ERateKey.Slab3.value,
                ERateKey.Slab4.value, ERateKey.Slab5.value, ERateKey.Surcharge.value,
                ERateKey.SummerTransport.value, ERateKey.WinterTransport.value)
    }

}