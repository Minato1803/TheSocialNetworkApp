package com.datn.thesocialnetwork.data.repository

import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.firebase.FirebaseListener
import com.datn.thesocialnetwork.data.datasource.firebase.UserFirebase
import com.datn.thesocialnetwork.data.datasource.local.sharedprefs.LoginSharedPrefs
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val mUserFirebase: UserFirebase,
    private val mLoginSharedPrefs: LoginSharedPrefs,
    private val mFirebaseDb: FirebaseDatabase,
) {

    fun getUserFirebaseAuth() = mUserFirebase.getAuth()
    private val userListeners: HashMap<Int, FirebaseListener<GetStatus<UserModel>>> = hashMapOf()

    fun removeUserListener(ownerHash: Int)
    {
        userListeners[ownerHash]?.removeListener()
        userListeners.remove(ownerHash)
    }

    @ExperimentalCoroutinesApi
    fun getUser(
        ownerHash: Int,
        userId: String,
    ): Flow<GetStatus<UserModel>>
    {
        return channelFlow {

            send(GetStatus.Loading)

            val dr = mFirebaseDb.getReference(FirebaseNode.user).child(userId)

            val l = object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    snapshot.getValue(UserDetail::class.java)?.let { user ->
                        launch {
                            val userResponse = UserResponse(userId,user)
                            val v = GetStatus.Success(ModelMapping.mapToUserModel(userResponse))
                            send(v)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        val v = GetStatus.Failed(Message(R.string.something_went_wrong))
                        send(v)
                    }
                }

            }

            userListeners[ownerHash] = FirebaseListener(l, dr)
            userListeners[ownerHash]?.addListener()

            awaitClose()
        }
    }

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

    fun getUserByID(uidUser: String) =
        mFirebaseDb.getReference(FirebaseNode.user)
            .orderByChild(FirebaseNode.uidUser)
            .equalTo(uidUser)

    fun getUserByName(username: String) =
        mFirebaseDb.getReference(FirebaseNode.user)
            .orderByChild(FirebaseNode.userName)
            .equalTo(username.normalize())

    fun getCurrentUserFirebase() = mUserFirebase.getAuth().currentUser

    fun setRememberUserId(userId: String?) {
        mLoginSharedPrefs.savePhoneNumber(userId)
    }

    fun getUserIdLogin() = mLoginSharedPrefs.getUserId()
}
