package com.datn.thesocialnetwork.data.repository.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FirebaseAuthAccount (
    var uidGoogle: String? = null,
    var firstName: String = "",
    var email: String = "",
) : Parcelable