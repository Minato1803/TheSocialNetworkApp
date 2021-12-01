package com.datn.thesocialnetwork.data.repository

import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.firebase.FirebaseListener
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
    private val mAuth: FirebaseAuth,
    private val followRepository: FollowRespository
) {
    companion object {
        @Volatile
        var userListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

        @Volatile
        var likeListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

        @Volatile
        var commentCounterListenerId: Int = 0
            @Synchronized get() = field++
            @Synchronized private set

    }

    fun getDatabaseUser(uidUser: String) = mFirebaseDb.getReference(FirebaseNode.user).child(uidUser)

    private val auth = Firebase.auth

    private val _loggedUser = MutableStateFlow(auth.currentUser)
    val loggedUser = _loggedUser.asStateFlow()

    private val _loggedUserData = MutableStateFlow(UserModel())
    val loggedUserData = _loggedUserData.asStateFlow()

    val requireUser: FirebaseUser
        get() = _loggedUser.value!!


    init
    {
        auth.addAuthStateListener {
            _loggedUser.value = it.currentUser
            it.currentUser?.let { user ->
                followRepository.loadLoggedUserFollowing(user.uid)
                loadUserData(user.uid)
            }
        }
    }

    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null

    private fun loadUserData(userId: String)
    {
        _userRef?.let { ref ->
            _userListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }

        // create listener to get user data (name, avatar url)
        _userRef = mFirebaseDb.getReference(FirebaseNode.user).child(userId)

        _userListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                snapshot.getValue(UserDetail::class.java)?.let { user ->
                    val userResponse = UserResponse(userId,user)
                    _loggedUserData.value = ModelMapping.mapToUserModel(userResponse)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }

        }
        _userRef!!.addValueEventListener(_userListener!!)
    }

    fun isOwnAccountId(userId: String): Boolean =
        GlobalValue.USER!!.uidUser == userId

    fun isOwnAccountName(username: String): Boolean
    {
        return GlobalValue.USER!!.userDetail.userName.lowercase(Locale.ENGLISH) == username.normalize()
    }

    private val userListeners: HashMap<Int, FirebaseListener<GetStatus<UserResponse>>> = hashMapOf()

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

            val dr = getDatabaseUser(userId)

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

}