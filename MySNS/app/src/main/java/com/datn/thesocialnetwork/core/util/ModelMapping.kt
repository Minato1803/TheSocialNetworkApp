package com.datn.thesocialnetwork.core.util

import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse

object ModelMapping {

    fun mapToUserResponse(
        email: String,
        uidUser: String,
        password: String,
        userName: String,
        firstName: String,
        lastName: String,
    ): UserResponse =
        UserResponse(
            uidUser = uidUser,
            UserDetail(
                email = email,
                password = password,
                userName = userName,
                firstName = firstName,
                lastName = lastName
            )
        )
}