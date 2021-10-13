package com.datn.thesocialnetwork.data.repository

import com.datn.thesocialnetwork.data.datasource.firebase.UserFirebase
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.FirebaseAuthAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val mUserFirebase: UserFirebase,
) {
    fun getUserFirebaseAuth() = mUserFirebase.getAuth()

    suspend fun insertUser(userRes: UserResponse): DataSnapshot {
        val userNode = mUserFirebase.insertUser(userRes)
        return userNode
    }

    suspend fun getAllUserNode(): DataSnapshot =
        mUserFirebase.getDatabase().get().await()

    suspend fun updateUser(uidUser: String, userDetail: UserDetail) =
        mUserFirebase.updateUser(uidUser, userDetail)


    suspend fun uploadUserAvatar(uidUser: String, imgByteAvatar: ByteArray) =
        mUserFirebase.uploadUserAvatar(uidUser, imgByteAvatar)

    suspend fun getUserById(uidUser: String): DataSnapshot =
        mUserFirebase.getUserById(uidUser)

    fun getCurrentUserFirebase() = mUserFirebase.getAuth().currentUser
}
