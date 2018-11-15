package com.gemini.energy.presentation.audit.list.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class AuditModel(
        val id: Int,
        val name: String,
        val createdAt: Date,
        val updateAt: Date
) : Parcelable
