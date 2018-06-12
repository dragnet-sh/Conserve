package com.gemini.energy.presentation.zone.list.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypeModel(
        val id: Int?,
        var name: String?,
        val type: String?,

        val zoneId: Int?,
        val auditId: Int?
) : Parcelable

