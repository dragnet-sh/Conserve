package com.gemini.energy.service.parse

import com.gemini.energy.service.IComputable

interface IQuery {

    fun query(parameter: HashMap<String, Any>): List<IComputable>

}

class AlternateEnergyEquivalent : IQuery {

    override fun query(parameter: HashMap<String, Any>): List<IComputable> {
        return listOf()
    }

}
