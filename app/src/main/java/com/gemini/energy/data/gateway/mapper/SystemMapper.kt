package com.gemini.energy.data.gateway.mapper

import android.util.Log
import com.gemini.energy.data.local.model.*
import com.gemini.energy.domain.entity.*
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.ELightingType
import com.gemini.energy.presentation.util.EZoneType

class SystemMapper {

    fun toEntity(type: AuditLocalModel) = Audit(
            type.auditId,
            type.name,

            type.createdAt,
            type.updatedAt
    )

    fun toEntity(zone: ZoneLocalModel) = Zone(
            zone.zoneId,
            zone.name,
            zone.type,

            zone.auditId,

            zone.createdAt,
            zone.updatedAt
    )

    fun toEntity(auditScopeParent: TypeLocalModel) = Type(
            auditScopeParent.auditParentId,
            auditScopeParent.name,
            auditScopeParent.type,
            auditScopeParent.subType,

            auditScopeParent.zoneId,
            auditScopeParent.auditId,

            auditScopeParent.createdAt,
            auditScopeParent.updatedAt
    )

    fun toEntity(feature: FeatureLocalModel) = Feature(
            feature.featureId,
            feature.formId,
            feature.belongsTo,
            feature.dataType,

            feature.auditId,
            feature.zoneId,
            feature.typeId,

            feature.key,
            feature.valueString,
            feature.valueInt,
            feature.valueDouble,

            feature.createdAt,
            feature.updatedAt
    )

    fun toEntity(computable: ComputableLocalModel): Computable<*> {

        val eZoneType = EZoneType.get(computable.auditScopeType)
        val entity = when (eZoneType) {
            EZoneType.Plugload      -> Computable<EApplianceType>(EApplianceType.get(computable.auditScopeSubType)!!)
            EZoneType.Lighting      -> Computable<ELightingType>(ELightingType.get(computable.auditScopeSubType)!!)
            else                    -> Computable()
        }

        entity.auditId = computable.auditId
        entity.auditName = computable.auditName
        entity.zoneId = computable.zoneId
        entity.zoneName = computable.zoneName
        entity.auditScopeId = computable.auditScopeId
        entity.auditScopeName = computable.auditScopeName
        entity.auditScopeType = eZoneType

        return entity
    }

}