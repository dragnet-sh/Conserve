package com.gemini.energy.service

import io.reactivex.Flowable

interface IComputable {
    fun compute(): Flowable<List<OutgoingRow>>
}