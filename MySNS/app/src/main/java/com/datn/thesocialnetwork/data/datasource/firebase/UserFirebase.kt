package com.datn.thesocialnetwork.data.datasource.firebase

import android.net.Uri
import android.util.Log
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.FirebaseNode.uidUser
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.FirebaseAuthAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserFirebase @Inject constructor(
    private val mStorage: FirebaseStorage,
    private val mDatabase: FirebaseDatabase,
    private val mAuth: FirebaseAuth,
) {

    fun getDatabase() = mDatabase.getReference(FirebaseNode.user)

    fun getAuth() = mAuth
    private fun getStorage() = mStorage.reference

    suspend fun insertUser(userRes: UserResponse): DataSnapshot {
        Log.d("insert", userRes.userDetail.email)
        getDatabase()
            .child(userRes.uidUser)
            .setValue(userRes.userDetail)
        return getUserById(userRes.uidUser)
    }

    suspend fun getUserById(uidUser: String): DataSnapshot =
        getDatabase().child(uidUser).get().await()

    suspend fun updateUser(uidUser: String, userDetail: UserDetail): DataSnapshot {
        val node = getDatabase()
            .child(uidUser)
        node.child(FirebaseNode.avatarUrl).setValue(userDetail.avatarUrl)
        node.child(FirebaseNode.firstName).setValue(userDetail.firstName)
        node.child(FirebaseNode.lastName).setValue(userDetail.lastName)
        node.child(FirebaseNode.email).setValue(userDetail.email)
        node.child(FirebaseNode.password).setValue(userDetail.password)
        node.child(FirebaseNode.birthday).setValue(userDetail.birthday)
        node.child(FirebaseNode.gender).setValue(userDetail.gender)
        return getUserById(uidUser)
    }

    suspend fun uploadUserAvatar(uidUser: String, imgByteAvatar: ByteArray): Uri =
        getStorage().child("MySNS/avatar/$uidUser")
            .putBytes(imgByteAvatar).await().storage.downloadUrl.await()
}