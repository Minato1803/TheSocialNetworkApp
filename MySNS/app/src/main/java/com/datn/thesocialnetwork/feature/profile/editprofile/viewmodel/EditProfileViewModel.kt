package com.datn.thesocialnetwork.feature.profile.editprofile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val mApp: Application,
    private val mUserRepository: UserRepository,
) : AndroidViewModel(mApp) {
    val liveDataUpdateUserDetail = MutableLiveData<Response<UserDetail>>()

    fun updateUserDetail(userUid: String, userDetail: UserDetail, imgAvatarByte: ByteArray?) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!SystemUtils.hasInternetConnection(mApp)) {
                liveDataUpdateUserDetail
                    .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
            } else {
                liveDataUpdateUserDetail.postValue(Response.Loading())
                try {
                    //upload avatar
                    imgAvatarByte?.let {
                        val url =
                            mUserRepository.uploadUserAvatar(userUid, imgAvatarByte).toString()
                        userDetail.avatarUrl = url
                    }
                    //update info
                    val userNode = mUserRepository.updateUser(userUid, userDetail)
                    val userDetailUpdated = userNode.getValue(UserDetail::class.java)
                    liveDataUpdateUserDetail.postValue(Response.Success(userDetailUpdated))
                } catch (ex: ApiException) {
                    liveDataUpdateUserDetail.postValue(Response.Error(ex.message.toString()))
                } catch (ex: IllegalStateException) {
                    liveDataUpdateUserDetail.postValue(Response.Error(ex.message.toString()))
                }
            }
        }
}