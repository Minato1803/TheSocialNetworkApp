package com.datn.thesocialnetwork.feature.login.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val mApp: Application,
    private val mUserRepository: UserRepository,
): AndroidViewModel(mApp) {

    val liveLoginUser = MutableLiveData<Response<FirebaseUser>>()
    val liveDataLogin = MutableLiveData<Response<UserResponse>>()
    val liveDataCheckLogin = MutableLiveData<UserResponse?>()

    private val mAuth = FirebaseAuth.getInstance()

    fun LoginWithEmailPassword(email: String, password: String) {
        if (!SystemUtils.hasInternetConnection(mApp)) {
            liveLoginUser
                .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
        } else {
            liveLoginUser.postValue(Response.Loading())

            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = mAuth.currentUser
                        liveLoginUser.postValue(Response.Success(user))
                    } else {
                        liveLoginUser.postValue(Response.Error("Đăng nhập thất bại!"))
                    }
                }
                .addOnFailureListener {
                    liveLoginUser.postValue(Response.Error(it.message.toString()))
                }
        }
    }

    fun getInfoUser(
        accFirebase: FirebaseUser,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!SystemUtils.hasInternetConnection(mApp)) {
                liveDataLogin
                    .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
            } else {
                liveDataLogin.postValue(Response.Loading())
                try {
                    val userNode = mUserRepository.getUserById(accFirebase.uid)
                    signIn(userNode, liveDataLogin)
                } catch (ex: ApiException) {
                    liveDataLogin.postValue(Response.Error(ex.message.toString()))
                }
            }
        }

    fun signIn(
        userNode: DataSnapshot,
        liveData: MutableLiveData<Response<UserResponse>>,
    ) {
        val userResponse =
            UserResponse(userNode.key.toString(), userNode.getValue(UserDetail::class.java)!!)

        liveData.postValue(Response.Success(userResponse))
    }

    private fun getUserById(userId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            var userResponse: UserResponse? = null
            val userNode = mUserRepository.getUserById(userId)

            userResponse =
                UserResponse(userId, userNode.getValue(UserDetail::class.java)!!)
            liveDataCheckLogin.postValue(userResponse)
        }

    fun setRememberUserId(userId: String?) {
        mUserRepository.setRememberUserId(userId)
    }

    fun checkLogin() {
        val userId = mUserRepository.getUserIdLogin()
        if (mUserRepository.getCurrentUserFirebase() != null && userId.isNotBlank()) {
            getUserById(userId)
        } else liveDataCheckLogin.postValue(null)
    }

}