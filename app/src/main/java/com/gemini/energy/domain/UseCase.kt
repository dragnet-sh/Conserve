package com.gemini.energy.domain

import io.reactivex.Observable

abstract class UseCase<in Params, Result> internal constructor(private val schedulers: Schedulers) {

    internal abstract fun buildObservable(params: Params?): Observable<Result>

    fun execute(params: Params? = null): Observable<Result> {
        return buildObservable(params)
                .subscribeOn(schedulers.subscribeOn)
                .observeOn(schedulers.observeOn, true)
    }
}