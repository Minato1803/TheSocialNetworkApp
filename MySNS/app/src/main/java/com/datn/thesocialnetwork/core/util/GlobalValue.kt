package com.datn.thesocialnetwork.core.util

import android.Manifest
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.UserModel

object GlobalValue {

    var USER: UserResponse? = null
    var USER_DETAIL: UserModel? = null

    val listPermissionSetAvatar: Array<String?> =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

}