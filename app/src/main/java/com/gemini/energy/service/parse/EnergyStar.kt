package com.gemini.energy.service.parse


interface IEnergyStar {
    fun check(modelNumber: String, company: String): Boolean
}

class EnergyStar: IEnergyStar {

    override fun check(modelNumber: String, company: String): Boolean {
        return false
    }

}
