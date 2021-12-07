package com.datn.thesocialnetwork.data.repository

import android.util.Log
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.google.firebase.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode.conversationsType
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.data.datasource.firebase.FirebaseListener
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.ConversationItem
import com.datn.thesocialnetwork.data.repository.model.UserModel
import java.util.HashMap

class ChatRespository @Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
) {
    fun getDatabaseChat() = mFirebaseDb.getReference(FirebaseNode.chat)
    fun getDatabaseUser(uidUser: String) =
        mFirebaseDb.getReference(FirebaseNode.user).child(uidUser)

    private fun getKeyFromTwoUsers(id1: String, id2: String): String =
        if (id1 > id2) id1 + id2 else id2 + id1

    private fun getFirstKeyFromTwoUsers(id1: String, id2: String): String =
        if (id1 > id2) id1 else id2

    private fun getSecondKeyFromTwoUsers(id1: String, id2: String): String =
        if (id1 < id2) id1 else id2

    @ExperimentalCoroutinesApi
    fun getMessages(userId: String): Flow<GetStatus<List<ChatMessage>>> = channelFlow {
        send(GetStatus.Loading)

        val key = getKeyFromTwoUsers(GlobalValue.USER!!.uidUser, userId)
        val ref = getDatabaseChat().child(key)
            .child(FirebaseNode.messageAllField)

        ref.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.getValue(FirebaseNode.messageType)
                    if (messages == null) {
                        launch {
                            send(GetStatus.Success<List<ChatMessage>>(data = listOf()))
                        }
                    } else {
                        launch {
                            // adding id to every message
                            messages.forEach {
                                it.value.id = it.key
                            }
                            send(
                                GetStatus.Success(
                                    data = messages.values.toList()
                                        .sortedByDescending { it.time })
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    launch {
                        send(GetStatus.Failed(message = Message(R.string.something_went_wrong)))
                    }
                }
            }
        )
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun sendMessage(userId: String, message: ChatMessage) = channelFlow<FirebaseStatus> {

        send(FirebaseStatus.Loading)


        val key = getKeyFromTwoUsers(GlobalValue.USER!!.uidUser, userId)
        val ref = getDatabaseChat().child(key)

        ref.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        /**
                         * The first message is sent between two users
                         */

                        val mKey = ref.push().key

                        if (mKey != null) {
                            val conversation: HashMap<String, Any?> = hashMapOf(
                                FirebaseNode.messageUser1 to getFirstKeyFromTwoUsers(
                                    GlobalValue.USER!!.uidUser,
                                    userId
                                ),
                                FirebaseNode.messageUser2 to getSecondKeyFromTwoUsers(
                                    GlobalValue.USER!!.uidUser,
                                    userId
                                ),
                                FirebaseNode.messageAllField to hashMapOf<String, Any>(
                                    mKey to message.toHashMap
                                )
                            )
                            ref.setValue(conversation)
                                .addOnFailureListener {
                                    launch {
                                        send(FirebaseStatus.Failed(Message(R.string.message_not_sent)))
                                        close()
                                    }
                                }
                                .addOnSuccessListener {
                                    launch {
                                        send(FirebaseStatus.Success(Message(R.string.message_sent)))
                                        close()
                                    }
                                }
                        } else {
                            // error
                            launch {
                                send(FirebaseStatus.Failed(Message(R.string.message_not_sent)))
                                close()
                            }
                        }
                    } else {
                        /**
                         * Users already have some messages
                         */
                        val msgRef = ref.child(FirebaseNode.messageAllField)
                        val mKey = msgRef.push().key

                        if (mKey != null) {
                            msgRef.child(mKey).setValue(message.toHashMap)
                                .addOnFailureListener {
                                    launch {
                                        send(FirebaseStatus.Failed(Message(R.string.message_not_sent)))
                                        close()
                                    }
                                }
                                .addOnSuccessListener {
                                    launch {
                                        send(FirebaseStatus.Success(Message(R.string.message_sent)))
                                        close()
                                    }
                                }
                        } else {
                            // error
                            launch {
                                send(FirebaseStatus.Failed(Message(R.string.message_not_sent)))
                                close()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    launch {
                        send(FirebaseStatus.Failed(Message(R.string.message_not_sent)))
                        close()
                    }
                }
            }
        )

        awaitClose()
    }

    fun deleteMessage(userId: String, message: ChatMessage) {
        val key = getKeyFromTwoUsers(GlobalValue.USER!!.uidUser, userId)
        val ref = getDatabaseChat().child(key)
        ref.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val msgRef = ref.child(FirebaseNode.messageAllField)
                            .child(message.id)
                            .setValue(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("failed", "failed delete")
                }
            })
    }

    /** message pattern */
    private val userListeners: HashMap<Int, FirebaseListener<GetStatus<UserResponse>>> = hashMapOf()

    fun removeUserListener(ownerHash: Int) {
        userListeners[ownerHash]?.removeListener()
        userListeners.remove(ownerHash)
    }

    @ExperimentalCoroutinesApi
    fun getUser(
        ownerHash: Int,
        userId: String,
    ): Flow<GetStatus<UserModel>> {
        return channelFlow {

            send(GetStatus.Loading)

            val dr = getDatabaseUser(userId)

            val l = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserDetail::class.java)?.let { user ->
                        launch {
                            val userResponse = UserResponse(userId, user)
                            val v = GetStatus.Success(ModelMapping.mapToUserModel(userResponse))
                            send(v)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
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

    @ExperimentalCoroutinesApi
    fun getAllConversations(): Flow<GetStatus<List<ConversationItem>>> = channelFlow {

        send(GetStatus.Loading)

        var calls = 0
        val conversations = mutableListOf<ConversationItem>()

        fun checkAndSend() {
            synchronized(calls)
            {
                calls++
                if (calls == 2) {
                    launch {
                        send(GetStatus.Success(conversations))
                        close()
                    }
                }
            }
        }

        val vel1 = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val r = snapshot.getValue(conversationsType)

                r?.forEach { entry ->

                    val lastMsg: ChatMessage? = entry.value.msg?.maxByOrNull {
                        it.value.time
                    }?.value

                    lastMsg?.let {
                        conversations.add(
                            ConversationItem(
                                lastMessage = lastMsg,
                                userId = entry.value.u2,
                                isRead = it.isRead
                            )
                        )
                    }
                }

                checkAndSend()
            }

            override fun onCancelled(error: DatabaseError) {
                checkAndSend()
            }

        }

        val vel2 = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val r = snapshot.getValue(conversationsType)

                r?.forEach { entry ->

                    val lastMsg: ChatMessage? = entry.value.msg?.maxByOrNull {
                        it.value.time
                    }?.value

                    lastMsg?.let {
                        conversations.add(
                            ConversationItem(
                                lastMessage = lastMsg,
                                userId = entry.value.u1,
                                isRead = it.isRead
                            )
                        )
                    }
                }

                checkAndSend()
            }

            override fun onCancelled(error: DatabaseError) {
                checkAndSend()
            }

        }

        getDatabaseChat().orderByChild(FirebaseNode.messageUser1)
            .equalTo(GlobalValue.USER!!.uidUser)
            .addListenerForSingleValueEvent(vel1)

        getDatabaseChat().orderByChild(FirebaseNode.messageUser2)
            .equalTo(GlobalValue.USER!!.uidUser)
            .addListenerForSingleValueEvent(vel2)

        awaitClose()
    }
}