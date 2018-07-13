package com.gemini.energy.data.gateway.mapper

import com.gemini.energy.data.local.model.*
import com.gemini.energy.domain.entity.*

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

    fun toEntity(computable: ComputableLocalModel) = Computable(
            computable.auditId,
            computable.auditName,

            computable.zoneId,
            computable.zoneName,

            computable.auditScopeId,
            computable.auditScopeName,
            computable.auditScopeType,
            computable.auditScopeSubType
    )

}