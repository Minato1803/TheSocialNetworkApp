package com.datn.thesocialnetwork.data.repository

import android.util.Log
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils.formatQuery
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.SearchModel
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.google.firebase.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.DataStatus
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId

@ExperimentalCoroutinesApi
class SearchRespository @Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
    private val postRepository: PostRepository,
) {

    fun getDatabaseUser() = mFirebaseDb.getReference(FirebaseNode.user)
    fun getDatabaseTAG() = mFirebaseDb.getReference(FirebaseNode.tag)

    fun searchUser(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        getUsers(query.formatQuery()).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val u = mutableListOf<SearchModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(UserDetail::class.java)?.let { user ->
                            val userResponse = dataSnapshot.key?.let { UserResponse(it, user) }
                            u.add(SearchModel.UserItem(userResponse?.let {
                                ModelMapping.mapToUserModel(it)
                            }))
                        }
                    }

                    launch {
                        Log.d("TAG", u.size.toString())
                        send(SearchStatus.Success(u))
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
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

    fun searchTag(query: String): Flow<SearchStatus> = channelFlow {

        send(SearchStatus.Loading)

        val text = query.formatQuery()
        Log.d("searchTag", "$text")
        if (text.isEmpty())
        {
            Log.d("tagGetVal", "empty")
            send(SearchStatus.Interrupted)
            close()
        } else {
            getHashtags(text).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val u = mutableListOf<SearchModel>()
                        Log.d("tagGetVal", "sn: ${snapshot.toString()}")
                        snapshot.children.forEach { dataSnapshot ->
                            Log.d("tagGetVal", "ds: ${dataSnapshot.toString()}")
                            dataSnapshot.key?.let { key ->
                                Log.d("tagGetVal", "key: ${key.toString()}")
                                u.add(SearchModel.TagItem(TagModel(key,
                                    dataSnapshot.childrenCount)))
                            }
                        }
                        Log.d("tagGetVal", "list ${u.toString()}")
                        launch {
                            send(SearchStatus.Success(u))
                            close()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
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

    private fun getHashtags(tag: String) =
        getDatabaseTAG()
            .orderByKey()
            .startAt(tag)
            .endAt(tag + "\uf8ff")

    private fun getHashtag(tag: String) =
        getDatabaseTAG()
            .orderByKey()
            .equalTo(tag.normalize())

    fun getTag(tag: String): Flow<GetStatus<TagModel>> = channelFlow {
        send(GetStatus.Loading)
        getHashtag(tag).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tags = snapshot.getValue(FirebaseNode.hashtagsType)
                    val counter = tags?.get(tag)?.size

                    launch {
                        send(
                            if (counter == null) {
                                GetStatus.Failed(Message(R.string.something_went_wrong))
                            } else {
                                GetStatus.Success(TagModel(tag, counter.toLong()))
                            }
                        )
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }
            }
        )
        awaitClose()
    }

    fun getAllPostsFromTag(tag: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        Log.d("tag", "start get all post from tag")
        send(GetStatus.Loading)

        getDatabaseTAG().orderByKey().equalTo(tag.normalize())
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tags =
                            snapshot.getValue(FirebaseNode.hashtagsType)?.get(tag)?.map { it.key }

                        if (tags.isNullOrEmpty()) {
                            launch {
                                send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                                close()
                            }
                        } else {
                            val postToDisplay = tags.size
                            var tagsQueried = 0
                            val posts: MutableList<PostWithId> = mutableListOf()

                            fun sendDataAndCheckClose() {
                                tagsQueried++
                                launch {
                                    if (tagsQueried == postToDisplay) {
                                        send(GetStatus.Success<List<PostWithId>>(posts))
                                        close()
                                    }
                                }
                            }
                            tags.forEach { id ->

                                postRepository.getPostById(id).addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val post = snapshot.getValue(PostsModel::class.java)
                                            if (post != null) {
                                                Log.d("postItemTag", "$post")
                                                val postItem =
                                                    PostWithId(id, post, post.image?.toList())
                                                posts.add(postItem)
                                            } else {
                                                Log.d("postItemTag", "failed")
                                            }
                                            sendDataAndCheckClose()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            sendDataAndCheckClose()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close()
                    }

                }
            )
        awaitClose()
    }

    fun findRecommendedPosts(size: Int = Const.RECOMMENDED_POSTS_SIZE): Flow<DataStatus<PostsModel>> =
        channelFlow {

            postRepository.getDatabasePost().orderByChild(FirebaseNode.postsCreatedTime)
                .limitToLast(size)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val posts = snapshot.getValue(FirebaseNode.postsType)
                            Log.d("postItemRmVal", "$posts")
                            if (posts != null) {
                                launch {
                                    send(
                                        DataStatus.Success(
                                            if (GlobalValue.USER != null) {
                                                val listPost = hashMapOf<String, PostsModel>()
                                                val postsItem = posts.filterValues {
                                                    it.ownerId != GlobalValue.USER!!.uidUser
                                                }
                                                postsItem.forEach {
                                                    listPost[it.key] = it.value
                                                }
                                                Log.d("postItemRm", "$listPost")
                                                listPost
                                            } else {
                                                posts
                                            }
                                        )
                                    )
                                    close()
                                }
                            } else {
                                Log.d("postItemRm", "failed")
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }

                    }
                )

            awaitClose()
        }
}