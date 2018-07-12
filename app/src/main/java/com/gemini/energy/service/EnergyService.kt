package com.gemini.energy.service

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.domain.entity.Zone
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.EZoneType
import io.reactivex.Observable
import java.util.*


class EnergyService(
        private val computableFactory: ComputableFactory,
        private val auditGateway: AuditGateway_Mock) {

    fun crunch(): Observable<List<OutgoingRows>> {
        return Observable.just(listOf())
    }

    // ** Computable Generator Factory gives me a list of IComputable ** //

    // ** Type 1 Service - Emits :: Single Row of Computable Data
    // ** Type 2 Service - Emits :: List of Computable Data



}


class AuditGateway_Mock {

    fun getAuditList(): List<Audit> {
        return listOf(
                Audit(1, "Audit - 01", Date(), Date()),
                Audit(2, "Audit - 02", Date(), Date())
        )
    }

    fun getZoneList(auditId: Int): List<Zone> {

        if (auditId == 1) {
            return listOf(
                    Zone(1, "Zone - A1Z1", "n/a", 1, Date(), Date()),
                    Zone(2, "Zone - A1Z2", "n/a", 1, Date(), Date()),
                    Zone(3, "Zone - A1Z3", "n/a", 1, Date(), Date())
            )
        }

        if (auditId == 2) {
            return listOf(
                    Zone(4, "Zone - A2Z4", "n/a", 2, Date(), Date()),
                    Zone(5, "Zone - A2Z5", "n/a", 2, Date(), Date())
            )
        }

        return listOf()

    }

    fun getAuditScopeList(zoneId: Int, type: String): List<Type> {

        if (zoneId == 1 && type == "plugload")
            return listOf(
                    Type(1, "Plugload - Fridge A1Z1PL1", EZoneType.Plugload.value, EApplianceType.Refrigerator.value, 1, null, Date(), Date()),
                    Type(2, "Plugload - Rack Oven A1Z1PL2", EZoneType.Plugload.value, EApplianceType.RackOven.value, 1, null, Date(), Date()),
                    Type(3, "Plugload -  A1Z1PL3", EZoneType.Plugload.value, EApplianceType.Fryer.value, 1, null, Date(), Date())
            )


        if (zoneId == 2 && type == "hvac") {
            return listOf(
                    Type(4, "HVAC - A1Z2HVAC4", EZoneType.HVAC.value, null, 2, null, Date(), Date())
            )
        }


        if (zoneId == 4 && type == "plugload") {
            return listOf(
                    Type(5, "Plugload - Ice Maker A2Z4PL5", EZoneType.Plugload.value, EApplianceType.IceMaker.value, 4, null, Date(), Date())
            )
        }


        if (zoneId == 4 && type == "motors") {
            return listOf(
                    Type(6, "Motor - A2Z4M6", EZoneType.Motors.value, null, 4, null, Date(), Date())
            )
        }

        return listOf()

    }

    fun getFeatureByType(zoneId: Int): List<Feature> {

        if (zoneId == 1) {
            return listOf(
                    Feature(1, 101, "type", "textrow", null, null,1, "Product Type", "2 Container Fridge", null, null, Date(), Date()),
                    Feature(2, 102, "type", "textrow", null, null,1, "Total Volume", "1000", null, null, Date(), Date()),
                    Feature(3, 103, "type", "decimalrow", null, null,1, "Fridge Capacity", "1000", null, null, Date(), Date()),
                    Feature(4, 101, "type", "textrow", null, null,1, "Model Number", "K12E", null, null, Date(), Date()),
                    Feature(5, 101, "type", "textrow", null, null,1, "Company", "Samsung", null, null, Date(), Date())
            )
        }

        return listOf()

    }

}