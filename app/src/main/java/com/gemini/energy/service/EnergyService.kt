package com.gemini.energy.service

import io.reactivex.Observable


class EnergyService {

    fun crunch(): Observable<List<OutgoingRows>> {
        return Observable.just(listOf())
    }


    // ** Computable Generator Factory gives me a list of IComputable ** //

    // ** Type 1 Service - Emits :: Single Row of Computed Data
    // ** Type 2 Service - Emits :: List of Computed Data



}