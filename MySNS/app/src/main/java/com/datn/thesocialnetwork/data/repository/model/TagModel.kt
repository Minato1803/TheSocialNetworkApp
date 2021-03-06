package com.datn.thesocialnetwork.data.repository.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagModel(
    val title: String = "",
    val count: Long = 0,
) : Parcelable