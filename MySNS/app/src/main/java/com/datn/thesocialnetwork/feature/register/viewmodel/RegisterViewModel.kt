package com.datn.thesocialnetwork.feature.register.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class RegisterViewModel @Inject constructor(
    private val mApp: Application,
    private val mUserRepository: UserRepository,
) : AndroidViewModel(mApp) {

    val liveDataRegisterUser = MutableLiveData<Response<FirebaseUser>>()
    val liveDataInsertUser = MutableLiveData<Response<UserResponse>>()

    private val mAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String) {
        if (!SystemUtils.hasInternetConnection(mApp)) {
            liveDataRegisterUser
                .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
        } else {
            liveDataRegisterUser.postValue(Response.Loading())
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = mAuth.currentUser
                        liveDataRegisterUser.postValue(Response.Success(user))
                    } else {
                        liveDataRegisterUser.postValue(Response.Error())
                    }
                }
                .addOnFailureListener {
                    liveDataRegisterUser.postValue(Response.Error(it.message.toString()))
                }
        }
    }

    fun InsertUserToFirebase(
        accFirebase: FirebaseUser,
        password: String,
        userName: String,
        firstName: String,
        lastName: String,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!SystemUtils.hasInternetConnection(mApp)) {
                liveDataInsertUser
                    .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
            } else {
                liveDataInsertUser.postValue(Response.Loading())
                try {
                    val userResponse =
                        ModelMapping.mapToUserResponse(
                            email = accFirebase.email.toString(),
                            uidUser = accFirebase.uid,
                            password = password,
                            userName = userName,
                            firstName = firstName,
                            lastName = lastName,
                            onlineStatus = 0
                        )
                    signUp(userResponse, accFirebase, liveDataInsertUser)
                } catch (ex: ApiException) {
                    liveDataInsertUser.postValue(Response.Error(ex.message.toString()))
                }
            }
        }


    private suspend fun signUp(
        userResponse: UserResponse,
        accFirebase: FirebaseUser,
        liveData: MutableLiveData<Response<UserResponse>>,
    ) {
        val userNode = mUserRepository.insertUser(userResponse)
        if (userNode.exists()) {
            liveData.postValue(Response.Success(userResponse))
        } else {
            accFirebase.delete().await()
            liveData.postValue(Response.Error())
        }
    }

    fun setRememberUserId(userId: String?) {
        mUserRepository.setRememberUserId(userId)
    }
}