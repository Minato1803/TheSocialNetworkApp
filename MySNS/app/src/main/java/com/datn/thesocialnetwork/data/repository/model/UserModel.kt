package com.datn.thesocialnetwork.data.repository.model

import android.os.Parcelable
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.data.datasource.remote.model.Gender
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(
    var uidUser: String = "",
    var userName: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var birthday: String = "",
    var description: String = "",
    var gender: String = Gender.UNDEFINED.nameType,
    var email: String = "",
    var password: String = "",
    var avatarUrl: String = Const.avatarDefaultUrl,
    var onlineStatus: Long = 0,
) : Parcelable