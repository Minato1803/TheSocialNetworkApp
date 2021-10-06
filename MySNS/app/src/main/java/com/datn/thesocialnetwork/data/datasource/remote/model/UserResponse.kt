package com.datn.thesocialnetwork.data.datasource.remote.model

import com.datn.thesocialnetwork.core.util.Const
import com.google.gson.annotations.SerializedName

data class UserResponse (
    @SerializedName("uidUser") var uidUser: String = "",
    @SerializedName("userDetail") var userDetail: UserDetail,
)

data class UserDetail(

    @SerializedName("uidGoogle") var uidGoogle: String = "",
    @SerializedName("firstName") var firstName: String = "",
    @SerializedName("lastName") var lastName: String = "",
    @SerializedName("birthday") var birthday: String = "",
    @SerializedName("gender") var gender: String = Gender.UNDEFINED.nameType,
    @SerializedName("email") var email: String = "",
    @SerializedName("password") var password: String = "",
    @SerializedName("avatarUrl") var avatarUrl: String = Const.avatarDefaultUrl,
)

enum class Gender(val nameType: String) {
    UNDEFINED("Không xác định"),
    MALE("Nam"),
    FEMALE("Nữ"),
}