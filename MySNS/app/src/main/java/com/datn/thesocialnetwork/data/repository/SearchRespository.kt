package com.datn.thesocialnetwork.data.repository

import android.util.Log
import com.datn.thesocialnetwork.core.api.status.SearchStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.SystemUtils.formatQuery
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.SearchModel
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class SearchRespository@Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
) {

    fun getDatabaseUser() = mFirebaseDb.getReference(FirebaseNode.user)
    fun getDatabaseTAG() = mFirebaseDb.getReference(FirebaseNode.tag)

    @ExperimentalCoroutinesApi
    fun searchUser(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        getUsers(query.formatQuery()).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val u = mutableListOf<SearchModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(UserDetail::class.java)?.let { user ->
                            u.add(SearchModel.UserItem(user))
                        }
                    }

                    launch {
                        Log.d("TAG",u.size.toString())
                        send(SearchStatus.Success(u))
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(SearchStatus.Interrupted)
                        close()
                    }
                }
            }
        )
        awaitClose()
    }

    private fun getUsers(nick: String) =
        getDatabaseUser()
            .orderByChild(FirebaseNode.userName)
            .startAt(nick.lowercase(Locale.getDefault()))
            .endAt(nick + "\uf8ff")

    fun getUserById(userId: String) =
        getDatabaseUser()
            .orderByChild(FirebaseNode.uidUser)
            .equalTo(userId)

    fun getUserByName(username: String) =
        getDatabaseUser()
            .orderByChild(FirebaseNode.uidUser)
            .equalTo(username.normalize())

    @ExperimentalCoroutinesApi
    fun searchTag(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        val text = query.formatQuery()
        if (text.isEmpty()) // tag cannot be empty because search is based on key, not value
        {
            send(SearchStatus.Interrupted)
            close()
        }
        else
        {
            getHashtags(text).addListenerForSingleValueEvent(
                object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val u = mutableListOf<SearchModel>()

                        snapshot.children.forEach { dataSnapshot ->
                            dataSnapshot.key?.let { key ->
                                u.add(SearchModel.TagItem(TagModel(key, dataSnapshot.childrenCount)))
                            }
                        }

                        launch {
                            send(SearchStatus.Success(u))
                            close()
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        launch {
                            send(SearchStatus.Interrupted)
                            close()
                        }
                    }
                }
            )
        }

        awaitClose()
    }

    /**
     * Get all hashtags which are like given String
     */
    private fun getHashtags(tag: String) =
        getDatabaseTAG()
            .orderByKey()
            .startAt(tag)
            .endAt(tag + "\uf8ff")

    /**
     * Get hashtag which is equal to given String
     */
    private fun getHashtag(tag: String) =
        getDatabaseTAG()
        .orderByKey()
        .equalTo(tag.normalize())
}