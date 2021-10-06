package com.datn.thesocialnetwork.feature.login.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.FirebaseAuthAccount
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val mApp: Application,
    private val mUserRepository: UserRepository,
): AndroidViewModel(mApp) {

    val liveDataAccFirebaseAuth = MutableLiveData<FirebaseAuthAccount>()
    val liveDataVerifyLogin = MutableLiveData<Response<String>>()
    val liveDataLogin = MutableLiveData<Response<UserResponse>>()

    fun verifyLogin(email: String, password: String, activity: Activity) {
        if (!SystemUtils.hasInternetConnection(mApp)) {
            liveDataVerifyLogin
                .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
        } else {
            liveDataVerifyLogin.postValue(Response.Loading())

            /*FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        liveDataVerifyLogin.postValue(Response.Success())
                    }
                    else {
                        liveDataVerifyLogin.postValue(Response.Error())
                    }
                }
                .addOnFailureListener {
                    liveDataVerifyLogin.postValue(Response.Error(it.message.toString()))
                }*/
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        liveDataVerifyLogin.postValue(Response.Success())
                    }
                    else {
                        liveDataVerifyLogin.postValue(Response.Error())
                    }
                }
                .addOnFailureListener {
                    liveDataVerifyLogin.postValue(Response.Error(it.message.toString()))
                }
        }
    }

    private suspend fun signIn(
        userNode: DataSnapshot,
        liveData: MutableLiveData<Response<UserResponse>>,
    ) {
        val userResponse =
            UserResponse(userNode.key.toString(), userNode.getValue(UserDetail::class.java)!!)
        liveData.postValue(Response.Success(userResponse))
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

    // LOGIN WITH GG
    fun checkUserExist(accGG: GoogleSignInAccount) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!SystemUtils.hasInternetConnection(mApp)) {
                liveDataLogin
                    .postValue(Response.Error(mApp.getString(R.string.str_error_socket_timeout)))
            } else {
                liveDataLogin.postValue(Response.Loading())
                try {
                    val credential = GoogleAuthProvider.getCredential(accGG.idToken, null)
                    val accFirebaseGG =
                        mUserRepository.signInWithCredential(credential).user
                    accFirebaseGG?.let { accFirebaseNN ->
                        var userNode: DataSnapshot? = null

                        val accFirebaseAuthGG = FirebaseAuthAccount(
                            uidGoogle = accFirebaseNN.uid,
                            firstName = accGG.givenName?.toString() ?: "",
                            email = accGG.email?.toString() ?: "")

                        //check exist
                        val listUser = mUserRepository.getAllUserNode()
                        for (element in listUser.children) {
                            if (accFirebaseNN.uid == element.child(FirebaseNode.uidGoogle).value.toString()) {
                                userNode = element
                                break
                            }
                        }

                        if (userNode != null && userNode.exists()) {
                            val userResponse = UserResponse(userNode.key.toString(),
                                userNode.getValue(UserDetail::class.java)!!)
                            liveDataLogin.postValue(Response.Success(userResponse))
                        } else {
                            liveDataLogin.postValue(Response.Error(mApp.getString(
                                R.string.err_no_exist)))
                            liveDataAccFirebaseAuth.postValue(accFirebaseAuthGG)
                        }
                    }
                } catch (ex: ApiException) {
                    liveDataLogin.postValue(Response.Error(ex.message.toString()))
                }
            }
        }
}