package com.datn.thesocialnetwork.data.repository

import android.util.Log
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.FollowStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.data.repository.model.FollowerModel
import com.datn.thesocialnetwork.R
import com.google.firebase.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowRespository @Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
) {
    fun getDatabaseFollow() = mFirebaseDb.getReference(FirebaseNode.follow)

    private val _loggedUserFollowing = MutableStateFlow(listOf<String>())
    val loggedUserFollowing = _loggedUserFollowing.asStateFlow()

    private var followersListener: Triple<String, Query, ValueEventListener>? = null
    private var followingListener: Triple<String, Query, ValueEventListener>? = null

    fun getListFollower(uidUser: String) =
        getDatabaseFollow().orderByChild(FirebaseNode.desId).equalTo(uidUser)

    fun getListFollowing(uidUser: String) =
        getDatabaseFollow().orderByChild(FirebaseNode.sourceId).equalTo(uidUser)

    fun loadLoggedUserFollowing(id: String)
    {
        getListFollowing(id).addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    val followers = dataSnapshot.getValue(FirebaseNode.followedType)

                    if (followers != null)
                    {
                        val followingUsers = followers.map {
                            it.value.desId
                        }
                        Log.d("TAG", followingUsers[0].toString())

                        _loggedUserFollowing.value = followingUsers
                    }
                    else // user doesn't follow anyone
                    {
                        _loggedUserFollowing.value = listOf()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    //
                }
            }
        )
    }

    fun removeFollowersListener()
    {
        followersListener?.let {
            it.second.removeEventListener(it.third)
        }
        followersListener = null
    }

    fun removeFollowingListener()
    {
        followingListener?.let {
            it.second.removeEventListener(it.third)
        }
        followingListener = null
    }

    @ExperimentalCoroutinesApi
    fun getUserFollowersFlow(userId: String): Flow<SearchFollowStatus> = channelFlow {

        removeFollowersListener()

        send(SearchFollowStatus.Loading)

        val q = getListFollower(userId)
        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val followers = snapshot.getValue(FirebaseNode.followedType)

                if (followers != null)
                {
                    val followingUsers = followers.map {
                        it.value.desId
                    }

                    launch {
                        send(SearchFollowStatus.Success(followingUsers))
                    }

                }
                else // user doesn't follow anyone
                {
                    launch {
                        send(SearchFollowStatus.Success(listOf()))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                //
            }
        }

        followersListener = Triple(userId, q, l)

        q.addValueEventListener(l)

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun getUserFollowingFlow(userId: String): Flow<SearchFollowStatus> = channelFlow {
        removeFollowingListener()

        send(SearchFollowStatus.Loading)

        val q = getListFollowing(userId)
        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {

                val followers = snapshot.getValue(FirebaseNode.followedType)

                if (followers != null)
                {

                    val followingUsers = followers.map {
                        it.value.desId
                    }
                    Log.d("TAG", followingUsers.get(0).toString())

                    launch {
                        send(SearchFollowStatus.Success(followingUsers))
                        close()
                    }

                }
                else // user doesn't follow anyone
                {
                    launch {
                        send(SearchFollowStatus.Success(listOf()))
                        close()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                //
            }
        }

        followingListener = Triple(userId, q, l)

        q.addValueEventListener(l)

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun getFollowers(userId: String): Flow<GetStatus<List<String>>> = channelFlow {
        send(GetStatus.Loading)

        getListFollower(userId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val followers = snapshot.getValue(FirebaseNode.followedType)

                    launch {
                        send(GetStatus.Success(
                            followers?.map {
                                it.value.sourceId
                            } ?: listOf()
                        ))
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }
            }
        )

        awaitClose()
    }


    @ExperimentalCoroutinesApi
    fun getFollowing(userId: String): Flow<GetStatus<List<String>>> = channelFlow {
        send(GetStatus.Loading)

        getListFollowing(userId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val followers = snapshot.getValue(FirebaseNode.followedType)

                    launch {
                        send(GetStatus.Success(
                            followers?.map {
                                it.value.desId
                            } ?: listOf()
                        ))
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }
            }
        )

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun follow(userToFollowId: String): Flow<FollowStatus> = channelFlow {

        send(FollowStatus.LOADING)

        val keyToFollow = getDatabaseFollow().push().key

        val loggedUserId = GlobalValue.USER!!.uidUser

        if (keyToFollow != null && loggedUserId != null)
        {
            val follow = hashMapOf(
                FirebaseNode.desId to userToFollowId,
                FirebaseNode.sourceId to loggedUserId,
            )

            getDatabaseFollow().child(keyToFollow).setValue(follow)
                .addOnSuccessListener {
                    launch {
                        send(FollowStatus.SUCCESS)
                        close()
                    }
                }
                .addOnFailureListener {
                    launch {
                        send(FollowStatus.FAILED)
                        close()
                    }
                }
        }
        else
        {
            launch {
                send(FollowStatus.FAILED)
                close()
            }
        }

        awaitClose()
    }


    @ExperimentalCoroutinesApi
    fun unfollow(userToUnfollowId: String): Flow<FollowStatus> = channelFlow {

        send(FollowStatus.LOADING)

        val loggedUserId = GlobalValue.USER!!.uidUser

        if (loggedUserId != null)
        {
            val q = getListFollowing(loggedUserId)

            q.addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        dataSnapshot.children.forEach { followSnapshot ->
                            followSnapshot.getValue(FollowerModel::class.java)?.let { follow ->
                                if (follow.desId == userToUnfollowId)
                                {
                                    followSnapshot.ref.removeValue()
                                }
                            }
                        }
                        launch {
                            send(FollowStatus.SUCCESS)
                            close()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError)
                    {
                        launch {
                            send(FollowStatus.FAILED)
                            close()
                        }
                    }
                }
            )
        }
        else
        {
            launch {
                send(FollowStatus.FAILED)
                close()
            }
        }

        awaitClose()
    }
}