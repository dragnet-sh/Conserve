package com.gemini.energy.service

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.entity.Zone

abstract class EnergyBase {

    protected lateinit var audit: Audit
    protected lateinit var zone: Zone

    abstract fun getFeaturePreAudit(preAudit: List<Feature>): List<Feature>
    abstract fun getFeatureAuditScope(featureData: List<Feature>): List<Feature>

}