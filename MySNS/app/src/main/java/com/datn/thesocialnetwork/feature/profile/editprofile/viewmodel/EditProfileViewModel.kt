package com.datn.thesocialnetwork.feature.profile.editprofile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.datn.thesocialnetwork.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val mApp: Application,
    private val mUserRepository: UserRepository,
) : AndroidViewModel(mApp) {

}