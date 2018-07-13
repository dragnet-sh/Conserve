package com.gemini.energy.service.device

import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.service.EnergyBase
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows

class Refrigerator : EnergyBase(), IComputable {

    override fun getFeaturePreAudit(preAudit: List<Feature>): List<Feature> {
        return listOf()
    }

    override fun getFeatureAuditScope(featureData: List<Feature>): List<Feature> {
        return listOf()
    }

    override fun compute(): List<List<OutgoingRows>> {
        return listOf()
    }
}
