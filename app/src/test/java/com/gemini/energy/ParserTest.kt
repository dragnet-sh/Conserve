package com.gemini.energy

import com.squareup.moshi.Moshi
import org.junit.Test

class Product {
    val section: String? = null
    val elements: List<Elements>? = null
}

class Elements {
    val productname: String? = null
    val price: String? = null
}

class JSONParserTest {

    @Test
    public fun readJSON() {
        val json = this.javaClass.getResourceAsStream("sample.json")
                .bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter<Array<Product>>(Array<Product>::class.java)
        var products: Array<Product>? = null

        try {
            products = jsonAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        for (p in products!!) {
            println("${p.section}")

            p.elements.let {
                it?.forEach {

                    println("${it.price} -- ${it.productname}")

                }
            }
        }
   }
}


