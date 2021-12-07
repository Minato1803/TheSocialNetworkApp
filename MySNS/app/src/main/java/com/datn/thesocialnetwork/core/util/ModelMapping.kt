package com.datn.thesocialnetwork.core.util

import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.UserModel

object ModelMapping {

    fun mapToUserResponse(
        email: String,
        uidUser: String,
        password: String,
        userName: String,
        firstName: String,
        lastName: String,
        onlineStatus: Long,
    ): UserResponse =
        UserResponse(
            uidUser = uidUser,
            UserDetail(
                email = email,
                password = password,
                userName = userName,
                firstName = firstName,
                lastName = lastName,
                onlineStatus = onlineStatus
            )
        )

    fun mapToUserModel(userResponse: UserResponse): UserModel =
        UserModel(
            uidUser = userResponse.uidUser,
            userName = userResponse.userDetail.userName,
            firstName = userResponse.userDetail.firstName,
            lastName = userResponse.userDetail.lastName,
            birthday = userResponse.userDetail.birthday,
            description = userResponse.userDetail.description,
            gender = userResponse.userDetail.gender,
            email = userResponse.userDetail.email,
            password = userResponse.userDetail.password,
            avatarUrl = userResponse.userDetail.avatarUrl,
            onlineStatus = userResponse.userDetail.onlineStatus
        )

    fun createUserDetail(userDetail: UserDetail, onlineStatus: Long) : UserDetail {
        return UserDetail(
            userName = userDetail.userName,
            firstName = userDetail.firstName,
            lastName = userDetail.lastName,
            birthday = userDetail.birthday,
            description = userDetail.description,
            gender = userDetail.gender,
            email = userDetail.email,
            password = userDetail.password,
            avatarUrl = userDetail.avatarUrl,
            onlineStatus = onlineStatus,
        )
    }
}