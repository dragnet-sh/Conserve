package com.gemini.energy.service

import com.gemini.energy.domain.entity.Feature



class Refrigerator : EnergyBase(), IComputable {
    override fun featurePreAudit(auditId: Int): List<Feature> {
        return listOf()
    }

    override fun featureType(typeId: Int, subTypeId: Int): List<Feature> {
        return listOf()
    }

    override fun compute(): List<List<OutgoingRows>> {
        return listOf()
    }
}

class Motors: EnergyBase(), IComputable {
    override fun featurePreAudit(auditId: Int): List<Feature> {
        return listOf()
    }

    override fun featureType(typeId: Int, subTypeId: Int): List<Feature> {
        return listOf()
    }

    override fun compute(): List<List<OutgoingRows>> {
        return listOf()
    }
}


// ** Computable Generator Factory gives me a list of IComputable ** //

// ** Type 1 Service - Emits :: Single Row of Computable Data
// ** Type 2 Service - Emits :: List of Computable Data

class ComputableFactory {
    fun build() : IComputable? {
        return null
    }
}