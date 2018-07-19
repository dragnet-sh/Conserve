package com.gemini.energy.service

import android.content.Context

class EnergyUtility(private val context: Context) {

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
    private fun getResourcePath() = "utility/pge_electric.csv"
    private fun getSeparator() = ','
    private fun getRowIdentifierRegEx() = "^$rate${getSeparator()}.*".toRegex()

    fun build(): EnergyUtility {
        val inputStream= context.resources.assets.open(getResourcePath())
        val text = inputStream.bufferedReader().use { it.readText() }
        val collection = text.lines()
        val outgoing: HashMap<String, List<String>> = hashMapOf()

        if (collection.count() > 0) {
            collection.forEach {
                if (it.matches(getRowIdentifierRegEx())) {
                    val result = parseLine(it, getSeparator())

                    val key = result[EKey.Season.index] + "-" + result[EKey.Peak.index]
                    outgoing[key] = listOf(result[EValue.EnergyCharge.index], result[EValue.Average.index])
                }
            }
        }

        this.structure = outgoing
        return this
    }


    companion object {
        private const val TAG = "EnergyUtility"

        enum class EKey(val index: Int) { Season(1), Peak(4) }
        enum class EValue(val index: Int) { EnergyCharge(5), Average(6) }

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
