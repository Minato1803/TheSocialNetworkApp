package com.datn.thesocialnetwork.core.util

import android.Manifest
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse

object GlobalValue {

    var USER: UserResponse? = null

    val listPermissionSetAvatar: Array<String?> =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

}