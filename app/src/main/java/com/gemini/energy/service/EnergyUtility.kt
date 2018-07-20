package com.gemini.energy.service

import android.content.Context

open class EnergyUtility(private val context: Context, private val utility: IUtility) {

    /**
     * The Rate Structure Gets Setup from the PreAudit
     * */
    var rate: String = "A-1"
    lateinit var content: String
    lateinit var structure: HashMap<String, List<String>>


    fun initRate(rate: String): EnergyUtility {
        this.rate = rate
        return this
    }


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
            collection.forEach {

                if (it.matches(getRowIdentifier())) {
                    val result = parseLine(it, getSeparator())
                    val key = utility.getKey(result)
                    outgoing[key] = utility.getValue(result)
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
    fun getKey(columns: List<String>): String
    fun getValue(columns: List<String>): List<String>
    fun getResourcePath(): String
    fun getSeparator(): Char
    fun getRowIdentifier(): Regex
    fun getRate(): String
}

//class Gas : IUtility {
//    override fun getKey(columns: List<String>): String { return "" }
//    override fun getValue(columns: List<String>): List<String> { return listOf() }
//}

class Electricity : IUtility {

    enum class EKey(val index: Int) { Season(1), Peak(4) }
    enum class EValue(val index: Int) { EnergyCharge(5), Average(6) }

    override fun getKey(columns: List<String>) = columns[EKey.Season.index] + "-" + columns[EKey.Peak.index]
    override fun getValue(columns: List<String>) = listOf(columns[EValue.EnergyCharge.index], columns[EValue.Average.index])
    override fun getResourcePath() = "utility/pge_electric.csv"
    override fun getSeparator(): Char = ','
    override fun getRate() = "A-1 TOU"
    override fun getRowIdentifier() = "^${getRate()}${getSeparator()}.*".toRegex()

}