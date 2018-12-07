package com.gemini.energy.presentation.type.list.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypeModel(
        val id: Int?,
        var name: String?,
        val type: String?,
        val subType: String?,

        val zoneId: Int?,
        val auditId: Long?
) : Parcelable

