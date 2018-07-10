package com.gemini.energy.service

import com.gemini.energy.domain.entity.Feature

interface IComputable {

    fun featurePreAudit(auditId: Int): List<Feature>
    fun featureType(typeId: Int, subTypeId: Int): List<Feature>
    fun compute(): List<OutgoingRows>

    // ** Cost (GAS | ELECTRIC)
    // ** Usage Hours (TOU Mapper)

}