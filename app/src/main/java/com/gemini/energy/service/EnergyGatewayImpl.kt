package com.gemini.energy.service

import com.gemini.energy.domain.entity.Audit
import com.gemini.energy.domain.gateway.AuditGateway
import com.gemini.energy.domain.gateway.EnergyGateway
import io.reactivex.Observable

class EnergyGatewayImpl(
        private val service: EnergyService,
        private val auditGateway: AuditGateway,
        private val uploader: IUploader) : EnergyGateway {

    override fun compute(audit: Audit, auditGateway: AuditGateway): Observable<List<OutgoingRows>> {
        return service.crunch()
    }

    override fun upload(outgoingRows: List<OutgoingRows>): Observable<Unit> {
        return uploader.upload(outgoingRows)
    }
}