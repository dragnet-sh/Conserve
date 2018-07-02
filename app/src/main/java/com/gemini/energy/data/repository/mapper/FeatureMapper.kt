package com.gemini.energy.data.repository.mapper

import com.gemini.energy.data.local.model.FeatureLocalModel
import com.gemini.energy.domain.entity.Feature

class FeatureMapper {

    fun toLocal(feature: List<Feature>): List<FeatureLocalModel> {

        return feature.map {
            FeatureLocalModel(
                    it.id,

                    it.formId,
                    it.belongsTo,
                    it.dataType,

                    it.auditId,
                    it.zoneId,
                    it.typeId,

                    it.valueString,
                    it.valueInt,
                    it.valueDouble,

                    it.createdAt,
                    it.updatedAt
            )
        }
    }

}