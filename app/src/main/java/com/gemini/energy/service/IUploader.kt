package com.gemini.energy.service

import io.reactivex.Observable

interface IUploader {

    fun upload(outgoingRows: List<OutgoingRow>): Observable<Unit>

}